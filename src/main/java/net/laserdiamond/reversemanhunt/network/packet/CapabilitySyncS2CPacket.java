package net.laserdiamond.reversemanhunt.network.packet;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public abstract class CapabilitySyncS2CPacket extends NetworkPacket {

    protected final int entityId;
    protected final CompoundTag nbtTag;

    public CapabilitySyncS2CPacket(int entityId, CompoundTag nbtTag)
    {
        this.entityId = entityId;
        this.nbtTag = nbtTag;
    }

    public CapabilitySyncS2CPacket(FriendlyByteBuf buf)
    {
        this.entityId = buf.readInt();
        this.nbtTag = buf.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.nbtTag);
    }
}
