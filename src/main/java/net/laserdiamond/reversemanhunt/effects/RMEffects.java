package net.laserdiamond.reversemanhunt.effects;

import net.laserdiamond.laserutils.util.registry.ObjectRegistry;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class RMEffects {

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ReverseManhunt.MODID);



    public static RegistryObject<MobEffect> registerEffect(String name, String localName, Supplier<MobEffect> mobEffectSupplier)
    {
        return ObjectRegistry.registerMobEffect(ReverseManhunt.MODID, EFFECTS, name, localName, mobEffectSupplier);
    }

    public static void registerEffects(IEventBus eventBus)
    {
        EFFECTS.register(eventBus);
    }
}
