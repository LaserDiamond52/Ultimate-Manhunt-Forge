package net.laserdiamond.reversemanhunt.datagen;

import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.RegistryObject;

public class RMSoundProvider extends SoundDefinitionsProvider {

    protected RMSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ReverseManhunt.MODID, helper);
    }

    @Override
    public void registerSounds()
    {
        for (RegistryObject<SoundEvent> soundEventRegistryObject : RMSoundEvents.SOUNDS.getEntries())
        {
            SoundEvent soundEvent = soundEventRegistryObject.get();
            String subtitle = this.subtitle(soundEventRegistryObject);
            String name = this.soundName(soundEventRegistryObject);
            this.add(soundEvent, SoundDefinition.definition()
                    .subtitle(subtitle)
                    .with(sound(ReverseManhunt.fromRMPath(name))));
        }
    }

    private String soundName(RegistryObject<SoundEvent> sound)
    {
        return sound.getId().toString().replace(":", "").replace(ReverseManhunt.MODID, "");
    }

    private String subtitle(RegistryObject<SoundEvent> sound)
    {
        return "sound." + sound.getId().toString().replace(":", ".");
    }
}
