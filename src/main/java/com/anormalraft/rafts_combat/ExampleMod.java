package com.anormalraft.rafts_combat;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.joml.Vector3f;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.util.Mth.sin;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ExampleMod.MODID)
public class ExampleMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rafts_combat";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ExampleMod(IEventBus modEventBus, ModContainer modContainer) {
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

        //Particles (jank, replace this with a line)
        Minecraft.getInstance().level.addParticle(ParticleTypes.GUST, eyePosition.x + 2, eyePosition.y, eyePosition.z, 1, 1, 1);


        if(entityHitResult != null) {
            LOGGER.debug(entityHitResult.toString());
        }
    }

    @SubscribeEvent
    public void onRenderLevelEvent(RenderLevelStageEvent event){
        if(event.getCamera().getEntity() instanceof Player player){
            if(player.isShiftKeyDown()) {
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                double interactionRange = player.entityInteractionRange();
                Vec3 eyePosition = player.getEyePosition(partialTick);
                Vec3 viewVector = player.getViewVector(partialTick);
                Vec3 scaledViewVector = viewVector.scale(interactionRange);
                Vec3 endpoint = eyePosition.add(scaledViewVector);

//                Vec3 pointOnPlane = Minecraft.getInstance().gameRenderer.getMainCamera().getNearPlane().getPointOnPlane(1F,0.5F);
//                Vec3 scaledPOP = pointOnPlane.scale(3);

//                Vec3 orthogonal = endpoint.cross(player.getLookAngle());
//                Vec3 modified = endpoint.add(orthogonal);

                //posx = -90
                //neg z = -180
                float rotationAngle = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot() % 360;
                if(rotationAngle < 0){
                    rotationAngle = 360 + rotationAngle;
                }
                double angleValue = ((rotationAngle)/360) * (2*Mth.PI);
                double sinValue = Mth.sin((float) angleValue);
                double cosValue = Mth.cos((float) angleValue);
                if(sinValue >= Mth.PI){
                    sinValue = -sinValue;
                }
                if(cosValue >= Mth.PI){
                    cosValue = -cosValue;
                }
                Vec3 modified = endpoint.add(cosValue * 0.5,0, sinValue * 0.5);


                //PoseStack
                PoseStack poseStack = event.getPoseStack();
                poseStack.pushPose();
                //Thank you TopSnek & Zergatul from the Forge Forums <3
                Vec3 view = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                poseStack.translate(-view.x, -view.y, -view.z);

//                LOGGER.info(String.valueOf(rotationAngle));

                PoseStack.Pose pose = poseStack.last();
                //Line (look at FishingHookRenderer or EntityRenderDispatcher)
                MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer vertexBuffer = bufferSource.getBuffer(RenderType.lines());
                //Vertices
                vertexBuffer.addVertex(pose, eyePosition.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1, 1, 1).setColor(255, 0, 0, 255);
                vertexBuffer.addVertex(pose, modified.toVector3f()).setUv(0, 0).setUv2(0, 0).setNormal(1,1,1).setColor(255, 0, 0, 255);
                poseStack.popPose();
            }
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = ExampleMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
