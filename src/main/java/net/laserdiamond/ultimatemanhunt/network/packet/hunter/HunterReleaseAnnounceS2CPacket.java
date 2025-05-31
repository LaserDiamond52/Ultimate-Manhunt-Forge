package net.laserdiamond.ultimatemanhunt.network.packet.hunter;

import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.AnnounceS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

public class HunterReleaseAnnounceS2CPacket extends AnnounceS2CPacket {

    private final boolean isHunter;

    public HunterReleaseAnnounceS2CPacket(boolean isHunter)
    {
        this.isHunter = isHunter;
    }

    public HunterReleaseAnnounceS2CPacket(FriendlyByteBuf buf)
    {
        this.isHunter = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeBoolean(this.isHunter);
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
    public Component title(Minecraft minecraft)
    {
        if (this.isHunter)
        {
            return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Released!");
        }
        return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Watch Out!");
    }

    @Override
    public Component subTitle(Minecraft minecraft)
    {
        if (this.isHunter)
        {
            return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Go and hunt down the speed runners!");
        }
        return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "The hunters are out to find you!");
    }


}
