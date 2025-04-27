package net.laserdiamond.ultimatemanhunt.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;

public interface UMHUDOverlay extends LayeredDraw.Layer {

    @Override
    default void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
        {
            return;
        }
        if (shouldNotRenderInSpectator(player))
        {
            return;
        }
        if (!renderIfGameNotStarted())
        {
            return;
        }
        RenderSystem.enableBlend();
        this.setUpRender();
        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer -> onRender(player, umPlayer, guiGraphics, deltaTracker));
        RenderSystem.disableBlend();
    }

    void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, DeltaTracker deltaTracker);

    default boolean shouldNotRenderInSpectator(LocalPlayer player)
    {
        return player.isSpectator();
    }

    default boolean renderIfGameNotStarted()
    {
        return ClientGameState.hasGameBeenStarted();
    }

    default void setUpRender()
    {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
