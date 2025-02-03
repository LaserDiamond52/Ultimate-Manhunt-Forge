package net.laserdiamond.reversemanhunt.network.packet.hunter;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class HunterChangeC2SPacket extends NetworkPacket {

    private boolean hunter;
    private boolean buffed;

    public HunterChangeC2SPacket(boolean hunter, boolean buffed)
    {
        this.hunter = hunter;
        this.buffed = buffed;
    }

    public HunterChangeC2SPacket(FriendlyByteBuf buf) {}

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        final ServerPlayer serverPlayer = context.getSender();
        if (serverPlayer == null)
        {
            return;
        }

        serverPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
        {
            playerHunter.setHunter(this.hunter);
            playerHunter.setBuffed(this.buffed);
        });

    }
}
