package net.laserdiamond.ultimatemanhunt.datagen;

import net.laserdiamond.laserutils.datagen.LULanguageProvider;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.ultimatemanhunt.client.UMKeyBindings;
import net.minecraft.data.PackOutput;

public class UMLanguageProvider extends LULanguageProvider<UMDataGenerator> {

    public UMLanguageProvider(PackOutput output, UMDataGenerator dataGenerator, LanguageRegistry.LanguageType language) {
        super(output, dataGenerator, language);
    }

    @Override
    protected void addAdditionalTranslations() {
        this.add(UMKeyBindings.CATEGORY, "Ultimate Manhunt");
    }
}
