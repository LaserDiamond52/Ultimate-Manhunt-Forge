package net.laserdiamond.ultimatemanhunt.network;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.laserutils.network.NetworkPackets;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.network.packet.UMCapabilitySyncS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.*;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameEndAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GamePausedAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameResumedS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.announce.GameStartAnnounceS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.*;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UMPackets {

    @SubscribeEvent
    public static void registerPackets(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> registerPackets());
    }

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id()
    {
        return packetId++;
    }

    private static void registerPackets()
    {
        INSTANCE = ChannelBuilder.named(UltimateManhunt.fromUMPath("main"))
                .serverAcceptedVersions((status, version) -> true)
                .clientAcceptedVersions((status, version) -> true)
                .networkProtocolVersion(1)
                .simpleChannel();

        registerGamePackets();
        registerSpeedRunnerPackets();
        registerHunterPackets();
    }

    private static void registerSpeedRunnerPackets()
    {
        // Speed Runner distance from hunter server to client
        registerPacket(SpeedRunnerDistanceFromHunterS2CPacket.class, SpeedRunnerDistanceFromHunterS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner Grace Period server to client
        registerPacket(SpeedRunnerGracePeriodDurationS2CPacket.class, SpeedRunnerGracePeriodDurationS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner max lives server to client
        registerPacket(SpeedRunnerMaxLifeChangeS2CPacket.class, SpeedRunnerMaxLifeChangeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void registerHunterPackets()
    {
        // Speed Runner distance server to client
        registerPacket(TrackingSpeedRunnerS2CPacket.class, TrackingSpeedRunnerS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter release announcement server to client
        registerPacket(HunterReleaseAnnounceS2CPacket.class, HunterReleaseAnnounceS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter Grace Period server to client
        registerPacket(HunterGracePeriodDurationS2CPacket.class, HunterGracePeriodDurationS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter change tracking speed runner server to client
        registerPacket(ChangeTrackingSpeedRunnerC2SPacket.class, ChangeTrackingSpeedRunnerC2SPacket::new, NetworkDirection.PLAY_TO_SERVER);
    }

    private static void registerGamePackets()
    {
        // UM Player Capability Sync
        registerPacket(UMCapabilitySyncS2CPacket.class, UMCapabilitySyncS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game State server to client
        registerPacket(GameStateS2CPacket.class, GameStateS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game Time server to client
        registerPacket(GameTimeS2CPacket.class, GameTimeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game Start Announce server to client
        registerPacket(GameStartAnnounceS2CPacket.class, GameStartAnnounceS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game Pause Announce server to client
        registerPacket(GamePausedAnnounceS2CPacket.class, GamePausedAnnounceS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game Resume Announce server to client
        registerPacket(GameResumedS2CPacket.class, GameResumedS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game End Announce server to client
        registerPacket(GameEndAnnounceS2CPacket.class, GameEndAnnounceS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hardcore update server to client
        registerPacket(HardcoreUpdateS2CPacket.class, HardcoreUpdateS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Remaining Speed Runner and Hunter count server to client
        registerPacket(RemainingPlayerCountS2CPacket.class, RemainingPlayerCountS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <P extends NetworkPacket> void registerPacket(Class<P> packetClazz, Function<RegistryFriendlyByteBuf, P> decoder, NetworkDirection<RegistryFriendlyByteBuf> direction)
    {
        NetworkPackets.registerPacket(INSTANCE, id(), packetClazz, decoder, direction);
    }

    public static <MSG> void sendToServer(MSG message)
    {
        NetworkPackets.sendToServer(INSTANCE, message);
    }

    public static <MSG> void sendToPlayer(MSG message, Player player)
    {
        NetworkPackets.sendToPlayer(INSTANCE, message, (ServerPlayer) player);
    }

    public static <MSG> void sendToAllClients(MSG message)
    {
        NetworkPackets.sendToAllClients(INSTANCE, message);
    }

    /**
     * Sends a {@link MSG} to all tracking the {@code trackedEntity}
     * @param message The {@link MSG} to send
     * @param trackedEntity The {@linkplain Entity entity} being tracked
     * @param <MSG> The {@link MSG} type to send
     */
    public static <MSG> void sendToAllTrackingEntity(MSG message, Entity trackedEntity)
    {
        NetworkPackets.sendToAllTrackingEntity(INSTANCE, message, trackedEntity);
    }

    /**
     * Sends a {@link MSG} to all tracking the {@code trackedEntity} and the client
     * @param message The {@link MSG} to send
     * @param trackedEntity The {@linkplain Entity entity} being tracked
     * @param <MSG> The {@link MSG} type to send
     */
    public static <MSG> void sendToAllTrackingEntityAndSelf(MSG message, Entity trackedEntity)
    {
        NetworkPackets.sendToAllTrackingEntityAndSelf(INSTANCE, message, trackedEntity);
    }
}
