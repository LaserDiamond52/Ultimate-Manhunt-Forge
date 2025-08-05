package net.laserdiamond.ultimatemanhunt.event;

import net.laserdiamond.laserutils.client.render.RenderLayers;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.client.hud.*;
import net.laserdiamond.ultimatemanhunt.client.layers.SpeedRunnerGracePeriodLayer;
import net.laserdiamond.ultimatemanhunt.client.models.GracePeriodArmorModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.List;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UMClientModEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(SpeedRunnerGracePeriodLayer.MODEL_LAYER_LOCATION, GracePeriodArmorModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event)
    {
        RenderLayers.addPlayerRenderLayer(List.of(
                SpeedRunnerGracePeriodLayer::new
        ));
    }

    @SubscribeEvent
    public static void addHUDOverlays(RegisterGuiOverlaysEvent event)
    {
        event.registerBelowAll("hunter_tracker", new HunterTrackerOverlay());
        event.registerBelowAll("speed_runner_lives", new SpeedRunnerLivesOverlay());
        event.registerBelowAll("upper_text", new UpperScreenTextOverlay());
        event.registerBelowAll("speed_runner_hunter_detection", new SpeedRunnerHunterDetectionOverlay());
        event.registerBelowAll("speed_runner_grace_period", new SpeedRunnerGracePeriodOverlay());
    }
}
