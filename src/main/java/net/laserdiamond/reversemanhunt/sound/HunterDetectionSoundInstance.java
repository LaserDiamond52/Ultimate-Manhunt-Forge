package net.laserdiamond.reversemanhunt.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class HunterDetectionSoundInstance extends AbstractSoundInstance {
    
    public HunterDetectionSoundInstance() 
    {
        super(RMSoundEvents.HUNTER_DETECTED.get(), SoundSource.MUSIC, RandomSource.create());
    }
}
