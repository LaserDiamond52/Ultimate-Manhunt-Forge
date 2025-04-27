package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.event.network.CustomPayloadEvent;

public final class GamePausedAnnounceS2CPacket extends AnnounceS2CPacket {

    public GamePausedAnnounceS2CPacket() {}

    public GamePausedAnnounceS2CPacket(FriendlyByteBuf buf)
    {
        super(buf);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player != null)
        {
            player.playSound(SoundEvents.ELDER_GUARDIAN_CURSE);

        }
        super.packetWork(context);
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
