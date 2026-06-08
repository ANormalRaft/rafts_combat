package com.anormalraft.rafts_combat.client;

import com.anormalraft.rafts_combat.config.ClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod(value = "rafts_combat", dist = Dist.CLIENT)
public class AttackZoneHud implements LayeredDraw.Layer {
    //Crosshair overlay
    public void renderCrosshair(int centerX, int centerY, int color, GuiGraphics guiGraphics){
        if(!ClientTasks.entityHitResultList.isEmpty()) {
            guiGraphics.fill(centerX - 1, centerY - 4, centerX, centerY + 5, color);
            guiGraphics.fill(centerX - 5, centerY, centerX + 4, centerY + 1, color);
        }
    }

    //Attack Zone progression bars


    //render override
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if(mc.options.getCameraType().isFirstPerson()){
            //Green
            int color = 0xFFFC4444;
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int x = screenWidth / 2;
            int y = screenHeight / 2;

            if(ClientConfig.CROSSHAIR_COLOR.get()) {
                renderCrosshair(x, y, color, guiGraphics);
            }
        }
    }
}
