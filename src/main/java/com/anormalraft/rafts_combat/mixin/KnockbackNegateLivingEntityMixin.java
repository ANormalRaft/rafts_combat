package com.anormalraft.rafts_combat.mixin;

import com.anormalraft.rafts_combat.config.ServerConfig;
import com.anormalraft.rafts_combat.Rafts_Combat;
import com.anormalraft.rafts_combat.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class KnockbackNegateLivingEntityMixin {

    //This is the one that negates knockback
    @Redirect(method = "hurt", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 7))
    public boolean negateKnockback(DamageSource instance, TagKey<DamageType> damageTypeKey){
        return DataUtils.isHoldingCorrectItem(Minecraft.getInstance().player) && Rafts_Combat.serverChargeProgressPercentage < ServerConfig.KNOCKBACK_THRESHOLD.get();
    }
}
