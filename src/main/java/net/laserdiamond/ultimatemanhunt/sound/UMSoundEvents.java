package net.laserdiamond.ultimatemanhunt.sound;

import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.laserutils.util.registry.ObjectRegistry;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.UpdateLoopedHunterMusicS2CPacket;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

public class UMSoundEvents {

    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, UltimateManhunt.MODID);

    public static RegistryObject<SoundEvent> GAME_START = registerSound("Ultimate Manhunt Game Started", "game_start");

    public static RegistryObject<SoundEvent> GAME_PAUSE = registerSound("Ultimate Manhunt Game Paused", "game_pause");

    public static RegistryObject<SoundEvent> GAME_RESUME = registerSound("Ultimate Manhunt Game Resumed", "game_resume");

    public static RegistryObject<SoundEvent> GAME_END = registerSound("Ultimate Manhunt Game Ended", "game_end");

    public static RegistryObject<SoundEvent> HUNTER_RELEASED = registerSound("Ultimate Manhunt Hunter Released Sound", "hunters_released");

    public static RegistryObject<SoundEvent> HEART_BEAT = registerSound("Heart Beating", "heart_beat");

    public static RegistryObject<SoundEvent> HEAT_BEAT_FLATLINE = registerSound("Heart Flatline", "heart_beat_flatline");

    public static RegistryObject<SoundEvent> HUNTER_DETECTED = registerSound("Hunter Nearby", "hunter_detected");

    private static RegistryObject<SoundEvent> registerSound(String name, String path)
    {
        ObjectRegistry.languageRegistry(UltimateManhunt.MODID, LanguageRegistry.Language.EN_US).additionalNamesRegistry.addEntry(createPath(path), name);
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(UltimateManhunt.fromUMPath(path)));
    }

    private static String createPath(String path)
    {
        return "sound." + UltimateManhunt.MODID + "." + path;
    }

    public static void registerSounds(IEventBus eventBus)
    {
        SOUNDS.register(eventBus);
    }

    public static void playGameStartSound(Player player)
    {
        playSound(player, GAME_START.getHolder(), SoundSource.MASTER);
    }

    public static void playGamePauseSound(Player player)
    {
        playSound(player, GAME_PAUSE.getHolder(), SoundSource.MASTER);
    }

    public static void playGameResumeSound(Player player)
    {
        playSound(player, GAME_RESUME.getHolder(), SoundSource.MASTER);
    }

    public static void playGameEndSound(Player player)
    {
        playSound(player, GAME_END.getHolder(), SoundSource.MASTER);
    }

    public static void playerHuntersReleasedSound(Player player)
    {
        playSound(player, HUNTER_RELEASED.getHolder(), SoundSource.MASTER);
    }

    public static void playHeartBeatSound(Player player)
    {
        playSound(player, HEART_BEAT.getHolder(), SoundSource.PLAYERS);
    }

    public static void playDetectionSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            UpdateLoopedHunterMusicS2CPacket.updateLoopedHunterMusic(serverPlayer, UpdateLoopedHunterMusicS2CPacket.Operation.START);
        }
    }

    public static void stopDetectionSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            UpdateLoopedHunterMusicS2CPacket.updateLoopedHunterMusic(serverPlayer, UpdateLoopedHunterMusicS2CPacket.Operation.STOP);
        }
    }

    public static void playFlatlineSound(Player player)
    {
        playSound(player, HEAT_BEAT_FLATLINE.getHolder(), SoundSource.PLAYERS);
    }

    public static void stopFlatlineSound(Player player)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            serverPlayer.connection.send(new ClientboundStopSoundPacket(HEAT_BEAT_FLATLINE.getId(), SoundSource.PLAYERS));
        }
    }

    private static void playSound(Player player, Optional<Holder<SoundEvent>> optional, SoundSource source)
    {
        if (optional.isEmpty())
        {
            return;
        }
        Holder<SoundEvent> soundEvent = optional.get();
        if (player instanceof ServerPlayer serverPlayer)
        {
            Level level = serverPlayer.level();
            Vec3 pos = serverPlayer.position();
            serverPlayer.connection.send(new ClientboundSoundPacket(soundEvent, source, pos.x, pos.y, pos.z, 1.0F, 1.0F, level.getRandom().nextLong()));
        }
    }

}
