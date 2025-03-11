package net.laserdiamond.reversemanhunt.datagen;

import net.laserdiamond.laserutils.datagen.LULanguageProvider;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.reversemanhunt.client.RMKeyBindings;
import net.minecraft.data.PackOutput;

public class RMLanguageProvider extends LULanguageProvider<RMDataGenerator> {

    public RMLanguageProvider(PackOutput output, RMDataGenerator dataGenerator, LanguageRegistry.LanguageType language) {
        super(output, dataGenerator, language);
    }

    @Override
    protected void addAdditionalTranslations() {
        this.add(RMKeyBindings.CATEGORY, "Reverse Manhunt");
    }
}
