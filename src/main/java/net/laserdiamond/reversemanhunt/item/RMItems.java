package net.laserdiamond.reversemanhunt.item;

import net.laserdiamond.laserutils.util.registry.ObjectRegistry;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class RMItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ReverseManhunt.MODID);

    public static final RegistryObject<Item> WIND_TORCH = registerItem("Wind Torch", "wind_torch", () -> new WindTorchItem(new Item.Properties().fireResistant()));

    private static RegistryObject<Item> registerItem(String name, String localName, Supplier<Item> itemSupplier)
    {
        return ObjectRegistry.registerItem(ReverseManhunt.MODID, ITEMS, name, localName, itemSupplier);
    }

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
