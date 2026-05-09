package com.anormalraft.rafts_combat;

import com.anormalraft.rafts_combat.client.ClientTasks;
import com.anormalraft.rafts_combat.util.VectorUtils;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
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

//                VectorUtils.logVectorChanges(LOGGER, eyePosition);
    //TODO: viewbobbing artifacts? The solution would be to cancel it once an attack is initiated
    //TODO: should this go in a client file?
    @SubscribeEvent
    public void onRenderLevelEvent(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
            ClientTasks.progressivelySummonRaycasts(event);
    }


    //From Toolforme
    // Event is on the NeoForge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        ClientTasks.handleAttack();
    }
}
