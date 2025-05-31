package net.laserdiamond.ultimatemanhunt.network.packet.game.announce;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public abstract class AnnounceS2CPacket extends NetworkPacket {

    public AnnounceS2CPacket() {}

    public AnnounceS2CPacket(FriendlyByteBuf buf) {}

    @Override
    public void packetWork(NetworkEvent.Context context)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setTitle(title(minecraft));
        minecraft.gui.setSubtitle(subTitle(minecraft));
    }

    /**
     * @return A {@link Component} being used to display the title on the client's screen
     */
    public abstract Component title(Minecraft minecraft);

    /**
     * @return A {@link Component} being used to display the subtitle on the client's screen
     */
    public abstract Component subTitle(Minecraft minecraft);
}
