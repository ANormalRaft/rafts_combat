package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.util.VectorUtils;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "getMaxZoom", at=@At("TAIL"))
    public void getLastMaxZoom(float maxZoom, CallbackInfoReturnable<Float> cir){
        VectorUtils.lastMaxZoom = cir.getReturnValue();
    }
}
