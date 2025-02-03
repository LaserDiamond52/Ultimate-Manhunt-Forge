package net.laserdiamond.reversemanhunt.network.packet;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.network.CustomPayloadEvent;

public abstract class AnnounceS2CPacket extends NetworkPacket {


    @Override
    public void packetWork(CustomPayloadEvent.Context context)
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
