package com.anormalraft.rafts_combat.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

import java.util.ArrayList;

public class DataUtils {

    //Adds if there isn't a UUID duplicate and if the raycast result isn't null
    public static void nonDuplicatesAddToList(ArrayList<EntityHitResult> arrayList, EntityHitResult entityHitResult) {
        if (entityHitResult != null) {
            for (EntityHitResult element : arrayList) {
                if (element.getEntity().getId() == entityHitResult.getEntity().getId()) {
                    return;
                }
            }
            arrayList.add(entityHitResult);
        }
    }

    //Conditional in method form to see if the player is holding an itemstack and if that itemstack is a tool
    public static boolean isHoldingCorrectItem(Player player) {
        if (player != null) {
            ItemStack itemStack = player.getMainHandItem();
            if (!itemStack.isEmpty()) {
                return itemStack.getComponents().has(DataComponents.TOOL);
            }
        }
        return false;
    }
}
