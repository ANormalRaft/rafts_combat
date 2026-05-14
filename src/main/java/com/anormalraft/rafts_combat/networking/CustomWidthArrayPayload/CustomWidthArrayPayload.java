package com.anormalraft.rafts_combat.networking.CustomWidthArrayPayload;

import com.anormalraft.rafts_combat.networking.HurtPayload.HurtPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CustomWidthArrayPayload(Double value, List<ItemStack> itemStackList) implements CustomPacketPayload {
    public static final Type<CustomWidthArrayPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rafts_combat", "customwidtharraypayload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomWidthArrayPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, CustomWidthArrayPayload::value,
        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), CustomWidthArrayPayload::itemStackList,
        CustomWidthArrayPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
