package com.anormalraft.rafts_combat.networking.CustomWidthArrayPayload;

import com.anormalraft.rafts_combat.client.ClientTasks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CCustomWidthArrayPayloadHandler {
    public static void handleDataOnMain(final CustomWidthArrayPayload data, IPayloadContext context){
        context.enqueueWork(() -> {
            Item[] itemArray = new Item[data.itemStackList().size()];
            for(int i=0; i < itemArray.length; i++){
                itemArray[i] = data.itemStackList().get(i).getItem();
            }
            ClientTasks.customWidthHashMap.put(data.value(), itemArray);
        })
        .exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.literal( e.getMessage()));
            return null;
        });
    }
}
