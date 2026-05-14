package com.anormalraft.rafts_combat.networking.MatchingTagsPayload;

import com.anormalraft.rafts_combat.util.DataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class S2CMatchingTagsPayloadHandler {
    public static void handleDataOnMain(final MatchingTagsPayload data, IPayloadContext context){
        context.enqueueWork(()->{
            TagKey<Item> itemTagKey = ItemTags.create(ResourceLocation.parse(data.itemTag()));
            TagKey<Block> blockTagKey = BlockTags.create(ResourceLocation.parse(data.blockTag()));
            DataUtils.itemTagsBlockTagsHashMap.put(itemTagKey, blockTagKey);
        }).exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.literal( e.getMessage()));
            return null;
        });
    }
}
