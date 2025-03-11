package net.laserdiamond.reversemanhunt.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.laserdiamond.laserutils.util.registry.LanguageRegistry;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.client.game.ClientGameState;
import net.laserdiamond.reversemanhunt.client.hunter.ClientHunter;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.hunter.ChangeTrackingSpeedRunnerC2SPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RMKeyBindings {

    public static final RMKeyBindings INSTANCE = new RMKeyBindings();

    private static final String DESCRIPTION_PREFIX = "key." + ReverseManhunt.MODID + ".";
    public static final String CATEGORY = "key.categories." + ReverseManhunt.MODID;

    public final KeyMapping cycleRight;
    public final KeyMapping cycleLeft;

    private RMKeyBindings()
    {
        this.cycleRight = registerKeyMapping("Track Next Speed Runner", "cycle_right_speed_runner", KeyConflictContext.IN_GAME, InputConstants.KEY_RIGHT);
        this.cycleLeft = registerKeyMapping("Track Previous Speed Runner", "cycle_left_speed_runner", KeyConflictContext.IN_GAME, InputConstants.KEY_LEFT);
    }

    public static KeyMapping registerKeyMapping(String name, String description, KeyConflictContext keyConflictContext, int keyInputConstant)
    {
        KeyMapping keyMapping = new KeyMapping(DESCRIPTION_PREFIX + description, keyConflictContext, InputConstants.getKey(keyInputConstant, -1), CATEGORY);
        LanguageRegistry.instance(ReverseManhunt.MODID, LanguageRegistry.Language.EN_US).keyMappingNameRegistry.addEntry(keyMapping, name);
        return keyMapping;
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event)
    {
        event.register(INSTANCE.cycleRight);
        event.register(INSTANCE.cycleLeft);
    }

    @Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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
            if (ClientHunter.isHunter()) // Player can only cycle through players to track if they are a hunter
            {
                if (INSTANCE.cycleRight.consumeClick())
                {
                    RMPackets.sendToServer(new ChangeTrackingSpeedRunnerC2SPacket(TrackCycleDirection.NEXT));
                } else if (INSTANCE.cycleLeft.consumeClick())
                {
                    RMPackets.sendToServer(new ChangeTrackingSpeedRunnerC2SPacket(TrackCycleDirection.PREVIOUS));
                }
            }
        }
    }

    public enum TrackCycleDirection
    {
        NEXT,
        PREVIOUS;
    }
}
