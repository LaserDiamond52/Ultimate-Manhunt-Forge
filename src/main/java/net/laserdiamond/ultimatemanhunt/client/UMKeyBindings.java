package net.laserdiamond.ultimatemanhunt.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameState;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.ChangeTrackingSpeedRunnerC2SPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UMKeyBindings {

    public static final UMKeyBindings INSTANCE = new UMKeyBindings();

    private static final String DESCRIPTION_PREFIX = "key." + UltimateManhunt.MODID + ".";
    public static final String CATEGORY = "key.categories." + UltimateManhunt.MODID;

    public final KeyMapping cycleRight;
    public final KeyMapping cycleLeft;
    public final KeyMapping refreshTracker;

    private UMKeyBindings()
    {
        this.cycleRight = registerKeyMapping("Track Next Speed Runner", "cycle_right_speed_runner", KeyConflictContext.IN_GAME, InputConstants.KEY_RIGHT);
        this.cycleLeft = registerKeyMapping("Track Previous Speed Runner", "cycle_left_speed_runner", KeyConflictContext.IN_GAME, InputConstants.KEY_LEFT);
        this.refreshTracker = registerKeyMapping("Refresh Tracker", "refresh_tracker", KeyConflictContext.IN_GAME, InputConstants.KEY_G);
    }

    private static KeyMapping registerKeyMapping(String name, String description, KeyConflictContext keyConflictContext, int keyInputConstant)
    {
        KeyMapping keyMapping = new KeyMapping(DESCRIPTION_PREFIX + description, keyConflictContext, InputConstants.getKey(keyInputConstant, -1), CATEGORY);
        LanguageRegistry.instance(UltimateManhunt.MODID, LanguageRegistry.Language.EN_US).keyMappingNameRegistry.addEntry(keyMapping, name);
        return keyMapping;
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event)
    {
        event.register(INSTANCE.cycleRight);
        event.register(INSTANCE.cycleLeft);
        event.register(INSTANCE.refreshTracker);
    }

    @Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class KeyInputEvents
    {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event)
        {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localPlayer = minecraft.player;
            if (localPlayer == null)
            {
                return;
            }
            if (!ClientGameState.hasGameBeenStarted())
            {
                return; // Game has not been started. End method
            }
            localPlayer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (umPlayer.isHunter()) // Player can only cycle through players to track or refresh their tracker if they are a hunter
                {
                    if (INSTANCE.cycleRight.consumeClick())
                    {
                        UMPackets.sendToServer(new ChangeTrackingSpeedRunnerC2SPacket(TrackCycleDirection.NEXT));
                    } else if (INSTANCE.cycleLeft.consumeClick())
                    {
                        UMPackets.sendToServer(new ChangeTrackingSpeedRunnerC2SPacket(TrackCycleDirection.PREVIOUS));
                    } else if (INSTANCE.refreshTracker.consumeClick())
                    {
                        List<Player> trackablePlayers = UMPlayer.getAvailableSpeedRunners(localPlayer);
                        if (!trackablePlayers.isEmpty())
                        {
                            umPlayer.setPlayerToTrack(0, trackablePlayers.get(0));
                            localPlayer.sendSystemMessage(Component.literal("Tracker reset!"));
                        }
                    }
                }
            });
        }
    }

    public enum TrackCycleDirection
    {
        NEXT,
        PREVIOUS;
    }
}
