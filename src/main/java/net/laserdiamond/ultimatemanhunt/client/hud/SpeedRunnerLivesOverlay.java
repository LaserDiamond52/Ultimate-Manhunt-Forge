package net.laserdiamond.ultimatemanhunt.client.hud;

import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameTime;
import net.laserdiamond.ultimatemanhunt.client.game.ClientHardcore;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientSpeedRunnerMaxLives;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final class SpeedRunnerLivesOverlay implements UMHUDOverlay {

    private static final ResourceLocation SPEED_RUNNER_EMPTY_HEART_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore");
    private static final ResourceLocation SPEED_RUNNER_FULL_HEART_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation SPEED_RUNNER_FULL_HEART_HARDCORE_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full");

    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        int lives = umPlayer.getLives();
        boolean isSpeedRunner = umPlayer.isSpeedRunner();
        long gameTime = ClientGameTime.getGameTime();

        if (isSpeedRunner)
        {
            int drawX = (guiGraphics.guiWidth() / 2 - 104);
            int drawY = (guiGraphics.guiHeight() - 39);

            if (ClientSpeedRunnerMaxLives.getMaxLives() <= 5)
            {
                for (int i = 0; i < ClientSpeedRunnerMaxLives.getMaxLives(); i++)
                {
                    guiGraphics.blitSprite(SPEED_RUNNER_EMPTY_HEART_TEXTURE, drawX, drawY - (i * 10), 9, 9);
                    if (i < lives)
                    {
                        if (ClientHardcore.isHardcore()) // Hardcore?
                        {
                            guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_HARDCORE_TEXTURE, drawX, drawY - (i * 10), 9, 9);
                        } else // Not hardcore
                        {
                            guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_TEXTURE, drawX, drawY - (i * 10), 9, 9);
                        }
                    }
                }
            } else
            {
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, Component.literal("x" + umPlayer.getLives()), drawX, drawY, ChatFormatting.WHITE.getColor());
                drawX -= 20;
                guiGraphics.blitSprite(SPEED_RUNNER_EMPTY_HEART_TEXTURE, drawX, drawY, 9, 9);
                if (ClientHardcore.isHardcore()) // Hardcore?
                {
                    guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_HARDCORE_TEXTURE, drawX, drawY, 9, 9);
                } else // Not hardcore
                {
                    guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_TEXTURE, drawX, drawY, 9, 9);
                }
            }


            Component speedRunnerGracePeriodComponent = getSpeedRunnerGracePeriodComponent(gameTime, umPlayer.getGracePeriodTimeStamp());

            drawX = (guiGraphics.guiWidth() / 2);
            double diff = guiGraphics.guiHeight() * 0.95678;
            drawY = (int) (guiGraphics.guiHeight() - (diff));

            if (!speedRunnerGracePeriodComponent.getString().isEmpty())
            {
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, speedRunnerGracePeriodComponent, drawX, drawY, ChatFormatting.BLUE.getColor());
            }
        }
    }

    @NotNull
    private Component getSpeedRunnerGracePeriodComponent(long gameTime, long gracePeriodTimeStamp)
    {
        double seconds = (double) (gracePeriodTimeStamp - gameTime) / 20;

        DecimalFormat format = new DecimalFormat("0.00");

        if (seconds >= 0)
        {
            return Component.literal(ChatFormatting.BLUE + "" + ChatFormatting.BOLD + "You are protected from hunters for " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.BLUE + ChatFormatting.BOLD + " seconds");
        } else
        {
            return Component.empty();
        }
    }
}
