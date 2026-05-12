package com.anormalraft.rafts_combat;

import com.anormalraft.rafts_combat.client.ClientTasks;
import com.anormalraft.rafts_combat.networking.PayloadHousekeeping;
import com.anormalraft.rafts_combat.util.DataUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import java.lang.reflect.InvocationTargetException;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rafts_Combat.MODID)
public class Rafts_Combat {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rafts_combat";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Rafts_Combat(IEventBus modEventBus, ModContainer modContainer) {
        //Register Networking Payloads
        modEventBus.addListener(PayloadHousekeeping::registerPayload);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    //Sync config if needed in future and also init the hashmap in DataUtils
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        DataUtils.itemTagsBlockTagsHashMap.put(ItemTags.AXES, BlockTags.MINEABLE_WITH_AXE);
        DataUtils.itemTagsBlockTagsHashMap.put(ItemTags.PICKAXES, BlockTags.MINEABLE_WITH_PICKAXE);
        DataUtils.itemTagsBlockTagsHashMap.put(ItemTags.SHOVELS, BlockTags.MINEABLE_WITH_SHOVEL);
        DataUtils.itemTagsBlockTagsHashMap.put(ItemTags.HOES, BlockTags.MINEABLE_WITH_HOE);
        DataUtils.itemTagsBlockTagsHashMap.put(ItemTags.SWORDS, BlockTags.SWORD_EFFICIENT);
    }

    //TODO: viewbobbing artifacts? The solution would be to cancel it once an attack is initiated
    @SubscribeEvent
    public void onRenderLevelEvent(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
            ClientTasks.progressivelySummonRaycasts(event);
    }

    // Event is on the NeoForge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        ClientTasks.handleAttack();
    }

    //Cancel right click item interactions when an attack is queued
    @SubscribeEvent
    public void onPlayerInteractRightClick(PlayerInteractEvent.RightClickItem event){
        if(ClientTasks.canRaftSwing){
            event.setCanceled(true);
        }
    }
}
