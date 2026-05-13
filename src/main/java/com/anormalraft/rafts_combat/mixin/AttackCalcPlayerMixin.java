package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.Rafts_Combat;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class AttackCalcPlayerMixin {

    //Scales the value for the damage calculation
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 1), name="f")
    public float applyScaling(float value, @Local (ordinal = 0) float f){
        return f * DataUtils.calculateScaling(Rafts_Combat.serverChargeProgressPercentage);
    }

    //Used, but not for damage it seems
    @Redirect(method = "attack", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttackStrengthScale(F)F", ordinal = 0))
    public float setScalingValue(Player instance, float adjustTicks){
        return (float) Rafts_Combat.serverChargeProgressPercentage;
    }

    //    @Redirect(method = "attack", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D", ordinal = 0))
//    public double setInitialDamage(Player instance, Holder holder){
//        if(DataUtils.isHoldingCorrectItem(instance)){
//            return 1.0F;
//        }
//        return (double) instance.getAttributeValue(Attributes.ATTACK_DAMAGE);
//    }
}
