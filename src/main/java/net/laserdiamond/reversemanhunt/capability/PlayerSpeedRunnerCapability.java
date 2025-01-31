package net.laserdiamond.reversemanhunt.capability;

import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class PlayerSpeedRunnerCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerSpeedRunner> PLAYER_SPEED_RUNNER_LIVES = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player player)
        {
            if (!player.getCapability(PLAYER_SPEED_RUNNER_LIVES).isPresent())
            {
                event.addCapability(ReverseManhunt.fromRMPath("speed_runner_lives_cap"), new PlayerSpeedRunnerCapability());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            event.getOriginal().reviveCaps(); // Revive capability

            event.getOriginal().getCapability(PLAYER_SPEED_RUNNER_LIVES).ifPresent(oldLives ->
                    event.getEntity().getCapability(PLAYER_SPEED_RUNNER_LIVES).ifPresent(newLives ->
                            newLives.copyFrom(oldLives)));

            event.getOriginal().invalidateCaps(); // Invalidate capability
        }
    }

    private PlayerSpeedRunner playerSpeedRunner = null;

    private final LazyOptional<PlayerSpeedRunner> optional = LazyOptional.of(this::createPlayerSpeedRunnerLives);

    private PlayerSpeedRunner createPlayerSpeedRunnerLives()
    {
        if (this.playerSpeedRunner == null)
        {
            this.playerSpeedRunner = new PlayerSpeedRunner();
        }
        return this.playerSpeedRunner;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_SPEED_RUNNER_LIVES)
        {
            return this.optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess)
    {
        CompoundTag nbt = new CompoundTag();
        this.createPlayerSpeedRunnerLives().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt)
    {
        this.createPlayerSpeedRunnerLives().loadNBTData(nbt);
    }
}
