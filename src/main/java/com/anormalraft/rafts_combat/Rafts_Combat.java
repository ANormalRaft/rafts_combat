package com.anormalraft.rafts_combat;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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

import java.text.DecimalFormat;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rafts_Combat.MODID)
public class Rafts_Combat {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rafts_combat";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Rafts_Combat(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ModEvents::onRegisterGui);
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

    //Test vars
    double oldV = 0;
    Vec3 oldVector = new Vec3(0,0,0);

    //Helper method to log vector changes
    public void lofVectorChanges(Logger logger, Vec3 susVector){
        String eyeX = new DecimalFormat("##.##").format(susVector.x);
        String eyeY = new DecimalFormat("##.##").format(susVector.y);
        String eyeZ = new DecimalFormat("##.##").format(susVector.z);
        if(!eyeX.equals(String.valueOf(oldVector.x)) || !eyeY.equals(String.valueOf(oldVector.y)) || !eyeZ.equals(String.valueOf(oldVector.z))) {
            logger.info(String.valueOf(susVector));
            oldVector = new Vec3(Double.parseDouble(eyeX), Double.parseDouble(eyeY), Double.parseDouble(eyeZ));
        }
    }
    
    //TODO: should this go in a client file?
    //TODO viewbobbing artifacts? I removed it for now
    //TODO?: eyePosition doesn't correctly follow the player's y coordinate upon a pose change (crouching, swimming), so the lines render weirdly in first person. Hopefully I can translate the endpoint/offset to draw something on the screen instead of trying to do the impossible
    @SubscribeEvent
    public void onRenderLevelEvent(RenderLevelStageEvent event){
        if(event.getCamera().getEntity() instanceof Player player){
//            if(player.isShiftKeyDown()) {
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                double interactionRange = player.entityInteractionRange();
                //Player raycast (endpoint)
                Vec3 eyePosition = player.getEyePosition(partialTick);
                Vec3 viewVector = player.getViewVector(partialTick);
                Vec3 scaledViewVector = viewVector.scale(interactionRange);
                Vec3 endpoint = eyePosition.add(scaledViewVector);

                //Calculates an offsetVector of the endpoint
                double offsetXZ = -0.5;
                float rotationAngleY = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot() % 360;
                float rotationAngleXZ = Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() % 360;
                if(rotationAngleY < 0){
                    rotationAngleY = 360 + rotationAngleY;
                }
                if(rotationAngleXZ < 0){
                    rotationAngleXZ = 360 + rotationAngleXZ;
                }

                //XY offset data
                double angleValueRotY = ((rotationAngleY)/360) * (2*Mth.PI);
                double sinValueRotY = Mth.sin((float) angleValueRotY);
                double cosValueRotY = Mth.cos((float) angleValueRotY);
                if(sinValueRotY >= Mth.PI){
                    sinValueRotY = -sinValueRotY;
                }
                if(cosValueRotY >= Mth.PI){
                    cosValueRotY = -cosValueRotY;
                }
                
                //Y offset data
                double offsetY = 0.5;
                double angleValueRotXZ = ((rotationAngleXZ)/360) * (2*Mth.PI);
                double sinValueRotXZ = Mth.sin((float) angleValueRotXZ);
                if(sinValueRotY >= Mth.PI){
                    sinValueRotXZ = -sinValueRotXZ;
                }

                //Result
                Vec3 offsetVector = endpoint.add( (sinValueRotXZ * -sinValueRotY * offsetY) + (cosValueRotY * offsetXZ), offsetY,  (sinValueRotXZ * cosValueRotY * offsetY) + (sinValueRotY * offsetXZ));

                //PoseStack
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();
                //Thank you TopSnek & Zergatul from the Forge Forums <3
                Vec3 mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                poseStack.translate(-mainCamera.x, -mainCamera.y, -mainCamera.z);

//                if(interactionRange != oldV) {
//                    LOGGER.info(String.valueOf(interactionRange));
//                    oldV = interactionRange;
//                }
                //-7.125762111734555, -58.384265780448914, 3.5958112686656225 cam
                //-7.125762111734555, -58.37999999523163, 3.5958112686656225 eyepos
//                lofVectorChanges(LOGGER, mainCamera);

                PoseStack.Pose pose = poseStack.last();
                //Line (look at FishingHookRenderer or EntityRenderDispatcher)
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.lines());
                //Vertices
                vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
                vertexBuffer.addVertex(pose, endpoint.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1,1,1).setColor(255, 0, 0, 255);
                poseStack.popPose();
//            }
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
