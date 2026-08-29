package com.anormalraft.rafts_combat.client;

import com.anormalraft.rafts_combat.config.ClientConfig;
import com.anormalraft.rafts_combat.config.ServerConfig;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

//Responsible for rendering the progress bars on the client GUI
@Mod(value = "rafts_combat", dist = Dist.CLIENT)
public class RangeIndicatorHud implements LayeredDraw.Layer {
    //Crosshair overlay
    public void renderCrosshair(int centerX, int centerY, GuiGraphics guiGraphics){
        if(!ClientTasks.entityHitResultList.isEmpty()) {
            int color = DataUtils.processAlpha(255, ClientConfig.MOB_IN_RANGE_COLOR.get());
            guiGraphics.fill(centerX - 1, centerY - 4, centerX, centerY + 5, color);
            guiGraphics.fill(centerX - 5, centerY, centerX + 4, centerY + 1, color);
        }
    }

    //Range indicator progression bars
    public void renderRangeIndicators(int centerX, int centerY, GuiGraphics guiGraphics, Minecraft mc){
        //Fov adjustment
        int fovNumber = mc.options.fov().get();
        //70 is Normal value
        double fovDifference = 70 - fovNumber;
        //These values were found through testing
        double fovMultiplier = 0.04;
        if (fovDifference < 0) {
            fovMultiplier = 0.012;
        }
        double fovAdjustmentMult = 1 + ((70 - fovNumber) * fovMultiplier);
        double widthRatio = DataUtils.getCorrectWidthRatio(ClientTasks.customWidthHashMap, mc.player);
        //The 191.5 seems to be a magic number for now, 0.33 as well
        double horizontalLength = 191.5 * widthRatio * 0.33 * fovAdjustmentMult;

        //Calculate alpha value
        int maxAlpha = ClientConfig.MAX_ALPHA.get();
        int currentAlpha = Mth.floor(maxAlpha * ClientTasks.chargeProgressPercentage);
        //Color
        int noColorValue = 0x00000000;
        //White
        String colorString = ClientConfig.NONE_IN_RANGE_COLOR.get();
        //Turn it Red when it detects at least 1 target
        if (!ClientTasks.entityHitResultList.isEmpty()) {
            colorString = ClientConfig.MOB_IN_RANGE_COLOR.get();
        }
        int colorValue = DataUtils.processAlpha(currentAlpha, colorString);

        //Draw range indicator bars (similarly as within GuiGraphics)
        int halfHeight = ClientConfig.BAR_HEIGHT.getAsInt();
        int z = 0;
        float chargeAccurateLength = (float) (horizontalLength * ClientTasks.chargeProgressPercentage);
        float chargeCurrentApexLengthLeft = centerX - chargeAccurateLength;
        float chargeCurrentApexLengthRight = centerX + chargeAccurateLength;
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexBuffer = guiGraphics.bufferSource().getBuffer(RenderType.gui());
        vertexBuffer.addVertex(matrix4f, centerX, centerY + halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, centerX, centerY - halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, chargeCurrentApexLengthLeft, centerY - halfHeight, z).setColor(colorValue);
        vertexBuffer.addVertex(matrix4f, chargeCurrentApexLengthLeft, centerY + halfHeight, z).setColor(colorValue);
        //Mirror
        vertexBuffer.addVertex(matrix4f, centerX, centerY - halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, centerX, centerY + halfHeight, z).setColor(noColorValue);
        vertexBuffer.addVertex(matrix4f, chargeCurrentApexLengthRight, centerY + halfHeight, z).setColor(colorValue);
        vertexBuffer.addVertex(matrix4f, chargeCurrentApexLengthRight, centerY - halfHeight, z).setColor(colorValue);

        //FULLNESS INDICATORS
        //Custom alpha logic
        int fullnessIndicatorsMinAlpha = ClientConfig.MIN_FULLNESS_INDICATORS_ALPHA.getAsInt();
        //Redundant, but needed for understanding purposes
        int fullnessIndicatorsColorValue = colorValue;
        double currentAlphaRatio = (double) currentAlpha/maxAlpha;
        if (currentAlphaRatio < (double) fullnessIndicatorsMinAlpha/255) {
            //Slower
            fullnessIndicatorsColorValue = DataUtils.processAlpha(fullnessIndicatorsMinAlpha, colorString);
        } else {
            //Faster
            int fullnessIndicatorsFasterAlpha = (int) (255 * currentAlphaRatio);
            fullnessIndicatorsColorValue = DataUtils.processAlpha(fullnessIndicatorsFasterAlpha, colorString);
        }

        //Rendering
        float chargeAbsoluteApexLengthLeft = (float) (centerX - horizontalLength);
        float chargeAbsoluteApexLengthRight = (float) (centerX + horizontalLength);
        int fullnessHOffset = 2;
        int fullnessVOffset = halfHeight + 1;
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthLeft + fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthLeft + fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthLeft - fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthLeft - fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        //Mirror
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthRight - fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthRight - fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthRight + fullnessHOffset, centerY + fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
        vertexBuffer.addVertex(matrix4f, chargeAbsoluteApexLengthRight + fullnessHOffset, centerY - fullnessVOffset, z).setColor(fullnessIndicatorsColorValue);
    }

    //render override (what actually renders the elements)
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.options.hideGui) return;
        if(mc.options.getCameraType().isFirstPerson() && DataUtils.isHoldingCorrectItem(mc.player)){
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
