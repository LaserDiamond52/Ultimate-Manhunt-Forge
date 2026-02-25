package net.laserdiamond.ultimatemanhunt.event;

import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.util.file.ClientConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, value = Dist.CLIENT)
public class ForgeClientEvents {

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event)
    {
        ClientConfig.getClientConfig().saveSettingsToFile();
    }

}
