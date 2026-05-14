package com.anormalraft.rafts_combat.networking.HurtPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record HurtPayload(List<Integer> idList, double chargeProgressPercentage) implements CustomPacketPayload {

    public static final Type<HurtPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rafts_combat", "hurtpayload"));

    public static final StreamCodec<ByteBuf, HurtPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()), HurtPayload::idList,
        ByteBufCodecs.DOUBLE, HurtPayload::chargeProgressPercentage,
        HurtPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
