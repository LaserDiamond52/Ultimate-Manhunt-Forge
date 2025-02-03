package net.laserdiamond.reversemanhunt.sound;

import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RMSoundEvents {

    public static DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReverseManhunt.MODID);

    public static RegistryObject<SoundEvent> HEART_BEAT = registerSound("heart_beat");

    public static RegistryObject<SoundEvent> HUNTER_DETECTED = registerSound("hunter_detected");

    private static RegistryObject<SoundEvent> registerSound(String name)
    {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ReverseManhunt.fromRMPath(name)));
    }

    public static void registerSounds(IEventBus eventBus)
    {
        SOUNDS.register(eventBus);
    }

}
