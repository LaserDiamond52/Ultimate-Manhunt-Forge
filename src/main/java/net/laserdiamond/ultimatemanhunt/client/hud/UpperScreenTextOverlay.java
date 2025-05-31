package net.laserdiamond.ultimatemanhunt.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameState;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameTime;
import net.laserdiamond.ultimatemanhunt.client.game.ClientRemainingPlayers;
import net.laserdiamond.ultimatemanhunt.client.hunter.ClientHunterGracePeriod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public final class UpperScreenTextOverlay implements UMHUDOverlay {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, float partialTick)
    {
        int drawX = (guiGraphics.guiWidth() / 2);
        double diff = guiGraphics.guiHeight() * 0.95678;
        int drawY = (int) (guiGraphics.guiHeight() - (diff));

        long gameTime = ClientGameTime.getGameTime();
        if (gameTime < ClientHunterGracePeriod.getGracePeriodDuration())
        {
            Component hunterGracePeriodComponent = getHunterGracePeriodComponent(umPlayer.isHunter(), gameTime);
            guiGraphics.drawCenteredString(MINECRAFT.font, hunterGracePeriodComponent, drawX, drawY, ChatFormatting.RED.getColor());
        }

        Component remainingHuntersComponent = createRemainingPlayersComponent(true);
        Component remainingSpeedRunnersComponent = createRemainingPlayersComponent(false);

        guiGraphics.drawCenteredString(MINECRAFT.font, remainingHuntersComponent, drawX, drawY + 10, ChatFormatting.RED.getColor());
        guiGraphics.drawCenteredString(MINECRAFT.font, remainingSpeedRunnersComponent, drawX, drawY + 20, ChatFormatting.BLUE.getColor());
    }

    @NotNull
    private Component getHunterGracePeriodComponent(boolean isHunter, long gameTime) {

        double seconds = (double) (ClientHunterGracePeriod.getGracePeriodDuration() - gameTime) / 20;

        DecimalFormat format = new DecimalFormat("0.00");

        if (isHunter)
        {
            return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "You will be released in " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.RED + ChatFormatting.BOLD + " seconds");
        } else
        {
            return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Hunters will be released in " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.RED + ChatFormatting.BOLD + " seconds");
        }
    }

    @NotNull
    private Component createRemainingPlayersComponent(boolean isHunters)
    {
        int speedRunners = ClientRemainingPlayers.getRemainingSpeedRunners();
        int hunters = ClientRemainingPlayers.getRemainingHunters();

        if (isHunters)
        {
            return Component.literal(ChatFormatting.RED + "Remaining Hunters: " + ChatFormatting.YELLOW + hunters);
        } else
        {
            return Component.literal(ChatFormatting.BLUE + "Remaining Speed Runners: " + ChatFormatting.YELLOW + speedRunners);
        }
    }
}
