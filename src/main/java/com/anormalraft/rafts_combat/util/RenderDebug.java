package com.anormalraft.rafts_combat.util;

import com.anormalraft.rafts_combat.client.ClientTasks;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.util.OptionalDouble;

import static com.anormalraft.rafts_combat.Rafts_Combat.LOGGER;
import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraft.client.renderer.RenderStateShard.NO_CULL;

//Unused, for debugging purposes
public class RenderDebug {
    //Debug lines but no depth test
    public static RenderType debugLinesNoDepth = RenderType.create("lines_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 1536,RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_DEPTH_WRITE).setCullState(NO_CULL).setDepthTestState(new RenderStateShard.DepthTestStateShard("respectmyalphavalueuprick", GL11.GL_NOTEQUAL)).createCompositeState(false));

    //For debugging only
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

                VertexConsumer vertexBufferQuad = bufferSource.getBuffer(ClientTasks.chargeMeterRenderType);

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
