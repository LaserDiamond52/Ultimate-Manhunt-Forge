package net.laserdiamond.reversemanhunt.datagen;

import net.laserdiamond.laserutils.datagen.LUItemModelProvider;
import net.laserdiamond.reversemanhunt.item.RMItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public final class RMItemModelProvider extends LUItemModelProvider<RMDataGenerator> {

    public RMItemModelProvider(PackOutput output, RMDataGenerator dataGenerator, ExistingFileHelper existingFileHelper) {
        super(output, dataGenerator, existingFileHelper);
    }

    @Override
    public void createOther(RegistryObject<Item> registryObject)
    {
        if (registryObject.equals(RMItems.KNOCKBACK_STICK))
        {
            this.mcLocModel(registryObject, "item/handheld");
        }
    }
}
