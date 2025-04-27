package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public final class GameResumedS2CPacket extends AnnounceS2CPacket {

    public GameResumedS2CPacket() {}

    public GameResumedS2CPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public Component title(Minecraft minecraft) {
        return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Game Resumed");
    }

    @Override
    public Component subTitle(Minecraft minecraft) {
        return Component.literal(ChatFormatting.RED + "The Manhunt has resumed!");
    }
}
