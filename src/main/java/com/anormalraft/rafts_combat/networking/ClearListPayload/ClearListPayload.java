package com.anormalraft.rafts_combat.networking.ClearListPayload;

import com.anormalraft.rafts_combat.Rafts_Combat;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

//Not used but kept for reference
public class ClearListPayload implements CustomPacketPayload {

    //Huge Thanks to Dr.gigaherz for providing an example of a payload which uses a StreamCodec.unit. When sending to server, the INSTANCE must be used instead of creating a new one. The example comes from: https://github.com/gigaherz/HudCompass/blob/master/src/main/java/dev/gigaherz/hudcompass/network/ClientHello.java
    public static final ClearListPayload INSTANCE = new ClearListPayload();

    public static final Type<ClearListPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rafts_combat", "clearlistpayload"));

    public static final StreamCodec<ByteBuf, ClearListPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private ClearListPayload(){}

    public void handleDataOnMain(IPayloadContext context) {
        context.enqueueWork(() -> {
            Rafts_Combat.entityHitResultListServer.clear();
        }).exceptionally(e -> {
            context.disconnect(Component.literal(e.getMessage()));
            return null;
        });
    }
}

