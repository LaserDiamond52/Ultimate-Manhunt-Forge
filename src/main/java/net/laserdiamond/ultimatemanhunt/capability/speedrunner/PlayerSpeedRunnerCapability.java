package net.laserdiamond.ultimatemanhunt.capability.speedrunner;

import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.AbstractCapability;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class PlayerSpeedRunnerCapability extends AbstractCapability<PlayerSpeedRunner> {

    public static Capability<PlayerSpeedRunner> PLAYER_SPEED_RUNNER = CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player player)
        {
            if (!player.getCapability(PLAYER_SPEED_RUNNER).isPresent())
            {
                event.addCapability(UltimateManhunt.fromRMPath("speed_runner_lives_cap"), new PlayerSpeedRunnerCapability());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event)
    {
        if (event.isWasDeath())
        {
            event.getOriginal().reviveCaps(); // Revive capability

            event.getOriginal().getCapability(PLAYER_SPEED_RUNNER).ifPresent(oldLives ->
                    event.getEntity().getCapability(PLAYER_SPEED_RUNNER).ifPresent(newLives ->
                            newLives.copyFrom(oldLives)));

            event.getOriginal().invalidateCaps(); // Invalidate capability
        }
    }

    private PlayerSpeedRunner playerSpeedRunner = null;

    @Override
    protected PlayerSpeedRunner createCapability()
    {
        if (this.playerSpeedRunner == null)
        {
            this.playerSpeedRunner = new PlayerSpeedRunner();
        }
        return this.playerSpeedRunner;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_SPEED_RUNNER)
        {
            return this.capabilityOptional.cast();
        }
        return LazyOptional.empty();
    }
}
