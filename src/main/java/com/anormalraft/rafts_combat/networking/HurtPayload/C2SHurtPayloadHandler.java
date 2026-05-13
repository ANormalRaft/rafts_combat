package com.anormalraft.rafts_combat.networking.HurtPayload;

import com.anormalraft.rafts_combat.Rafts_Combat;
import com.anormalraft.rafts_combat.client.ClientTasks;
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

import static com.anormalraft.rafts_combat.Rafts_Combat.LOGGER;

public class C2SHurtPayloadHandler {
    public static void handleDataOnMain(final HurtPayload data , IPayloadContext context) {
        // Do something with the data, on the main thread
        context.enqueueWork(()-> {
            Player player = context.player();
            //Set value on serverside
            Rafts_Combat.serverChargeProgressPercentage = data.chargeProgressPercentage();
            //Set crit possibility to true; If one is found in AttackCalcPlayerMixin, then it will be flipped and further crits will be denied
            Rafts_Combat.canCrit = true;
            //Thank you XFactHD for this idea
            for (Integer id : data.idList()) {
                Entity entity = player.level().getEntity(id);
                if(entity != null) {
                    player.attack(entity);
                    //Unused but kept for reference
                    //entity.hurt(context.player().damageSources().playerAttack(context.player()), 4);
                }
            }
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}
