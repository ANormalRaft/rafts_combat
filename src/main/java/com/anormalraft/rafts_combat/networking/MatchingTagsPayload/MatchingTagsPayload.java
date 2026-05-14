package com.anormalraft.rafts_combat.networking.MatchingTagsPayload;

import com.anormalraft.rafts_combat.networking.HurtPayload.HurtPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public record MatchingTagsPayload (String itemTag, String blockTag) implements CustomPacketPayload {

    public static final Type<MatchingTagsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rafts_combat", "matchingtagspayload"));

    public static final StreamCodec<ByteBuf, MatchingTagsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MatchingTagsPayload::itemTag,
            ByteBufCodecs.STRING_UTF8, MatchingTagsPayload::blockTag,
            MatchingTagsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
