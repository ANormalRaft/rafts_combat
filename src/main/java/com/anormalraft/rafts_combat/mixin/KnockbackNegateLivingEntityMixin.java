package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.config.ServerConfig;
import com.anormalraft.rafts_combat.Rafts_Combat;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class KnockbackNegateLivingEntityMixin {

    //This is the one that negates knockback
    @ModifyExpressionValue(method = "hurt", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 7))
    public boolean negateKnockback(boolean original, @Local (argsOnly = true) DamageSource source){
        Entity sourceEntity = source.getEntity();
        if(sourceEntity instanceof Player) {
            if (DataUtils.isHoldingCorrectItem((LivingEntity) sourceEntity)) {
                if (Rafts_Combat.serverChargeProgressPercentage < ServerConfig.KNOCKBACK_THRESHOLD.get()) {
                    return true;
                }
            }
        }
        return original;
    }
}
