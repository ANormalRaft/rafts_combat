package com.anormalraft.rafts_combat.networking.HurtPayload;

import com.anormalraft.rafts_combat.Rafts_Combat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class C2SHurtPayloadHandler {
    public static void handleDataOnMain(final HurtPayload data , IPayloadContext context) {
        // Do something with the data, on the main thread
        context.enqueueWork(()-> {
            Player player = context.player();

            //Thank you XFactHD for this idea
            for (Integer id : data.idList()) {
                Entity entity = player.level().getEntity(id);
                if(entity != null) {
                    //TODO: Which one is better? entity.hurt it seems since player.attack is very unpredictable. Will need to put custom damage calculations inDataUtil and use it here for damage calculations. Will also probably need to expand the HurtPayload for the charge data
                    entity.hurt(context.player().damageSources().playerAttack(context.player()), 4);
                    //                    player.attack(entity);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}
