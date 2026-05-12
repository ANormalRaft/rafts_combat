package com.anormalraft.rafts_combat.client;

import com.anormalraft.rafts_combat.networking.HurtPayload.HurtPayload;
import com.anormalraft.rafts_combat.util.DataUtils;
import com.anormalraft.rafts_combat.util.VectorUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.anormalraft.rafts_combat.Rafts_Combat.LOGGER;
import static net.minecraft.client.renderer.RenderStateShard.*;

@Mod(value = "rafts_combat", dist = Dist.CLIENT)
public class ClientTasks {

    //To render my quad correctly (no depth test)
    public static RenderType chargeMeterRenderType = RenderType.create("charge_meter", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 1536, false, true, RenderType.CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDepthTestState(new RenderStateShard.DepthTestStateShard("respectmyalphavalueuprick", GL11.GL_NOTEQUAL)).createCompositeState(false));
    //Detects click behavior
    public static boolean canRaftSwing = false;
    //Item data
    public static int maxChargeThreshold = -1;
    public static int currentChargeValue = -1;
    //List of hit targets
    public static ArrayList<EntityHitResult> entityHitResultList = new ArrayList<>();
    //Mining lock for first click. The block retaining its destruction status after a re-hover whilst keeping holding down the attack key is actually vanilla behavior lol
    public static boolean canMineFirstClick = false;
    
    //Key input logic
    public static void handleAttack() {
        LocalPlayer player = Minecraft.getInstance().player;
        if(DataUtils.isHoldingCorrectItem(player)) {
            //Disables the functionality if an item is being used
            if(player.isUsingItem()){
                return;
            }
            //Mod functionality
            //Holding down the key
            if (Minecraft.getInstance().options.keyAttack.isDown()) {
                //Lets tools mine their respective blocks if one is targeted with the starting click and we are not charging
                if(!canRaftSwing){
                    double interactionRange = player.entityInteractionRange();
                    Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
                    Vec3 mainCameraPosition = mainCamera.getPosition();
                    Vec3 eyePosition = new Vec3(mainCameraPosition.x, mainCameraPosition.y, mainCameraPosition.z);
                    Vec3 viewVector = player.getViewVector(1);
                    Vec3 scaledViewVector = viewVector.scale(interactionRange);
                    BlockHitResult blockHitResult = VectorUtils.getRaycastResultBlock(eyePosition, eyePosition.add(scaledViewVector), player);
                    if(blockHitResult.getType() != HitResult.Type.MISS){
                        ItemStack itemStack = player.getMainHandItem();
                        BlockPos blockPos = blockHitResult.getBlockPos();
                        BlockState blockState = Minecraft.getInstance().level.getBlockState(blockPos);
                        if(!canMineFirstClick) {
                            canMineFirstClick = DataUtils.tagMatchAny(itemStack, blockState);
                        }
                    }
                }
                if(!canMineFirstClick) {
                    //If we are already charging
                    if (canRaftSwing) {
                        //Start Charging
                        if (currentChargeValue < maxChargeThreshold) {
                            currentChargeValue += 1;
                        }
                        //progressivelySummonRaycasts takes care of the raycast and rendering logic
                    } else {
                        //If we are not already charging. Enable the charge
                        //If the mainhanditem item is a tool...
                        ItemStack itemStack = player.getMainHandItem();
                        //Get weapon data here & init charge meter data
                        Optional<ItemAttributeModifiers.Entry> use_coolown = itemStack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().stream().filter(attributeEntry -> attributeEntry.modifier().is(ResourceLocation.parse("minecraft:base_attack_speed"))).findFirst();
                        double actualAttackSpeed = use_coolown.get().attribute().value().getDefaultValue() + use_coolown.get().modifier().amount();
                        maxChargeThreshold = Mth.floor(20.0 / actualAttackSpeed);
                        currentChargeValue = 0;
                        //...Flip the swing boolean
                        canRaftSwing = true;
                    }
                }

                //Release the key when charging beforehand (Attack)
            } else if (canRaftSwing) {
                //Swing animation
                player.swing(InteractionHand.MAIN_HAND);
                //Attack packet (HurtPayload)
                //Extract the mob ids from entityHitResultList into an ArrayList of Integers to then send to the server. C2SHurtPayloadHandler applies the damage
                ArrayList<Integer> idArray = new ArrayList<>();
                for (EntityHitResult entityHitResult : entityHitResultList) {
                    idArray.add(entityHitResult.getEntity().getId());
                }
                PacketDistributor.sendToServer(new HurtPayload(idArray));
                //Reset charge data
                canRaftSwing = false;
                maxChargeThreshold = -1;
                currentChargeValue = -1;
                canMineFirstClick = false;
            } else if (canMineFirstClick){
                canMineFirstClick = false;
            }
        } else {
            canRaftSwing = false;
            maxChargeThreshold = -1;
            currentChargeValue = -1;
            canMineFirstClick = false;

        }
    }

    //Rendering that depends on the charge meter data
    public static void progressivelySummonRaycasts(RenderLevelStageEvent event) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //Needed or else we draw on all stages and some render weirdly
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            if (event.getCamera().getEntity() instanceof Player player) {
                if(maxChargeThreshold < 0 || currentChargeValue < 0){
                    return;
                }
                double chargeProgressPercentage = (double) currentChargeValue /maxChargeThreshold;
                float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
                double interactionRange = player.entityInteractionRange();
                Camera mainCamera = Minecraft.getInstance().gameRenderer.getMainCamera();
                Vec3 mainCameraPosition = mainCamera.getPosition();
                Vec3 eyePosition = new Vec3(mainCameraPosition.x, mainCameraPosition.y, mainCameraPosition.z);
                Vec3 viewVector = player.getViewVector(partialTick);
                Vec3 scaledViewVector = viewVector.scale(interactionRange);

                //Get the first person camera position when in third person(s)
                boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
                if (!isFirstPerson) {
                    eyePosition = VectorUtils.getFirstPersonCameraPosition(mainCamera);
                }

                //Player raycast (endpoint) position
                Vec3 endpoint = eyePosition.add(scaledViewVector);
                //Raycast
                EntityHitResult endpointRaycastResult = VectorUtils.getRaycastResult(eyePosition, endpoint, interactionRange, player);

                //Offset vectors
                //offsetXZ needs to be negative with my setup due to quad rendering shenanigans probably
                //Has to be scaled with a ratio from the interactionRange
                double turnRatio = 2.0/5.0;
                double offsetXZ = -(interactionRange * turnRatio);
                double offsetY = 0.0;
                Vec3 lastOffsetVector = VectorUtils.calculateOffsetVector(offsetXZ, offsetY, endpoint);
                Vec3 lastOffsetVectorMirrored = VectorUtils.calculateOffsetVector(-offsetXZ, offsetY, endpoint);

                //Clear list
                entityHitResultList.clear();
                //Summons all remaining offsets & get their results
                VectorUtils.raycastOffsets(chargeProgressPercentage, lastOffsetVector, lastOffsetVectorMirrored, eyePosition, endpoint, interactionRange, player, entityHitResultList);
                DataUtils.nonDuplicatesAddToList(entityHitResultList, endpointRaycastResult);
                //Remove all nulls
                entityHitResultList.removeIf(Objects::isNull);

                //Rendering
                renderQuads(event, mainCameraPosition, viewVector, endpoint, lastOffsetVector, lastOffsetVectorMirrored, chargeProgressPercentage, interactionRange);
            }
        }
    }

    //Renders the quads and performs the calculations required to render them
    public static void renderQuads(RenderLevelStageEvent event, Vec3 mainCameraPosition, Vec3 viewVector, Vec3 endpoint, Vec3 lastOffsetVector, Vec3 lastOffsetVectorMirrored, double chargeProgressPercentage, double interactionRange){
        //PoseStack stuff
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        //Thank you TopSnek & Zergatul from the Forge Forums <3
        poseStack.translate(-mainCameraPosition.x, -mainCameraPosition.y, -mainCameraPosition.z);
        PoseStack.Pose pose = poseStack.last();
        //Buffer stuff
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexBufferQuad = bufferSource.getBuffer(chargeMeterRenderType);

        //Calculate the "always left" vector
        Vec3 leftOrthogonalViewVector = VectorUtils.calculateOffsetVector(Mth.PI, 0, viewVector).normalize();
        //The scale must be a ratio from the interaction range to keep its "zoom"
        double quadHeight = interactionRange * 7/1000;
        Vec3 correctHeightDirection = viewVector.cross(leftOrthogonalViewVector).scale(quadHeight);

        //Calculate reveal position
        Vec3 voidedChargeAccurateOffsetVector = endpoint.vectorTo(lastOffsetVector).scale(chargeProgressPercentage);
        Vec3 chargeAccurateOffsetVector = endpoint.add(voidedChargeAccurateOffsetVector);
        Vec3 voidedChargeAccurateOffsetVectorMirrored = endpoint.vectorTo(lastOffsetVectorMirrored).scale(chargeProgressPercentage);
        Vec3 chargeAccurateOffsetVectorMirrored = endpoint.add(voidedChargeAccurateOffsetVectorMirrored);
        //Calculate alpha value
        int maxAlpha = 100;
        int minAlpha = 0;
        int currentAlpha = Mth.floor((maxAlpha * chargeProgressPercentage) + minAlpha);
        //Turn it red when it detects at least 1 target
        int colorValue = 255;
        if(!entityHitResultList.isEmpty()){
            colorValue = 0;
        }

        //Quad rendering (representing extended horizontal range)
        vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
        vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
        vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVector.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);
        vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVector.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);
        //Mirror it (HOLY FUCK WHY WAS THIS SO HARD TO FIGURE OUT)
        vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
        vertexBufferQuad.addVertex(pose, endpoint.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, minAlpha);
        vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVectorMirrored.add(correctHeightDirection).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);
        vertexBufferQuad.addVertex(pose, chargeAccurateOffsetVectorMirrored.add(correctHeightDirection.scale(-1)).toVector3f()).setColor(255, colorValue, colorValue, currentAlpha);

        poseStack.popPose();
    }
}
