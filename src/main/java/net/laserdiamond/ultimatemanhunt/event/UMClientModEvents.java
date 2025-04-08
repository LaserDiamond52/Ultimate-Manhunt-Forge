package net.laserdiamond.ultimatemanhunt.event;

import net.laserdiamond.laserutils.client.render.RenderLayers;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.client.layers.SpeedRunnerGracePeriodLayer;
import net.laserdiamond.ultimatemanhunt.client.models.GracePeriodArmorModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

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
}
