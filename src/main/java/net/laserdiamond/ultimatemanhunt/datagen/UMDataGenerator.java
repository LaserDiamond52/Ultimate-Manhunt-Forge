package net.laserdiamond.ultimatemanhunt.datagen;

import net.laserdiamond.laserutils.datagen.LUDataGenerator;
import net.laserdiamond.laserutils.datagen.LULanguageProvider;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class UMDataGenerator extends LUDataGenerator<UMDataGenerator> {

    public UMDataGenerator(IEventBus eventBus) {
        super(UltimateManhunt.MODID, eventBus);
    }

    @Override
    protected void additionalGatherData(GatherDataEvent event)
    {
        DataGenerator dataGenerator = event.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookUpProvider = event.getLookupProvider();

        dataGenerator.addProvider(event.includeClient(), new UMSoundProvider(packOutput, existingFileHelper));
    }

    @Override
    protected @NotNull LULanguageProvider<UMDataGenerator> languageProvider(PackOutput packOutput) {
        return new UMLanguageProvider(packOutput, this, LanguageRegistry.Language.EN_US);
    }
}
