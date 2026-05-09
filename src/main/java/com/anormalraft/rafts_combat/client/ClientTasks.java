package com.anormalraft.rafts_combat.client;

import com.anormalraft.rafts_combat.util.VectorUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

import static com.anormalraft.rafts_combat.Rafts_Combat.LOGGER;
import static net.minecraft.client.renderer.RenderStateShard.*;

//@Mod(value = "rafts_combat", dist = Dist.CLIENT)
public class ClientTasks {
    //further debug lines
    public static RenderType debugLinesNoDepth = RenderType.create("lines_no_depth",DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 1536,RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).setDepthTestState(new RenderStateShard.DepthTestStateShard("respectmyalphavalueuprick", GL11.GL_NOTEQUAL)).createCompositeState(false));
    //To render my quad correctly
    public static RenderType chargeMeterRenderType = RenderType.create("charge_meter", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(new RenderStateShard.DepthTestStateShard("respectmyalphavalueuprick", GL11.GL_NOTEQUAL)).createCompositeState(false));
    //Detects click behavior
    public static boolean canRaftSwing = false;
    //Item data
    public static int maxChargeThreshold = -1;
    public static int currentChargeValue = -1;

    //Key input logic
    public static void handleAttack(){
        //Holding down the key
        if(Minecraft.getInstance().options.keyAttack.isDown()) {
            if(ClientTasks.canRaftSwing){
                //Start Charging
                if(currentChargeValue < maxChargeThreshold) {
                    currentChargeValue += 1;
                }
                //progressivelySummonRaycasts takes care of the raycast and rendering logic. Check if they work on the server too
            } else {
                //Enable the swing
                Player player = Minecraft.getInstance().player;
                //If the mainhanditem item is a tool...
                if (player != null) {
                    ItemStack itemStack = player.getMainHandItem();
                    if (!itemStack.isEmpty()) {
                        if (itemStack.getComponents().has(DataComponents.TOOL)){
                            //Get weapon data here
                             Optional<ItemAttributeModifiers.Entry> use_coolown = itemStack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().filter(attributeEntry -> attributeEntry.modifier().is(ResourceLocation.parse("minecraft:base_attack_speed"))).findFirst();
                            double actualAttackSpeed = use_coolown.get().attribute().value().getDefaultValue() + use_coolown.get().modifier().amount();
                             maxChargeThreshold = Mth.floor(20.0 / actualAttackSpeed);
                             currentChargeValue = 0;
                            //...Flip the swing boolean
                            ClientTasks.canRaftSwing = true;
                        }
                    }
                }
            }
            //Release the key
        } else if (ClientTasks.canRaftSwing){
            ClientTasks.canRaftSwing = false;
            //Attack (separate function surely). Should also trigger on hotbar slot switch somehow

            //Swing (animation)
            //Custom Damage calc

            //Reset data
            maxChargeThreshold = -1;
            currentChargeValue = -1;
        }
    }

    public static void progressivelySummonRaycasts(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //Needed or else we draw on all stages and some render weirdly
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (event.getCamera().getEntity() instanceof Player player) {
                if(maxChargeThreshold < 0 || currentChargeValue < 0){
                    return;
                }
                double chargeProgressPercentage = (double) currentChargeValue /maxChargeThreshold;
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                double interactionRange = player.entityInteractionRange();
                Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
                Vec3 mainCameraPosition = mainCamera.getPosition();
                Vec3 eyePosition = new Vec3(mainCameraPosition.x, mainCameraPosition.y, mainCameraPosition.z);
                Vec3 viewVector = player.getViewVector(partialTick);
                Vec3 scaledViewVector = viewVector.scale(interactionRange);

                //Get the first person camera position when in third person(s)
                boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
                if (!isFirstPerson) {
                    eyePosition = VectorUtils.getFirstPersonCameraPosition(mainCamera);
                }

                //Player raycast (endpoint) position
                Vec3 endpoint = eyePosition.add(scaledViewVector);
                //Raycast
                EntityHitResult endpointRaycastResult = VectorUtils.getRaycastResult(eyePosition, endpoint, interactionRange, player);

                //Offset vectors
                double offsetXZ = -0.5;
                double offsetY = 0.0;
                Vec3 lastOffsetVector = VectorUtils.calculateOffsetVector(offsetXZ, offsetY, endpoint);
                Vec3 lastOffsetVectorMirrored = VectorUtils.calculateOffsetVector(-offsetXZ, offsetY, endpoint);
                //Summons all remaining offsets & get their results
                List<EntityHitResult> arrayEntityHitResult = VectorUtils.raycastOffsets(chargeProgressPercentage, lastOffsetVector, lastOffsetVectorMirrored, eyePosition, endpoint, interactionRange, player);
                arrayEntityHitResult.add(endpointRaycastResult);
                //Remove all nulls
                arrayEntityHitResult.removeIf(Objects::isNull);

                //Quad rendering representing range
                //PoseStack stuff
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();
                //Thank you TopSnek & Zergatul from the Forge Forums <3
                poseStack.translate(-mainCameraPosition.x, -mainCameraPosition.y, -mainCameraPosition.z);
                PoseStack.Pose pose = poseStack.last();
                //Buffer stuff
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer vertexBufferQuad = bufferSource.getBuffer(chargeMeterRenderType);

                //Calculate the "always left" vector
                Vec3 leftOrthogonalViewVector = VectorUtils.calculateOffsetVector(Mth.PI, 0, viewVector).normalize();
                Vec3 correctHeightDirection = viewVector.cross(leftOrthogonalViewVector).scale(0.05);

                //Calculate reveal position
                Vec3 voidedChargeAccurateOffsetVector = endpoint.vectorTo(lastOffsetVector).scale(chargeProgressPercentage);
                Vec3 chargeAccurateOffsetVector = endpoint.add(voidedChargeAccurateOffsetVector);
                Vec3 voidedChargeAccurateOffsetVectorMirrored = endpoint.vectorTo(lastOffsetVectorMirrored).scale(chargeProgressPercentage);
                Vec3 chargeAccurateOffsetVectorMirrored = endpoint.add(voidedChargeAccurateOffsetVectorMirrored);
                //Calculate alpha value
                int maxAlpha = 100;
                int minAlpha = 0;
                int currentAlpha = Mth.floor((maxAlpha * chargeProgressPercentage) + minAlpha);
                //Turn it red when it detects at least 1 target
                int colorValue = 255;
                if(!arrayEntityHitResult.isEmpty()){
                    colorValue = 0;
                }

                //Add the vertices to the world
                vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
                vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
                vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVector.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);
                vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVector.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);
                //Mirror it (HOLY FUCK WHY WAS THIS SO HARD TO FIGURE OUT)
                vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
                vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
                vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVectorMirrored.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);
                vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVectorMirrored.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);

                poseStack.popPose();
            }
        }
    }

    //For debugging
    public static void debugRender(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //Needed or else we draw on all stages and some render weirdly
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (event.getCamera().getEntity() instanceof Player player) {
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                double interactionRange = player.entityInteractionRange();
                Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
                Vec3 mainCameraPosition = mainCamera.getPosition();
                Vec3 eyePosition = new Vec3(mainCameraPosition.x, mainCameraPosition.y, mainCameraPosition.z);
                Vec3 viewVector = player.getViewVector(partialTick);
                Vec3 scaledViewVector = viewVector.scale(interactionRange);

                //Get the first person camera position when in third person(s)
                boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
                if (!isFirstPerson) {
                    eyePosition = VectorUtils.getFirstPersonCameraPosition(mainCamera);
                }

                //Player raycast (endpoint) position
                Vec3 endpoint = eyePosition.add(scaledViewVector);

                //Raycast test
                if (player.isShiftKeyDown()) {
                    Vec3 calculatedViewVector = endpoint.add(eyePosition.scale(-1));
                    AABB aabb = player.getBoundingBox().expandTowards(calculatedViewVector).inflate(1.0, 1.0, 1.0);
                    EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(player, eyePosition, endpoint, aabb, (e) -> !e.isSpectator() && e.isPickable(), Mth.square(interactionRange));
                    if (entityHitResult != null) {
                        LOGGER.debug(entityHitResult.toString());
                    }
                }

                //PoseStack stuff
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();
                //Thank you TopSnek & Zergatul from the Forge Forums <3
                poseStack.translate(-mainCameraPosition.x, -mainCameraPosition.y, -mainCameraPosition.z);
                PoseStack.Pose pose = poseStack.last();

                //Line stuff (look at FishingHookRenderer or EntityRenderDispatcher). Don't use Tesselator as that is only for GUI
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer vertexBuffer = bufferSource.getBuffer(debugLinesNoDepth);
                //Raycast Vertices
                vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
                vertexBuffer.addVertex(pose, endpoint.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);

                //Offset vectors
                double offsetXZ = -0.5;
                double offsetY = 0.5;
                Vec3 lastOffsetVector = VectorUtils.calculateOffsetVector(offsetXZ, offsetY, endpoint);
                Vec3 lastOffsetVectorMirrored = VectorUtils.calculateOffsetVector(-offsetXZ, offsetY,endpoint);
                VectorUtils.renderOffsets(lastOffsetVector, lastOffsetVectorMirrored, eyePosition, endpoint , vertexBuffer, pose);
//                bufferSource.endBatch(RenderType.lines());

                //Render the visible quad representing range
                //Calculate the "always left" vector
                Vec3 leftOrthogonalViewVector = VectorUtils.calculateOffsetVector(Mth.PI, 0, viewVector).normalize();
                Vec3 correctHeightDirection = viewVector.cross(leftOrthogonalViewVector).scale(0.05);

                VertexConsumer vertexBufferQuad = bufferSource.getBuffer(chargeMeterRenderType);

                vertexBufferQuad.addVertex(pose, lastOffsetVector.add(correctHeightDirection).toVector3f()).setColor(255, 255, 255, 100);
                vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection).toVector3f()).setColor(255, 255, 255, 0);
                vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, 255, 255, 0);
                vertexBufferQuad.addVertex(pose, lastOffsetVector.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, 255, 255, 100);

//                bufferSource.endBatch(chargeMeterRenderType);
                poseStack.popPose();
            }
        }
    }
}
