package net.laserdiamond.ultimatemanhunt.capability.game;

import net.laserdiamond.ultimatemanhunt.capability.AbstractCapabilityData;
import net.minecraft.nbt.CompoundTag;

public class PlayerGameTime extends AbstractCapabilityData<PlayerGameTime> {

    private long gameTime;

    public PlayerGameTime()
    {
        this.gameTime = 0;
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    @Override
    public void copyFrom(PlayerGameTime source)
    {
        this.gameTime = source.gameTime;
    }

    @Override
    public void saveNBTData(CompoundTag nbt)
    {
        nbt.putLong("rm_game_time", this.getGameTime());
    }

    @Override
    public void loadNBTData(CompoundTag nbt)
    {
        this.gameTime = nbt.getLong("rm_game_time");
    }
}
