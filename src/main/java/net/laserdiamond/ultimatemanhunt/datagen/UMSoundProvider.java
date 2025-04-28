package net.laserdiamond.ultimatemanhunt.datagen;

import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.RegistryObject;

public class UMSoundProvider extends SoundDefinitionsProvider {

    protected UMSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, UltimateManhunt.MODID, helper);
    }

    @Override
    public void registerSounds()
    {
        for (RegistryObject<SoundEvent> soundEventRegistryObject : UMSoundEvents.SOUNDS.getEntries())
        {
            SoundEvent soundEvent = soundEventRegistryObject.get();
            String subtitle = this.subtitle(soundEventRegistryObject);
            String name = this.soundName(soundEventRegistryObject);
            this.add(soundEvent, SoundDefinition.definition()
                    .subtitle(subtitle)
                    .with(sound(UltimateManhunt.fromUMPath(name))));
        }
    }

    private String soundName(RegistryObject<SoundEvent> sound)
    {
        return sound.getId().toString().replace(":", "").replace(UltimateManhunt.MODID, "");
    }

    private String subtitle(RegistryObject<SoundEvent> sound)
    {
        return "sound." + sound.getId().toString().replace(":", ".");
    }
}
