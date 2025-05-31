package net.laserdiamond.ultimatemanhunt.event;

import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.api.event.UltimateManhuntGameStateEvent;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ForgeServerEvents {

    private static final ResourceLocation KILL_DRAGON_ADVANCEMENT = ResourceLocation.withDefaultNamespace("end/kill_dragon");

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event)
    {
        Advancement advancement = event.getAdvancement();
        if (advancement != null)
        {
            ResourceLocation advPath = advancement.getId();
            if (advPath.equals(KILL_DRAGON_ADVANCEMENT))
            {
                MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.End(UltimateManhuntGameStateEvent.End.Reason.SPEED_RUNNERS_WIN));
            }
        }
    }



}
