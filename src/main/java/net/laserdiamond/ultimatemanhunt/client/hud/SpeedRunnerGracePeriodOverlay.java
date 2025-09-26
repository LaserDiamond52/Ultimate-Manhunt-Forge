package net.laserdiamond.ultimatemanhunt.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;

public final class SpeedRunnerGracePeriodOverlay implements UMHUDOverlay {

    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, float partialTick)
    {
        if (umPlayer.isSpeedRunnerOnGracePeriodClient())
        {
            int width = guiGraphics.guiWidth();
            int height = guiGraphics.guiHeight();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            guiGraphics.setColor(0.1F, 0.1F, 0.3F, 1.0F);
            guiGraphics.blit(GameRenderer.NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }
}
