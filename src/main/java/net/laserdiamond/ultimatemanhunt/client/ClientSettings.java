package net.laserdiamond.ultimatemanhunt.client;

import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;


/**
 * Client settings for controlling volume, vignette effects, and 3D tracker
 */
@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class ClientSettings {

    @SubscribeEvent
    public static void onSoundPlay(PlayLevelSoundEvent event)
    {
        if (!event.getLevel().isClientSide)
        {
            return;
        }
        Holder<SoundEvent> sound = event.getSound();
        if (sound == null)
        {
            return;
        }
        if (modifySound(event, sound, START_GAME_VOLUME)) return;
        if (modifySound(event, sound, PAUSE_GAME_VOLUME)) return;
        if (modifySound(event, sound, RESUME_GAME_VOLUME)) return;
        if (modifySound(event, sound, END_GAME_VOLUME)) return;
        if (modifySound(event, sound, HUNTERS_RELEASED_VOLUME)) return;
        if (modifySound(event, sound, HEART_BEAT_VOLUME)) return;
        if (modifySound(event, sound, HEART_FLATLINE_VOLUME)) return;
        modifySound(event, sound, PROWLER_VOLUME);
    }

    private static boolean modifySound(PlayLevelSoundEvent event, Holder<SoundEvent> soundEventHolder, SoundController soundController)
    {
        ResourceLocation r1 = soundController.getSoundResourceLocation();
        ResourceLocation r2 = soundEventHolder.get().getLocation();
        if (r1.equals(r2))
        {
            if (!soundController.isEnabled())
            {
                event.setCanceled(true);
                return true;
            }
            event.setNewVolume(soundController.getValue());
            return true;
        }
        return false;
    }

    public static final class SoundController extends FloatBooleanController
    {
        private final RegistryObject<SoundEvent> soundEvent;

        private SoundController(float initValue, boolean initEnabled, RegistryObject<SoundEvent> soundEvent)
        {
            super(initValue, initEnabled, adjustVolume(soundEvent), adjustVolume(soundEvent));
            this.soundEvent = soundEvent;
        }

        public SoundEvent getSoundEvent()
        {
            return this.soundEvent.get();
        }

        public ResourceLocation getSoundResourceLocation()
        {
            return this.getSoundEvent().getLocation();
        }
    }

    /**
     * Volume/Vignette Controller. Float value is bounded by [0, 1]
     */
    public static class FloatBooleanController
    {
        private float value;
        private boolean isEnabled;
        private final BiConsumer<Float, Boolean> onValueChange;
        private final BiConsumer<Float, Boolean> onBooleanChange;

        private FloatBooleanController(float initValue, boolean initEnabled)
        {
            this.value = Mth.clamp(initValue, 0f, 1f);
            this.isEnabled = initEnabled;
            this.onValueChange = (aFloat, aBoolean) -> {};
            this.onBooleanChange = (aFloat, aBoolean) -> {};
        }

        private FloatBooleanController(float initValue, boolean initEnabled, BiConsumer<Float, Boolean> onValueChange, BiConsumer<Float, Boolean> onBooleanChange)
        {
            this.value = initValue;
            this.isEnabled = initEnabled;
            this.onValueChange = onValueChange;
            this.onBooleanChange = onBooleanChange;
        }


        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = Mth.clamp(value, 0.0F, 1.0F);
            this.onValueChange.accept(this.value, this.isEnabled);
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
            this.onBooleanChange.accept(this.value, this.isEnabled);
        }
    }

    /**
     * Adjusts the volume of the {@linkplain SoundEvent Sound Event}
     * @param soundEventObj The {@linkplain SoundEvent Sound Event} to target
     * @return A BiConsumer that will take in the new volume level and whether the sound is enabled. If the boolean is true, the volume is set to 0 (sound is still active)
     */
    private static BiConsumer<Float, Boolean> adjustVolume(RegistryObject<SoundEvent> soundEventObj)
    {
        return (aFloat, aBoolean) -> {
            SoundEvent soundEvent = soundEventObj.get();
            SoundManager soundManager = Minecraft.getInstance().getSoundManager();
            SoundEngine soundEngine = soundManager.soundEngine;

            soundEngine.instanceToChannel.forEach((soundInstance, channelHandle) ->
            {
                if (soundInstance.getLocation().equals(soundEvent.getLocation()))
                {
                    channelHandle.execute(channel ->
                    {
                        channel.setVolume(aBoolean ? aFloat : 0); // Muting should just set volume to 0
                    });
                }
            });
        };
    }

    /**
     * Controls for the {@linkplain UMSoundEvents#GAME_START Game Start Sound}
     */
    public static final SoundController START_GAME_VOLUME = new SoundController(1.0f, true, UMSoundEvents.GAME_START);

    /**
     * Controls for the {@linkplain UMSoundEvents#GAME_PAUSE Game Pause Sound}
     */
    public static final SoundController PAUSE_GAME_VOLUME = new SoundController(1.0f, true, UMSoundEvents.GAME_PAUSE);

    /**
     * Controls for the {@linkplain UMSoundEvents#GAME_RESUME Game Resume Sound}
     */
    public static final SoundController RESUME_GAME_VOLUME = new SoundController(1.0f, true, UMSoundEvents.GAME_RESUME);

    /**
     * Controls for the {@linkplain UMSoundEvents#GAME_END Game End Sound}
     */
    public static final SoundController END_GAME_VOLUME = new SoundController(1.0f, true, UMSoundEvents.GAME_END);

    /**
     * Controls for the {@linkplain UMSoundEvents#HUNTER_RELEASED Hunters Released Sound}
     */
    public static final SoundController HUNTERS_RELEASED_VOLUME = new SoundController(1.0f, true, UMSoundEvents.HUNTER_RELEASED);

    /**
     * Controls for the {@linkplain UMSoundEvents#HEART_BEAT Heart Beating Sound}
     */
    public static final SoundController HEART_BEAT_VOLUME = new SoundController(1.0f, true, UMSoundEvents.HEART_BEAT);

    /**
     * Controls for the {@linkplain UMSoundEvents#HEAT_BEAT_FLATLINE Heart Flatlining Sound}
     */
    public static final SoundController HEART_FLATLINE_VOLUME = new SoundController(1.0f, true, UMSoundEvents.HEAT_BEAT_FLATLINE);

    /**
     * Controls for the {@linkplain UMSoundEvents#HUNTER_DETECTED Prowler Theme (Hunter Detection Music)}
     */
    public static final SoundController PROWLER_VOLUME = new SoundController(1.0f, true, UMSoundEvents.HUNTER_DETECTED);

    /**
     * Controls for the {@linkplain net.laserdiamond.ultimatemanhunt.client.hud.SpeedRunnerHunterDetectionOverlay Hunter Detection Vignette}
     */
    public static final FloatBooleanController HUNTER_VIGNETTE = new FloatBooleanController(1.0f, true);

    /**
     * Controls for the {@linkplain net.laserdiamond.ultimatemanhunt.client.hud.SpeedRunnerGracePeriodOverlay Grace Period Vignette}
     */
    public static final FloatBooleanController GRACE_PERIOD_VIGNETTE = new FloatBooleanController(1.0f, true);

    /**
     * Controls for the {@linkplain net.laserdiamond.ultimatemanhunt.client.hud.HunterTrackerOverlay 3D Player Tracker}. Adjusting the float value does nothing.
     */
    public static final FloatBooleanController PLAYER_TRACKER = new FloatBooleanController(0f, true);

}
