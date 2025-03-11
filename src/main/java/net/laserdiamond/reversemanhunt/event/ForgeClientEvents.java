package net.laserdiamond.reversemanhunt.event;

import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Deprecated
@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {

//    @SubscribeEvent
//    public static void render(RenderPlayerEvent.Pre event)
//    {
//        Player player = event.getEntity();
//        PlayerRenderer renderer = event.getRenderer();
//        float partialTick = event.getPartialTick();
//        PoseStack poseStack = event.getPoseStack();
//        MultiBufferSource bufferSource = event.getMultiBufferSource();
//        int packedLight = event.getPackedLight();
//
//        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
//        {
//            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
//            {
//                if (playerSpeedRunner.getWasLastKilledByHunter() && !playerHunter.isHunter())
//                {
//                    if (player instanceof AbstractClientPlayer abstractClientPlayer)
//                    {
//                        float limbSwing = 0.0F;
//                        float limbSwingAmount = 0.0F;
//                        float ageInTicks = player.tickCount + partialTick;
//                        float f = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
//                        float f1 = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
//                        float netHeadYaw = f1 - f;
//                        float headPitch;
//
//                        boolean shouldSit = player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());
//
//                        if (shouldSit && player.getVehicle() instanceof LivingEntity living)
//                        {
//                            f = Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot);
//                            netHeadYaw = f1 - f;
//                        }
//
//                        GracePeriodArmorModel.render(renderer.getModel(), new GracePeriodArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(SpeedRunnerGracePeriodLayer.MODEL_LAYER_LOCATION)), SpeedRunnerGracePeriodLayer.TEXTURE, poseStack, bufferSource, packedLight, abstractClientPlayer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
//                    }
//                }
//            });
//        });
//    }
}
