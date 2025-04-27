package net.laserdiamond.ultimatemanhunt;

import com.mojang.logging.LogUtils;
import net.laserdiamond.laserutils.client.overlay.LUOverlayManager;
import net.laserdiamond.ultimatemanhunt.client.hud.*;
import net.laserdiamond.ultimatemanhunt.datagen.UMDataGenerator;
import net.laserdiamond.ultimatemanhunt.item.UMItems;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(UltimateManhunt.MODID)
public class UltimateManhunt {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "ultimate_manhunt";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation fromUMPath(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public UltimateManhunt(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        this.register(modEventBus);
    }

    private void register(IEventBus eventBus)
    {
        new UMDataGenerator(eventBus);
        UMItems.register(eventBus);
        UMSoundEvents.registerSounds(eventBus);
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

    public static ServerLevel getServerLevel(Player player)
    {
        Level level  = getLevel(player);
        if (level != null)
        {
            MinecraftServer mcs = level.getServer();
            if (mcs != null)
            {
                return mcs.getLevel(level.dimension());
            }
        }
        return null;
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Client
    {

        @SubscribeEvent
        public static void onClientSetUp(FMLClientSetupEvent event)
        {
            new LUOverlayManager()
                    .registerConditionalOverlayFirst(new LayeredDraw()
                                    .add(new SpeedRunnerLivesOverlay())
                                    .add(new UpperScreenTextOverlay())
                                    .add(new HunterTrackerOverlay()),
                            () -> !Minecraft.getInstance().options.hideGui)
                    .registerOverlayFirst(new SpeedRunnerHunterDetectionOverlay())
                    .registerOverlayFirst(new SpeedRunnerGracePeriodOverlay())
                    .clientSetUp(event);
        }
    }

}
