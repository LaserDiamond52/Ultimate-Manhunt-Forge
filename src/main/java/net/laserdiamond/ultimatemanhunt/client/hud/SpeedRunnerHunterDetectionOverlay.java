package net.laserdiamond.ultimatemanhunt.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameState;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientDistanceFromHunter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;

public final class SpeedRunnerHunterDetectionOverlay implements UMHUDOverlay {

    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        float distanceFromHunter = ClientDistanceFromHunter.getDistance();

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();

        if (umPlayer.isSpeedRunner())
        {
            if ((distanceFromHunter > -1) && (distanceFromHunter < UMGame.HUNTER_DETECTION_RANGE) && !umPlayer.isSpeedRunnerOnGracePeriodClient()) // Player has to be in hunter detection range, distance cannot be 0, and must not be on grace period
            {
                float red = -(distanceFromHunter / 110F) + 0.9F;

                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                guiGraphics.setColor(red, 0.1F, 0.1F, 1.0F);
                guiGraphics.blit(GameRenderer.NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
            }
        }
    }

    @Override
    public boolean renderIfGameNotStarted() {
        return ClientGameState.isGameRunning();
    }
}
