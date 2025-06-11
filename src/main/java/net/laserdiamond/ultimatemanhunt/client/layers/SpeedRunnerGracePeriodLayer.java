package net.laserdiamond.ultimatemanhunt.client.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.client.models.GracePeriodArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SpeedRunnerGracePeriodLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ResourceLocation TEXTURE = UltimateManhunt.fromUMPath("textures/entity/player/grace_period_armor.png");

    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(UltimateManhunt.fromUMPath("grace_period_armor"), "main");

    private final GracePeriodArmorModel<AbstractClientPlayer> gracePeriodArmorModel;

    public SpeedRunnerGracePeriodLayer(PlayerRenderer pRenderer, EntityModelSet modelSet)
    {
        super(pRenderer);
        this.gracePeriodArmorModel = new GracePeriodArmorModel<>(modelSet.bakeLayer(MODEL_LAYER_LOCATION));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        // We want to check if the player was last killed by the hunter and if the player is a hunter through the capability because it is saved TO THE PLAYER
        // The Client values are results from the packet. Checking purely with those will render the shield on the client for all players

        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            if (umPlayer.isSpeedRunner() && umPlayer.isWasLastKilledByHunter())
            {
                if (umPlayer.isSpeedRunnerOnGracePeriodClient())
                {
                    GracePeriodArmorModel.render(this.getParentModel(), this.gracePeriodArmorModel, TEXTURE, poseStack, multiBufferSource, packedLight, player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                }
            }
        });
    }
}
