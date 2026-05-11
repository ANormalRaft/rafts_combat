package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.client.ClientTasks;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class CancelStartDestroyMultiPlayerGameModeMixin {

    //TODO: Will need a raycast for tools in ClientTasks. canRaftSwing is temporary
    @Inject(method = "startDestroyBlock", at=@At("HEAD"), cancellable = true)
    public void cancelStartDestroy(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir){
        if(ClientTasks.canRaftSwing) {
            cir.setReturnValue(false);
        }
    }
}
