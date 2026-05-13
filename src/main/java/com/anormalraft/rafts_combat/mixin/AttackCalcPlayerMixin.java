package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.Rafts_Combat;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class AttackCalcPlayerMixin {

    //Scales the value for the damage calculation
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 1), name="f")
    public float applyScaling(float value, @Local (ordinal = 0) float f){
        if(DataUtils.isHoldingCorrectItem((Player) (Object) this)){
            return f * DataUtils.calculateScaling(Rafts_Combat.serverChargeProgressPercentage);
        }
        return value;
    }

    //Negate the sweeping logic
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 2), name="flag2")
    public boolean negateKnockback(boolean value){
        if(DataUtils.isHoldingCorrectItem((Player) (Object) this)) {
            return false;
        }
        return value;
    }

    //Negate knockback logic if charge is less than 60% (not here)
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 0), name="f4")
    public float negateKnockback(float value){
        if(DataUtils.isHoldingCorrectItem((Player) (Object) this) && Rafts_Combat.serverChargeProgressPercentage < 0.6) {
            return 0F;
        }
        return value;
    }

    //Used, but not for damage it seems
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 0), name="f2")
    public float setScalingValue(float value){
        if(DataUtils.isHoldingCorrectItem((Player) (Object) this)) {
            return (float) Rafts_Combat.serverChargeProgressPercentage;
        }
        return value;
    }

    //    @Redirect(method = "attack", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeValue(Lnet/minecraft/core/Holder;)D", ordinal = 0))
//    public double setInitialDamage(Player instance, Holder holder){
//        if(DataUtils.isHoldingCorrectItem(instance)){
//            return 1.0F;
//        }
//        return (double) instance.getAttributeValue(Attributes.ATTACK_DAMAGE);
//    }
}
