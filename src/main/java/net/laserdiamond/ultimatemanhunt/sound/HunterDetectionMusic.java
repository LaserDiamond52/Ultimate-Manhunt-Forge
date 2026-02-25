package net.laserdiamond.ultimatemanhunt.sound;

import net.laserdiamond.ultimatemanhunt.client.ClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;

public class HunterDetectionMusic {

    /**
     * Ensures that the Hunter Detection Music is already playing on the given SoundSource.
     * The music will not play if the player has their {@linkplain net.laserdiamond.ultimatemanhunt.client.ClientSettings.SoundController#isEnabled() hunter music muted}
     */
    public static void ensureLooping()
    {
        if (!ClientSettings.PROWLER_VOLUME.isEnabled())
        {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        boolean alreadyPlaying = soundManager.soundEngine.instanceToChannel.keySet().stream()
                .anyMatch(soundInstance ->
                        soundInstance.getLocation().equals(UMSoundEvents.HUNTER_DETECTED.get().getLocation())
                );

        if (!alreadyPlaying && minecraft.player != null)
        {
            minecraft.player.playSound(UMSoundEvents.HUNTER_DETECTED.get(), ClientSettings.PROWLER_VOLUME.getValue(), 1.0F);
        }
    }
}
