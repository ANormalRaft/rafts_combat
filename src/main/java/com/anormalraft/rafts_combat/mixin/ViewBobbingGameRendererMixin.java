package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.util.RenderDebug;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class ViewBobbingGameRendererMixin {

    @Shadow @Final private static Logger LOGGER;

    //Erases the two method calls which affect the posestack in bobView(), resulting in a camera roll that I can't figure out how to cancel-out in ClientTasks. Only reversing or erasing it with a mixin successfully removed it
    @Redirect(method = "bobView", at=@At(value = "INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"))
    public void cancelBobViewRoll(PoseStack instance, Quaternionf quaternion){

    }

    //Debugging
//    @Inject(method = "bobView", at= @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 0))
//    public void getRollValue(PoseStack poseStack, float partialTicks, CallbackInfo ci, @Local (name="f1") float f1, @Local (name="f2") float f2){
//        RenderDebug.trueF1 = f1;
//        RenderDebug.trueF2 = f2;
////        poseStack.mulPose(Axis.ZP.rotationDegrees((float) ((Mth.sin(f1 * (float)Math.PI) * f2 * -3.0F))));
//    }

    //For debugging purposes
//    @Redirect(method = "bobView", at=@At(value = "INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
//    public void cancelTranslate(PoseStack instance, float x, float y, float z){
//
//    }
}
