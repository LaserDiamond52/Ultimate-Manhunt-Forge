package net.laserdiamond.reversemanhunt.sound;

import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.HunterDetectionS2CPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.UUID;

public class RMSoundEvents {

    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReverseManhunt.MODID);

    public static RegistryObject<SoundEvent> HEART_BEAT = registerSound("heart_beat");

    public static RegistryObject<SoundEvent> HEAT_BEAT_FLATLINE = registerSound("heart_beat_flatline");

    public static RegistryObject<SoundEvent> HUNTER_DETECTED = registerSound("hunter_detected");

    private static RegistryObject<SoundEvent> registerSound(String name)
    {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ReverseManhunt.fromRMPath(name)));
    }

    public static void registerSounds(IEventBus eventBus)
    {
        SOUNDS.register(eventBus);
    }

    public static void playDetectionSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            SoundManager sm = SoundManager.INSTANCE;
            if (sm.getSoundTime(player) == 0)
            {
                Level level = player.level();
                serverPlayer.connection.send(new ClientboundSoundPacket(HUNTER_DETECTED.getHolder().get(), SoundSource.MUSIC, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
            }
            sm.increment(player);
        }
        RMPackets.sendToPlayer(new HunterDetectionS2CPacket(true), player);
    }

    public static void stopDetectionSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            SoundManager sm = SoundManager.INSTANCE;
            sm.reset(player);
            serverPlayer.connection.send(new ClientboundStopSoundPacket(HUNTER_DETECTED.getId(), SoundSource.MUSIC));
        }
        RMPackets.sendToPlayer(new HunterDetectionS2CPacket(false), player);
    }

    public static void playFlatlineSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            Level level = player.level();
            serverPlayer.connection.send(new ClientboundSoundPacket(HEAT_BEAT_FLATLINE.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
        }
    }

    public static void stopFlatlineSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            Level level = player.level();
            serverPlayer.connection.send(new ClientboundStopSoundPacket(HEAT_BEAT_FLATLINE.getId(), SoundSource.PLAYERS));
        }
    }

    private static class SoundManager
    {
        public static final SoundManager INSTANCE = new SoundManager();
        private static final int SOUND_DURATION_TICKS = 825;
        private final HashMap<UUID, Integer> timings;

        private SoundManager()
        {
            this.timings = new HashMap<>();
        }

        public void increment(Player player)
        {
            if (!this.hasKey(player) || this.timings.get(player.getUUID()) >= SOUND_DURATION_TICKS)
            {
                this.timings.put(player.getUUID(), 0); // Reset time back if time is over or if player has no value
                return;
            }
            Integer value = this.timings.get(player.getUUID());
            if (value != null)
            {
                this.timings.put(player.getUUID(), value + 1);
            }
        }

        public int getSoundTime(Player player)
        {
            if (!this.hasKey(player))
            {
                this.timings.put(player.getUUID(), 0);
            }
            return this.timings.get(player.getUUID());
        }

        public boolean hasKey(Player player)
        {
            return this.timings.get(player.getUUID()) != null && this.timings.containsKey(player.getUUID());
        }

        public void reset(Player player)
        {
            this.timings.put(player.getUUID(), 0);
        }
    }

}
