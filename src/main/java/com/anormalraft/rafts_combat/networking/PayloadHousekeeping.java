package com.anormalraft.rafts_combat.networking;

import com.anormalraft.rafts_combat.networking.ClearListPayload.ClearListPayload;
import com.anormalraft.rafts_combat.networking.HurtPayload.C2SHurtPayloadHandler;
import com.anormalraft.rafts_combat.networking.HurtPayload.HurtPayload;
import com.anormalraft.rafts_combat.networking.RaycastPayload.C2SRaycastPayloadHandler;
import com.anormalraft.rafts_combat.networking.RaycastPayload.RaycastPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

//Where all payloads are registered
public class PayloadHousekeeping {
    //Registers Payloads
    public static void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(RaycastPayload.TYPE, RaycastPayload.STREAM_CODEC, C2SRaycastPayloadHandler::handleDataOnMain);
        registrar.playToServer(HurtPayload.TYPE, HurtPayload.STREAM_CODEC, C2SHurtPayloadHandler::handleDataOnMain);
        registrar.playToServer(ClearListPayload.TYPE, ClearListPayload.STREAM_CODEC, ClearListPayload::handleDataOnMain);
    }
}
