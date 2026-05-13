package com.anormalraft.rafts_combat.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.slf4j.Logger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.UUID;

import static com.anormalraft.rafts_combat.Rafts_Combat.LOGGER;

public class VectorUtils {
    //Gets the last value from getMaxZoom
    public static float lastMaxZoom = 0.0F;
    //Log helper for vectors
    static Vec3 oldVector = new Vec3(0,0,0);

    //Calculates useful sin cos values for vector offset calculations
    public static double[] sinCosAngleValues(){
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        float rotationAngleY = camera.getYRot() % 360;
        float rotationAngleXZ = camera.getXRot() % 360;
        float rotationAngleRoll = camera.getRoll() % 360;
        if(rotationAngleY < 0){
            rotationAngleY = 360 + rotationAngleY;
        }
        if(rotationAngleXZ < 0){
            rotationAngleXZ = 360 + rotationAngleXZ;
        }
        if(rotationAngleRoll < 0){
            rotationAngleRoll = 360 + rotationAngleRoll;
        }

        //XY offset data
        double angleValueRotY = ((rotationAngleY)/360) * (2 * Mth.PI);
        double sinValueRotY = Mth.sin((float) angleValueRotY);
        double cosValueRotY = Mth.cos((float) angleValueRotY);

        //Y offset data
        double angleValueRotXZ = ((rotationAngleXZ)/360) * (2*Mth.PI);
        double sinValueRotXZ = Mth.sin((float) angleValueRotXZ);
        double cosValueRotXZ = Mth.cos((float) angleValueRotXZ);

        //Unused but probably helpful for future
        double angleValueRotRoll = ((rotationAngleRoll)/360) * (2 * Mth.PI);
        double sinValueRotRoll = Mth.sin((float) angleValueRotRoll);
        double cosValueRotRoll = Mth.cos((float) angleValueRotRoll);

        return new double[]{sinValueRotY, cosValueRotY, sinValueRotXZ, cosValueRotXZ, sinValueRotRoll, cosValueRotRoll};
    }

    //Calculates an offsetVector of the endpoint (+x means left, -x means right of crosshair)
    public static Vec3 calculateOffsetVector(double offsetXZ, double offsetY, Vec3 endpoint){
        //Calculates an offsetVector of the endpoint
        double[] sinCosValuesArray = sinCosAngleValues();

        //Result
        return endpoint.add((sinCosValuesArray[3-1] * -sinCosValuesArray[1-1] * offsetY) + (sinCosValuesArray[2-1] * offsetXZ), offsetY * sinCosValuesArray[4-1], (sinCosValuesArray[3-1] * sinCosValuesArray[2-1] * offsetY) + (sinCosValuesArray[1-1] * offsetXZ));
    }

    //Gets the first person camera's position even if in third person
    public static Vec3 getFirstPersonCameraPosition(Camera mainCamera) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        boolean isMirroredThirdPerson = Minecraft.getInstance().options.getCameraType().isMirrored();
        Camera firstpersonCamera = new Camera();
        Method setPositionMethod = firstpersonCamera.getClass().getDeclaredMethod("setPosition", Vec3.class);
        setPositionMethod.setAccessible(true);
        Method setRotationMethod = firstpersonCamera.getClass().getDeclaredMethod("setRotation", float.class, float.class, float.class);
        setRotationMethod.setAccessible(true);
        Method getMaxZoomMethod = firstpersonCamera.getClass().getDeclaredMethod("getMaxZoom", float.class);
        getMaxZoomMethod.setAccessible(true);
        Method moveCameraMethod = firstpersonCamera.getClass().getDeclaredMethod("move", float.class, float.class, float.class);
        moveCameraMethod.setAccessible(true);

        setPositionMethod.invoke(firstpersonCamera, mainCamera.getPosition());
        setRotationMethod.invoke(firstpersonCamera, mainCamera.getYRot(), mainCamera.getXRot(), mainCamera.getRoll());
        float zoomValue = VectorUtils.lastMaxZoom;

        if(isMirroredThirdPerson){
            setRotationMethod.invoke(firstpersonCamera,mainCamera.getYRot() + 180.0F, -mainCamera.getXRot(), -mainCamera.getRoll());
            moveCameraMethod.invoke(firstpersonCamera,-zoomValue,0.0F,0.0F);
        } else {
            moveCameraMethod.invoke(firstpersonCamera,zoomValue,0.0F,0.0F);
        }
        return firstpersonCamera.getPosition();
    }

    //Renders more offset vectors in-between the endpoint and the lastOffsetVector in a line and mirrors them
    public static void renderOffsets(Vec3 lastOffsetVector , Vec3 lastOffsetVectorMirrored, Vec3 eyePosition, Vec3 endpoint, VertexConsumer vertexBuffer, PoseStack.Pose pose){
        //Last Offset from endpoint
        vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
        vertexBuffer.addVertex(pose, lastOffsetVector.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1,1,1).setColor(255, 0, 0, 255);
        //Mirrored
        vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
        vertexBuffer.addVertex(pose, lastOffsetVectorMirrored.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1,1,1).setColor(255, 0, 0, 255);

        //Puts offsetVectors between the endpoint and the lastOffsetVector at a given segment amount
        Vec3 differenceEndpointLastOffset = endpoint.vectorTo(lastOffsetVector);
        Vec3 differenceEndpointLastOffsetMirrored = endpoint.vectorTo(lastOffsetVectorMirrored);
        int segmentAmount = 3;
        for(int i = 1; i < segmentAmount; i++){
            Vec3 segment = differenceEndpointLastOffset.scale((double) i/segmentAmount);
            Vec3 newOffset = endpoint.add(segment);
            vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
            vertexBuffer.addVertex(pose, newOffset.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1,1,1).setColor(255, 0, 0, 255);
            //Mirror
            Vec3 segmentMirrored = differenceEndpointLastOffsetMirrored.scale((double) i/segmentAmount);
            Vec3 newOffsetMirrored = endpoint.add(segmentMirrored);
            vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
            vertexBuffer.addVertex(pose, newOffsetMirrored.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1,1,1).setColor(255, 0, 0, 255);
        }
    }

    //Raycasts for block collision detection
    public static BlockHitResult getRaycastResultBlock(Vec3 eyePosition, Vec3 endpoint, Entity player){
        ClipContext clipContext = new ClipContext(eyePosition, endpoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        return player.level().clip(clipContext);
    }

    //Checks for block collisions to find the definite endpoint, then checks for an entity hit. If there is a hit, add it to the hurtlist, then call itself recursively with the hit location and the exempted entity
    public static void summonAndProcessRaycasts(Vec3 startVector, Vec3 endpoint, double interactionRange, Entity player, ArrayList<EntityHitResult> hurtList, boolean isDefinitiveEndpoint, ArrayList<Integer> exemptionList){
        Vec3 finalLocation = new Vec3(endpoint.toVector3f());
        //If this is the first raycast, checks for block collisions so that the raycasts don't go through walls by picking an appropriate new endpoint
        if(!isDefinitiveEndpoint) {
            BlockHitResult blockHitResult = getRaycastResultBlock(startVector, endpoint, player);
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                finalLocation = blockHitResult.getLocation();
            }
        }
        //Calculate missing variables for the raycast
        Vec3 calculatedViewVector = finalLocation.add(startVector.scale(-1));
        AABB aabb = player.getBoundingBox().expandTowards(calculatedViewVector).inflate(1.0, 1.0, 1.0);
        //Perform/Summon raycast
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(player, startVector, finalLocation, aabb, (e) -> !e.isSpectator() && e.isPickable() && DataUtils.isNotPet(e, player) && !exemptionList.contains(e.getId()), Mth.square(interactionRange));
        //If we have a hit
        if(entityHitResult != null){
            //Add to the hurtList
            DataUtils.nonDuplicatesAddToList(hurtList, entityHitResult);
            //Add to the exemptionList for recursive raycasts
            exemptionList.add(entityHitResult.getEntity().getId());
            //Get new start location
            Vec3 newStartVector = entityHitResult.getLocation();
            //Recursive call
            summonAndProcessRaycasts(newStartVector, finalLocation, interactionRange, player, hurtList, true, exemptionList);
        } else {
            exemptionList.clear();
        }
    }

    //Same as renderOffsets, but summons raycasts instead
    public static void raycastOffsets(double chargeProgressPercentage, Vec3 lastOffsetVector, Vec3 lastOffsetVectorMirrored, Vec3 eyePosition, Vec3 endpoint, double interactionRange, Entity player, ArrayList<EntityHitResult> arrayList){
        //Last Offset from endpoint
        //Puts offsetVectors between the endpoint and the lastOffsetVector at a given segment amount
        Vec3 differenceEndpointLastOffset = endpoint.vectorTo(lastOffsetVector);
        Vec3 differenceEndpointLastOffsetMirrored = endpoint.vectorTo(lastOffsetVectorMirrored);
        int segmentAmount = 3;
        ArrayList<Integer> exemptionList = new ArrayList<>();

        for(int i = 1; i < segmentAmount; i++){
            if((double) i/segmentAmount <= chargeProgressPercentage) {
                Vec3 segment = differenceEndpointLastOffset.scale((double) i / segmentAmount);
                Vec3 newOffset = endpoint.add(segment);
                summonAndProcessRaycasts(eyePosition, newOffset, interactionRange, player, arrayList, false, exemptionList);
                //Mirrored
                Vec3 segmentMirrored = differenceEndpointLastOffsetMirrored.scale((double) i / segmentAmount);
                Vec3 newOffsetMirrored = endpoint.add(segmentMirrored);
                summonAndProcessRaycasts(eyePosition, newOffsetMirrored, interactionRange, player, arrayList, false, exemptionList);
            }
        }
        if(chargeProgressPercentage == 1){
            summonAndProcessRaycasts(eyePosition, lastOffsetVector, interactionRange, player, arrayList, false, exemptionList);
            summonAndProcessRaycasts(eyePosition, lastOffsetVectorMirrored, interactionRange, player, arrayList, false, exemptionList);
        }
    }

    //Log vector changes
    public static void logVectorChanges(Logger logger, Vec3 susVector){
        String eyeX = new DecimalFormat("##.##").format(susVector.x);
        String eyeY = new DecimalFormat("##.##").format(susVector.y);
        String eyeZ = new DecimalFormat("##.##").format(susVector.z);
        if(!eyeX.equals(String.valueOf(oldVector.x)) || !eyeY.equals(String.valueOf(oldVector.y)) || !eyeZ.equals(String.valueOf(oldVector.z))) {
            logger.info(String.valueOf(susVector));
            oldVector = new Vec3(Double.parseDouble(eyeX), Double.parseDouble(eyeY), Double.parseDouble(eyeZ));
        }
    }
}
