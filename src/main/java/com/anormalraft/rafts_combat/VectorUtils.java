package com.anormalraft.rafts_combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

public class VectorUtils {
    //Gets the last value from getMaxZoom
    public static float lastMaxZoom = 0.0F;
    //Log helper for vectors
    static Vec3 oldVector = new Vec3(0,0,0);

    //Calculates an offsetVector of the endpoint (+x means left, -x means right of crosshair)
    public static Vec3 calculateOffsetVector(double offsetXZ, double offsetY, Vec3 endpoint){
        //Calculates an offsetVector of the endpoint
        float rotationAngleY = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot() % 360;
        float rotationAngleXZ = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() % 360;
        if(rotationAngleY < 0){
            rotationAngleY = 360 + rotationAngleY;
        }
        if(rotationAngleXZ < 0){
            rotationAngleXZ = 360 + rotationAngleXZ;
        }

        //XY offset data
        double angleValueRotY = ((rotationAngleY)/360) * (2* Mth.PI);
        double sinValueRotY = Mth.sin((float) angleValueRotY);
        double cosValueRotY = Mth.cos((float) angleValueRotY);
        if(sinValueRotY >= Mth.PI){
            sinValueRotY = -sinValueRotY;
        }
        if(cosValueRotY >= Mth.PI){
            cosValueRotY = -cosValueRotY;
        }

        //Y offset data
        double angleValueRotXZ = ((rotationAngleXZ)/360) * (2*Mth.PI);
        double sinValueRotXZ = Mth.sin((float) angleValueRotXZ);
        double cosValueRotXZ = Mth.cos((float) angleValueRotXZ);
        if(sinValueRotY >= Mth.PI){
            sinValueRotXZ = -sinValueRotXZ;
        }

        //Result
        return endpoint.add((sinValueRotXZ * -sinValueRotY * offsetY) + (cosValueRotY * offsetXZ), offsetY * cosValueRotXZ,  (sinValueRotXZ * cosValueRotY * offsetY) + (sinValueRotY * offsetXZ));
    }

    //Gets the first person camera's position even if in third person
    public static Vec3 getFirstPersonCameraPosition(Camera mainCamera) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        boolean isMirroredThirdPerson = Minecraft.getInstance().options.getCameraType().isMirrored();
        Camera firstpersonCamera = new Camera();
        Field cameraForwards = firstpersonCamera.getClass().getDeclaredField("forwards");
        cameraForwards.setAccessible(true);
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
    public static void renderOffsets(double offsetXZ, double offsetY, Vec3 lastOffsetVector , Vec3 endpoint, Vec3 eyePosition, VertexConsumer vertexBuffer, PoseStack.Pose pose){
        //Last Offset from endpoint
        Vec3 lastOffsetVectorMirrored = calculateOffsetVector(-offsetXZ, offsetY,endpoint);
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
    //Quad version of the lines in case of quad-unification
//            VertexConsumer vertexBufferQuad = bufferSource.getBuffer(RenderType.debugQuads());
//            double quadLineThickness = -0.005;
//            vertexBufferQuad.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
//            vertexBufferQuad.addVertex(pose, calculateOffsetVector(0, quadLineThickness, endpoint).toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
//            vertexBufferQuad.addVertex(pose, endpoint.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
//            vertexBufferQuad.addVertex(pose, calculateOffsetVector(0, quadLineThickness, eyePosition).toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
}
