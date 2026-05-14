package com.anormalraft.rafts_combat.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Helper methods that manipulate or check data
public class DataUtils {

    public static HashMap<TagKey<Item>, TagKey<Block>> itemTagsBlockTagsHashMap = new HashMap<>(5);

    //Adds if there isn't an Entity id duplicate and if the raycast result isn't null
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

    //Conditional in method form to see if the entity is holding an itemstack and if that itemstack is a tool. Keep in mind that mod authors often don't include these or misspell them...
    public static boolean isHoldingCorrectItem(LivingEntity entity) {
        if (entity != null) {
            ItemStack itemStack = entity.getMainHandItem();
            if (!itemStack.isEmpty()) {
                return itemStack.is(Tags.Items.MELEE_WEAPON_TOOLS);
            }
        }
        return false;
    }

    //Pet check and Horse-like check
    public static boolean isNotPet(Entity entity, Entity player){
        //If the entity can either be owned or tamed
        if(entity instanceof OwnableEntity){
            //If it is owned, return false
            LivingEntity owner = ((OwnableEntity) entity).getOwner();
            if(owner != null) {
                return owner.getId() != player.getId();
            }
            //If it doesn't target the player and it extends from AbstractHorse and it is tamed, return false
            if(entity instanceof AbstractHorse){
                boolean isTargetingPlayer = false;
                if(((Mob) entity).getTarget() != null) {
                    isTargetingPlayer =  ((Mob) entity).getTarget().getId() == player.getId();
                }
                return !((AbstractHorse) entity).isTamed() && !isTargetingPlayer;
            }
        }
        return true;
    }

    //Compares block tag and item tag for an ANY match
    public static boolean tagMatchAny(ItemStack itemStack, BlockState blockState){
        for (Map.Entry<TagKey<Item>,TagKey<Block>> entry : itemTagsBlockTagsHashMap.entrySet()){
            if(itemStack.is(entry.getKey()) && blockState.is(entry.getValue())){
                return true;
            }
        }
        return false;
    }

    //Checks for item tags for disallowed right-clickable items
    public static boolean tagNoRightClick(ItemStack itemStack){
        ArrayList<TagKey<Item>> list = new ArrayList<>();
        list.add(Tags.Items.FOODS);
        list.add(Tags.Items.POTIONS);
        list.add(Tags.Items.TOOLS_BOW);
        list.add(Tags.Items.TOOLS_CROSSBOW);
        list.add(Tags.Items.TOOLS_SPEAR);

        for(TagKey<Item> tagKey: list){
            if(itemStack.is(tagKey)){
                return true;
            }
        }
        return false;
    }

    //Damage scaling calculation similar to exponential
    public static float calculateScaling(double chargeProgressPercent){
        return (float) (Math.pow(10,chargeProgressPercent) - 1) /(10-1);
    }
}
