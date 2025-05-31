package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

public final class GameResumedS2CPacket extends AnnounceS2CPacket {

    public GameResumedS2CPacket() {}

    public GameResumedS2CPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void packetWork(NetworkEvent.Context context)
    {
        // ON CLIENT
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null)
        {
            player.playSound(SoundEvents.WITHER_SPAWN);

        }
        super.packetWork(context);
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
