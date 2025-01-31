package net.laserdiamond.reversemanhunt.datagen;

import net.laserdiamond.laserutils.datagen.LUDataGenerator;
import net.laserdiamond.laserutils.datagen.LUItemModelProvider;
import net.laserdiamond.laserutils.datagen.LULanguageProvider;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.reversemanhunt.item.RMItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public final class RMDataGenerator extends LUDataGenerator<RMDataGenerator> {

    public RMDataGenerator(String modId, IEventBus eventBus) {
        super(modId, eventBus);
    }

    @Override
    protected DeferredRegister<Item> itemDeferredRegister() {
        return RMItems.ITEMS;
    }

    @Override
    protected LUItemModelProvider<RMDataGenerator> itemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        return new RMItemModelProvider(packOutput, this, existingFileHelper);
    }

    @Override
    protected @NotNull LULanguageProvider<RMDataGenerator> languageProvider(PackOutput packOutput) {
        return new LULanguageProvider<>(packOutput, this, LanguageRegistry.Language.EN_US);
    }
}
