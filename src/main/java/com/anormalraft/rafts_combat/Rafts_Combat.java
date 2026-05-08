package com.anormalraft.rafts_combat;

import com.anormalraft.rafts_combat.mixin.CameraMixin;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.InvocationTargetException;

import static net.minecraft.client.renderer.RenderStateShard.*;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rafts_Combat.MODID)
public class Rafts_Combat {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rafts_combat";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static RenderType chargeMeterRenderType = RenderType.create("charge_meter", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(new RenderStateShard.DepthTestStateShard("respectmyalphavalueuprick", GL11.GL_NOTEQUAL)).createCompositeState(false));

    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Rafts_Combat(IEventBus modEventBus, ModContainer modContainer) {
//        modEventBus.addListener(ModEvents::onRegisterGui);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    @SubscribeEvent
    public void OnRightClickEvent(PlayerInteractEvent.RightClickItem event){
        Player player = event.getEntity();
        double interactionRange = player.entityInteractionRange();
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1);
        Vec3 scaledViewVector = viewVector.scale(interactionRange);
        Vec3 endpoint = eyePosition.add(scaledViewVector);
        //From GameRenderer.pick
        AABB aabb = player.getBoundingBox().expandTowards(scaledViewVector).inflate(1.0, 1.0, 1.0);


        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(player, eyePosition,endpoint, aabb, (e) -> !e.isSpectator() && e.isPickable(), Mth.square(interactionRange));

        //Line here?

        if(entityHitResult != null) {
            LOGGER.debug(entityHitResult.toString());
        }
    }

//                VectorUtils.logVectorChanges(LOGGER, eyePosition);
    //TODO: viewbobbing artifacts? I removed it for now
    //TODO: FOV changes (sprinting, speed potions)
    //TODO: should this go in a client file?
    @SubscribeEvent
    public void onRenderLevelEvent(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if(event.getCamera().getEntity() instanceof Player player){
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            double interactionRange = player.entityInteractionRange();
            Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 mainCameraPosition = mainCamera.getPosition();
            Vec3 eyePosition = new Vec3(mainCameraPosition.x, mainCameraPosition.y, mainCameraPosition.z);
            Vec3 viewVector = player.getViewVector(partialTick);
            Vec3 scaledViewVector = viewVector.scale(interactionRange);

            //Get the first person camera position when in third person(s)
            boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
            if(!isFirstPerson) {
                eyePosition = VectorUtils.getFirstPersonCameraPosition(mainCamera);
            }
            //Player raycast (endpoint) position
            Vec3 endpoint = eyePosition.add(scaledViewVector);

            //PoseStack stuff
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            //Thank you TopSnek & Zergatul from the Forge Forums <3
            poseStack.translate(-mainCameraPosition.x, -mainCameraPosition.y, -mainCameraPosition.z);
            PoseStack.Pose pose = poseStack.last();

            //Line stuff (look at FishingHookRenderer or EntityRenderDispatcher)
            //Don't use Tesselator as that is only for GUI
            MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//            VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.lines());
            //Raycast Vertices
//            vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
//            vertexBuffer.addVertex(pose, endpoint.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);

            //Offset vectors
            double offsetXZ = -0.5;
            double offsetY = 0.5;
            Vec3 lastOffsetVector = VectorUtils.calculateOffsetVector(offsetXZ, offsetY, endpoint);
//            VectorUtils.renderOffsets(offsetXZ, offsetY, lastOffsetVector, endpoint, eyePosition, vertexBuffer, pose);

            //Render the visible quad representing range
            //Calculate the "always left" vector
            Vec3 leftOrthogonalViewVector = VectorUtils.calculateOffsetVector(Mth.PI,0, viewVector).normalize();
            Vec3 correctHeightDirection = viewVector.cross(leftOrthogonalViewVector).scale(0.05);
            //TODO?: fix artifact(s) (not the end of the world), happens when I .getBuffer() a second time on the buffersource...
            VertexConsumer vertexBufferQuad = bufferSource.getBuffer(chargeMeterRenderType);

            vertexBufferQuad.addVertex(pose, lastOffsetVector.add(correctHeightDirection).toVector3f()).setColor(255, 255, 255, 255);
            vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection).toVector3f()).setColor(255, 255, 255, 0);
            vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, 255, 255, 0);
            vertexBufferQuad.addVertex(pose, lastOffsetVector.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, 255, 255, 255);

            //Turn it red when it detects at least 1 target

            poseStack.popPose();
        }
    }

    //Gui management?


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = Rafts_Combat.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
