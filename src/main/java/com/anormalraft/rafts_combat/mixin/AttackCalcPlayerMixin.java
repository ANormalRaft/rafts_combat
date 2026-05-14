package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.config.ServerConfig;
import com.anormalraft.rafts_combat.Rafts_Combat;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class AttackCalcPlayerMixin extends LivingEntity {

    protected AttackCalcPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    //Scales the value for the damage calculation
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 1), name="f")
    public float applyScaling(float value, @Local (ordinal = 0) float f){
        if(DataUtils.isHoldingCorrectItem(this)){
            return f * DataUtils.calculateScaling(Rafts_Combat.serverChargeProgressPercentage);
        }
        return value;
    }

    //Negate the sweeping logic
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 2), name="flag2")
    public boolean negateSweeping(boolean value){
        if(DataUtils.isHoldingCorrectItem(this)) {
            return false;
        }
        return value;
    }

    //Deny crits if one target already received it
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 0), name="flag1")
    public boolean denyFurtherCrits(boolean value){
        if(value){
            if(DataUtils.isHoldingCorrectItem(this)) {
                if(Rafts_Combat.canCrit){
                    Rafts_Combat.canCrit = false;
                    return true;
                }
                return false;
            }
        }
        return value;
    }

    //Negate sprint knockback logic if charge is less than 60% (not here). The true knockback removal is in KnockbackNegateLivingEntityMixin
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 0), name="f4")
    public float kindaNegateKnockback(float value){
        if(DataUtils.isHoldingCorrectItem(this) && Rafts_Combat.serverChargeProgressPercentage < ServerConfig.KNOCKBACK_THRESHOLD.get()) {
            return 0F;
        }
        return value;
    }

    //Used, but not for damage, for other flags such as enabling crits?
    @ModifyVariable(method = "attack", at=@At(value = "STORE", ordinal = 0), name="f2")
    public float setScalingValue(float value){
        if(DataUtils.isHoldingCorrectItem(this)) {
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
