package net.laserdiamond.ultimatemanhunt.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public abstract class AbstractCapability<C extends AbstractCapabilityData<C>> implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    protected final LazyOptional<C> capabilityOptional = LazyOptional.of(this::createCapability);

    protected abstract C createCapability();

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess)
    {
        CompoundTag nbt = new CompoundTag();
        this.createCapability().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt)
    {
        this.createCapability().loadNBTData(nbt);
    }
}
