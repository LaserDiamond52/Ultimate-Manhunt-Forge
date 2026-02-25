package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.laserdiamond.ultimatemanhunt.api.event.UltimateManhuntGameStateEvent;
import net.laserdiamond.ultimatemanhunt.client.ClientSettings;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

public final class GameEndAnnounceS2CPacket extends AnnounceS2CPacket {

    private final UltimateManhuntGameStateEvent.End.Reason reason;

    public GameEndAnnounceS2CPacket(UltimateManhuntGameStateEvent.End.Reason reason)
    {
        this.reason = reason;
    }

    public GameEndAnnounceS2CPacket(FriendlyByteBuf buf)
    {
        this.reason = buf.readEnum(UltimateManhuntGameStateEvent.End.Reason.class);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(this.reason);
    }

    @Override
    public Component title(Minecraft minecraft) {
        return switch (this.reason)
        {
            case HUNTER_WIN -> Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "No more Speed Runners!");
            case SPEED_RUNNERS_WIN -> Component.literal(ChatFormatting.AQUA + "" + ChatFormatting.BOLD + "Ender Dragon defeated!");
            case COMMAND -> Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.BOLD + "Game Ended");
        };
    }

    @Override
    public Component subTitle(Minecraft minecraft) {
        return switch (this.reason)
        {
            case HUNTER_WIN -> Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Hunters win!");
            case SPEED_RUNNERS_WIN -> Component.literal(ChatFormatting.AQUA + "" + ChatFormatting.BOLD + "Speed Runners win!");
            case COMMAND -> Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.BOLD + "No one wins!");
        };
    }
}
