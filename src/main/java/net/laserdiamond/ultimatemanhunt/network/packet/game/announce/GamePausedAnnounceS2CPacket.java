package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.laserdiamond.ultimatemanhunt.client.ClientSettings;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

public final class GamePausedAnnounceS2CPacket extends AnnounceS2CPacket {

    public GamePausedAnnounceS2CPacket() {}

    public GamePausedAnnounceS2CPacket(FriendlyByteBuf buf)
    {
        super(buf);
    }

    @Override
    public Component title(Minecraft minecraft) {
        return Component.literal(ChatFormatting.DARK_GRAY + "" + ChatFormatting.BOLD + "Game Paused");
    }

    @Override
    public Component subTitle(Minecraft minecraft) {
        return Component.literal(ChatFormatting.DARK_GRAY + "The Manhunt has been paused!");
    }
}
