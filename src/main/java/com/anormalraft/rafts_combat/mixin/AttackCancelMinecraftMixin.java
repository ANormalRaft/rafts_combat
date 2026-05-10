package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class AttackCancelMinecraftMixin {

    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void cancelAttack(CallbackInfoReturnable<Boolean> cir){
        if(DataUtils.isHoldingCorrectItem(player)){
            cir.setReturnValue(false);
        }
    }
}
