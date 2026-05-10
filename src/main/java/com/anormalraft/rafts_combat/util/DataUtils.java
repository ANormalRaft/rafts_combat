package com.anormalraft.rafts_combat.util;

import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;

public class DataUtils {
    //Adds if there isn't a UUID duplicate and if the raycast result isn't null
    public static void nonDuplicatesAddToList(ArrayList<EntityHitResult> arrayList, EntityHitResult entityHitResult){
        if(entityHitResult != null) {
            for (EntityHitResult element : arrayList) {
                if (element.getEntity().getId() == entityHitResult.getEntity().getId()) {
                    return;
                }
            }
            arrayList.add(entityHitResult);
        }
    }
}
