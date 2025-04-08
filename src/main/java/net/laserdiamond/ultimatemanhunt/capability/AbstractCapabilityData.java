package net.laserdiamond.ultimatemanhunt.capability;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractCapabilityData<T> {

    public abstract void copyFrom(T source);

    public abstract void saveNBTData(CompoundTag nbt);

    public abstract void loadNBTData(CompoundTag nbt);

    public final CompoundTag toNBT()
    {
        CompoundTag tag = new CompoundTag();
        this.saveNBTData(tag);
        return tag;
    }
}
