package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.client.ClientTasks;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class ResetDataInventoryMixin {

    //Resets the data when switching hotbar slots
    @Inject(method = "swapPaint", at=@At("HEAD"))
    public void resetClientData(double direction, CallbackInfo ci){
        ClientTasks.canRaftSwing = false;
        ClientTasks.maxChargeThreshold = -1;
        ClientTasks.currentChargeValue = -1;
        ClientTasks.chargeProgressPercentage = 0;
        ClientTasks.canMineFirstClick = false;
    }
}
