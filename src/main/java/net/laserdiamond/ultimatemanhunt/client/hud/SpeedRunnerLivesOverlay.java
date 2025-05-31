package net.laserdiamond.ultimatemanhunt.client.hud;

import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameTime;
import net.laserdiamond.ultimatemanhunt.client.game.ClientHardcore;
import net.laserdiamond.ultimatemanhunt.client.speedrunner.ClientSpeedRunnerMaxLives;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final class SpeedRunnerLivesOverlay implements UMHUDOverlay {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, float partialTick)
    {
        int lives = umPlayer.getLives();
        boolean isSpeedRunner = umPlayer.isSpeedRunner();
        long gameTime = ClientGameTime.getGameTime();

        if (isSpeedRunner)
        {
            int drawX = (guiGraphics.guiWidth() / 2 - 104);
            int drawY = (guiGraphics.guiHeight() - 39);

            if (UMPlayer.getMaxLives() <= 5)
            {
                for (int i = 0; i < ClientSpeedRunnerMaxLives.getMaxLives(); i++)
                {
                    guiGraphics.blit(Gui.GUI_ICONS_LOCATION, drawX, drawY - (i * 10), 16, 0, 9, 9);
                    if (i < lives)
                    {
                        if (ClientHardcore.isHardcore()) // Hardcore?
                        {
                            guiGraphics.blit(Gui.GUI_ICONS_LOCATION, drawX, drawY - (i * 10), 52, 45, 9, 9);
                        } else // Not hardcore
                        {
                            guiGraphics.blit(Gui.GUI_ICONS_LOCATION, drawX, drawY - (i * 10), 52, 0, 9, 9);
                        }
                    }
                }
            } else
            {
                guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal("x" + umPlayer.getLives()), drawX, drawY, ChatFormatting.WHITE.getColor());
                drawX -= 20;
                guiGraphics.blit(Gui.GUI_ICONS_LOCATION, drawX, drawY, 16, 0, 9, 9);
                if (ClientHardcore.isHardcore()) // Hardcore?
                {
                    guiGraphics.blit(Gui.GUI_ICONS_LOCATION, drawX, drawY, 52, 45, 9, 9);
                } else // Not hardcore
                {
                    guiGraphics.blit(Gui.GUI_ICONS_LOCATION, drawX, drawY, 52, 0, 9, 9);
                }
            }


            Component speedRunnerGracePeriodComponent = getSpeedRunnerGracePeriodComponent(gameTime, umPlayer.getGracePeriodTimeStamp());

            drawX = (guiGraphics.guiWidth() / 2);
            double diff = guiGraphics.guiHeight() * 0.95678;
            drawY = (int) (guiGraphics.guiHeight() - (diff));

            if (!speedRunnerGracePeriodComponent.getString().isEmpty())
            {
                guiGraphics.drawCenteredString(MINECRAFT.font, speedRunnerGracePeriodComponent, drawX, drawY, ChatFormatting.BLUE.getColor());
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
