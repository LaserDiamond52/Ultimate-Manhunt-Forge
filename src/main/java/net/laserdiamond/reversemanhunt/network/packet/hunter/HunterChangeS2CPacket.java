package net.laserdiamond.reversemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.client.ClientPlayer;
import net.laserdiamond.reversemanhunt.client.hunter.ClientHunter;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class HunterChangeS2CPacket extends NetworkPacket {

    private final boolean hunter;
    private final boolean buffed;

//    public HunterChangeS2CPacket(boolean hunter, boolean buffed)
//    {
//        this.hunter = hunter;
//        this.buffed = buffed;
//    }
    public HunterChangeS2CPacket(PlayerHunter playerHunter)
    {
        this.hunter = playerHunter.isHunter();
        this.buffed = playerHunter.isBuffed();
    }

    public HunterChangeS2CPacket(FriendlyByteBuf buf)
    {
        this.hunter = buf.getBoolean(1);
        this.buffed = buf.getBoolean(2);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.hunter);
        buf.writeBoolean(this.buffed);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        // ON CLIENT
        ClientHunter.setHunter(this.hunter);
        ClientHunter.setBuffed(this.buffed);
    }
}
