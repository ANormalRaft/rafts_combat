package com.anormalraft.rafts_combat;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ModEvents {
    @SubscribeEvent
    public static void onRegisterGui (RegisterGuiLayersEvent event){
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, ResourceLocation.parse("raftchargemeter"), new ChargeMeterOverlay());
    }
}
