package net.laserdiamond.ultimatemanhunt.network.packet;

import net.laserdiamond.laserutils.network.CapabilitySyncS2CPacket;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;

public class UMCapabilitySyncS2CPacket extends CapabilitySyncS2CPacket<UMPlayer> {

    public UMCapabilitySyncS2CPacket(Entity entity, UMPlayer capability)
    {
        super(entity, capability);
    }

    public UMCapabilitySyncS2CPacket(FriendlyByteBuf buf)
    {
        super(buf);
    }

    @Override
    protected Capability<UMPlayer> capability() {
        return UMPlayerCapability.UM_PLAYER;
    }
}
