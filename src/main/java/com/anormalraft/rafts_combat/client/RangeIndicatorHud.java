package com.anormalraft.rafts_combat.client;

import com.anormalraft.rafts_combat.config.ClientConfig;
import com.anormalraft.rafts_combat.config.ServerConfig;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@Mod(value = "rafts_combat", dist = Dist.CLIENT)
public class RangeIndicatorHud implements LayeredDraw.Layer {
    //Crosshair overlay
    public void renderCrosshair(int centerX, int centerY, GuiGraphics guiGraphics){
        if(!ClientTasks.entityHitResultList.isEmpty()) {
            int color = 0xFFFC4444;
            guiGraphics.fill(centerX - 1, centerY - 4, centerX, centerY + 5, color);
            guiGraphics.fill(centerX - 5, centerY, centerX + 4, centerY + 1, color);
        }
    }

    //Range indicator progression bars
    public void renderRangeIndicators(int centerX, int centerY, GuiGraphics guiGraphics, Minecraft mc){
        int halfHeight = ClientConfig.BAR_HEIGHT.getAsInt();
        //Fov adjustment
        int fovNumber = mc.options.fov().get();
        //70 is Normal value
        double fovDifference = 70 - fovNumber;
        //These values were found through testing
        double fovMultiplier = 0.04;
        if(fovDifference < 0){
            fovMultiplier = 0.012;
        }
        double fovAdjustmentMult = 1 + ((70 - fovNumber) * fovMultiplier);
        //The 191.5 seems to be the magic number for now
        double horizontalLength = 191.5 * ServerConfig.WIDTH_RATIO.get() * fovAdjustmentMult;

        //Calculate alpha value
        int maxAlpha = ClientConfig.MAX_ALPHA.get();
        int minAlpha = 0;
        int currentAlpha = Mth.floor((maxAlpha * ClientTasks.chargeProgressPercentage) + minAlpha);
        String colorString = "FFFFFF";
        //Turn it red when it detects at least 1 target
        if(!ClientTasks.entityHitResultList.isEmpty()){
            colorString = "FF0000";
        }
        String alphaString = Integer.toHexString(currentAlpha).toUpperCase();
        String finalColorString = alphaString + colorString;
        int colorValue = Integer.parseUnsignedInt(finalColorString, 16);
        //For fullness indicators
        String fullAlphaColorString = "FF" + colorString;
        int fullAlphaColorValue = Integer.parseUnsignedInt(fullAlphaColorString, 16);
        int noColorValue = 0x00000000;

        //Draw range indicator bars (similarly as within GuiGraphics)
        int z = 0;
        float chargeAccurateLength = (float) (horizontalLength * ClientTasks.chargeProgressPercentage);
        float chargeApexLengthLeft = centerX - chargeAccurateLength;
        float chargeApexLengthRight = centerX + chargeAccurateLength;
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexBuffer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        vertexBuffer.addVertex(matrix4f, centerX,centerY + halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, centerX,centerY - halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, chargeApexLengthLeft,centerY - halfHeight, z).setColor(colorValue);
        vertexBuffer.addVertex(matrix4f, chargeApexLengthLeft,centerY + halfHeight, z).setColor(colorValue);
        //Mirror
        vertexBuffer.addVertex(matrix4f, centerX,centerY - halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, centerX,centerY + halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, chargeApexLengthRight,centerY + halfHeight, z).setColor(colorValue);
        vertexBuffer.addVertex(matrix4f, chargeApexLengthRight,centerY - halfHeight, z).setColor(colorValue);

        //Render fullness indicators
        if(ClientTasks.chargeProgressPercentage == 1) {
            int fullnessHOffset = 2;
            int fullnessVOffset = halfHeight + 1;
            vertexBuffer.addVertex(matrix4f, chargeApexLengthLeft + fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullAlphaColorValue);
            vertexBuffer.addVertex(matrix4f, chargeApexLengthLeft + fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullAlphaColorValue);
            vertexBuffer.addVertex(matrix4f, chargeApexLengthLeft - fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullAlphaColorValue);
            vertexBuffer.addVertex(matrix4f, chargeApexLengthLeft - fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullAlphaColorValue);
            //Mirror
            vertexBuffer.addVertex(matrix4f, chargeApexLengthRight - fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullAlphaColorValue);
            vertexBuffer.addVertex(matrix4f, chargeApexLengthRight - fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullAlphaColorValue);
            vertexBuffer.addVertex(matrix4f, chargeApexLengthRight + fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullAlphaColorValue);
            vertexBuffer.addVertex(matrix4f, chargeApexLengthRight + fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullAlphaColorValue);
        }
    }

    //render override (what actually renders the elements)
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        if(mc.options.getCameraType().isFirstPerson()){
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            //When in fullscreen, the crosshair moved by one pixel, so we compensate
            if(mc.options.fullscreen().get()){
                centerY -= 1;
            }
            //Crosshair
            if(ClientConfig.CROSSHAIR_COLOR.get()) {
                renderCrosshair(centerX, centerY, guiGraphics);
            }
            //Attack range
            renderRangeIndicators(centerX, centerY, guiGraphics, mc);
        }
    }
}
