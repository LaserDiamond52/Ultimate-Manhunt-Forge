package net.laserdiamond.reversemanhunt.datagen;

import net.laserdiamond.laserutils.datagen.LUDataGenerator;
import net.laserdiamond.laserutils.datagen.LUItemModelProvider;
import net.laserdiamond.laserutils.datagen.LULanguageProvider;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.item.RMItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class RMDataGenerator extends LUDataGenerator<RMDataGenerator> {

    public RMDataGenerator(IEventBus eventBus) {
        super(ReverseManhunt.MODID, eventBus);
    }

    @Override
    protected void additionalGatherData(GatherDataEvent event)
    {
        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookUpProvider = event.getLookupProvider();

        dataGenerator.addProvider(event.includeClient(), new RMSoundProvider(packOutput, existingFileHelper));
    }

    @Override
    protected @NotNull LULanguageProvider<RMDataGenerator> languageProvider(PackOutput packOutput) {
        return new LULanguageProvider<>(packOutput, this, LanguageRegistry.Language.EN_US);
    }
}
