package com.anormalraft.rafts_combat.networking.HurtPayload;

import com.anormalraft.rafts_combat.Rafts_Combat;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class C2SHurtPayloadHandler {
    public static void handleDataOnMain(final HurtPayload data , IPayloadContext context) {
        // Do something with the data, on the main thread
        context.enqueueWork(()-> {
            //Thank you XFactHD for this idea
            for (Integer id : data.idList()) {
                Entity entity = context.player().level().getEntity(id);
                if(entity != null) {
                    context.player().attack(entity);
//                    entity.hurt(context.player().damageSources().playerAttack(context.player()), context.player().getMainHandItem().getDamageValue());
                }
            }
            //Clear serverside list (nonono)
//            Rafts_Combat.entityHitResultListServer.clear();
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}
