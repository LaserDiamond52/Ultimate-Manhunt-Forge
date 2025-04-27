package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public final class GameStartAnnounceS2CPacket extends AnnounceS2CPacket {

    public GameStartAnnounceS2CPacket() {}

    public GameStartAnnounceS2CPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Component title(Minecraft minecraft) {
        return Component.literal(ChatFormatting.GREEN + "" + ChatFormatting.BOLD + "Game Started");
    }

    @Override
    public Component subTitle(Minecraft minecraft) {
        return Component.literal(ChatFormatting.GREEN + "The Manhunt has started!");
    }
}
