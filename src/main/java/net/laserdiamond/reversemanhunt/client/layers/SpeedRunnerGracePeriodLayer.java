package net.laserdiamond.reversemanhunt.client.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.game.PlayerGameTimeCapability;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.client.models.GracePeriodArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;

public final class SpeedRunnerGracePeriodLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ResourceLocation TEXTURE = ReverseManhunt.fromRMPath("textures/entity/player/grace_period_armor.png");

    public static final ModelLayerLocation MODEL_LAYER_LOCATION = new ModelLayerLocation(ReverseManhunt.fromRMPath("grace_period_armor"), "main");

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
        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner -> // Get player speed runner capability
        {
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter -> // Get player hunter capability
            {
                if (playerSpeedRunner.getWasLastKilledByHunter() && !playerHunter.isHunter())
                {
                    player.getCapability(PlayerGameTimeCapability.PLAYER_GAME_TIME).ifPresent(playerGameTime ->
                    {
                        if (playerGameTime.getGameTime() < playerSpeedRunner.getGracePeriodTimeStamp())
                        {
                            GracePeriodArmorModel.render(this.getParentModel(), this.gracePeriodArmorModel, TEXTURE, poseStack, multiBufferSource, packedLight, player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 16777215);
                        }
                    });
                }

            });
        });
    }
}
