package com.anormalraft.rafts_combat;

import com.anormalraft.rafts_combat.client.ClientTasks;
import com.anormalraft.rafts_combat.config.ClientConfig;
import com.anormalraft.rafts_combat.config.ServerConfig;
import com.anormalraft.rafts_combat.networking.CustomWidthArrayPayload.CustomWidthArrayPayload;
import com.anormalraft.rafts_combat.networking.MatchingTagsPayload.MatchingTagsPayload;
import com.anormalraft.rafts_combat.networking.PayloadHousekeeping;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Rafts_Combat.MODID)
public class Rafts_Combat {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "rafts_combat";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    //Serverside charge progress percentage
    public static double serverChargeProgressPercentage = -1;
    //Mace test
    public static boolean canCrit = false;

    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Rafts_Combat(IEventBus modEventBus, ModContainer modContainer) {
        //Register Networking Payloads
        modEventBus.addListener(PayloadHousekeeping::registerPayload);
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's configs
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    //Sync custom ratios from config and also init the hashmap in DataUtils
    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //Sync custom ratios (code from Toolforme)
        HashMap<Double, Item[]> bindingsHashMap = HashMap.newHashMap(3);
        //Gson-ify bindings
        JsonObject bindings = new Gson().fromJson(ServerConfig.CUSTOM_RATIOS.get(), JsonObject.class);
        for(var entry : bindings.asMap().entrySet()){
            //You cannot do like in KubeJS where you can use "matches()". You have to do all these steps due to Java devs
            String output = entry.getValue().toString();
            //I don't know why the Value appears with the "", but not the key. Maybe somewhere in the conversions, the key lost them?
            String stringValue = output.substring(1, output.length()-1);
            //Array value
            if(stringValue.charAt(0) == '['){
                String stringWishlist = stringValue.substring(1, stringValue.length() -1);
                String[] stringArray = stringWishlist.split(", ");
                Item[] allMatchesArray = BuiltInRegistries.ITEM.stream().filter((item) -> Arrays.asList(stringArray).contains(item.toString())).toArray(Item[]::new);
                bindingsHashMap.put(Double.valueOf(entry.getKey()), allMatchesArray);
                //Regex value
            } else {
                Pattern pattern = Pattern.compile(stringValue);
                Item[] allMatchesArray = BuiltInRegistries.ITEM.stream().filter((item) -> pattern.matcher(item.toString()).find()).toArray(Item[]::new);
                bindingsHashMap.put(Double.valueOf(entry.getKey()), allMatchesArray);
            }
        }
        Player player = event.getEntity();
        ServerPlayer serverPlayer = player.getServer().getPlayerList().getPlayer(player.getUUID());
        bindingsHashMap.forEach((k,v) -> {
            ItemStack[] itemStackArray = new ItemStack[v.length];
            for(int i=0; i < itemStackArray.length; i++){
                itemStackArray[i] = v[i].getDefaultInstance();
            }
            PacketDistributor.sendToPlayer(serverPlayer, new CustomWidthArrayPayload(k, Arrays.asList(itemStackArray)));
        });

        //DataUtils hashmap init sent to client. Perhaps could be a future config option, but for now no
        HashMap<String, String> tagsMap = new HashMap<>(5);
        tagsMap.put("minecraft:axes", "minecraft:mineable/axe");
        tagsMap.put("minecraft:pickaxes","minecraft:mineable/pickaxe");
        tagsMap.put("minecraft:hoes","minecraft:mineable/hoe");
        tagsMap.put("minecraft:shovels","minecraft:mineable/shovel");
        tagsMap.put("minecraft:swords","minecraft:sword_efficient");
        tagsMap.forEach((k,v) -> {
            PacketDistributor.sendToPlayer(serverPlayer, new MatchingTagsPayload(k, v));
        });
    }

    //TODO list: Config cooldown time, Decouple shield stuff from Toolforme to make its own mod and put the correct ordinal in LivingEntityMixin there

    @SubscribeEvent
    public void onRenderLevelEvent(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ClientTasks.progressivelySummonRaycasts(event);
//        RenderDebug.debugRender(event);
    }

    // Event is on the NeoForge event bus only on the physical client
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        ClientTasks.handleAttack();
    }

    //Cancel specific item interactions when a charge is undergoing (this isn't bound to the right click button specifically)
    @SubscribeEvent
    public void onPlayerInteractRightClick(PlayerInteractEvent.RightClickItem event){
        if(ClientTasks.canRaftSwing && DataUtils.tagNoRightClick(event.getItemStack())){
            event.setCanceled(true);
            //Thank you Random from Neoforge discord
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }
}
