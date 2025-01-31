package net.laserdiamond.reversemanhunt;

import com.mojang.logging.LogUtils;
import net.laserdiamond.reversemanhunt.item.RMItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ReverseManhunt.MODID)
public class ReverseManhunt {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "reverse_manhunt";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation fromRMPath(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public ReverseManhunt(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        this.register(modEventBus);
    }

    private void register(IEventBus eventBus)
    {
        RMItems.register(eventBus);
    }

    public static Level getLevel(Player player)
    {
        Level ret = null;
        try (Level level = player.level())
        {
            ret = level;
        } catch (IOException e) {
            LOGGER.info("Something went wrong getting player " + player.getDisplayName() + "'s level");
            e.printStackTrace();
        }
        return ret;
    }

}
