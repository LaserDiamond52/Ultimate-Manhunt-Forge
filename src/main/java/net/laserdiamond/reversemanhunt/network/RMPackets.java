package net.laserdiamond.reversemanhunt.network;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.laserutils.network.NetworkPackets;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.network.packet.game.GameEndAnnounceS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.GameStateS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.GameTimeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.HardcoreUpdateS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.*;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RMPackets {

    @SubscribeEvent
    public static void registerPackets(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> registerPackets());
    }

    public static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id()
    {
        return packetId++;
    }

    private static void registerPackets()
    {
        INSTANCE = ChannelBuilder.named(ReverseManhunt.fromRMPath("main"))
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
        // Speed Runner life change client to server
        registerPacket(SpeedRunnerLifeChangeC2SPacket.class, SpeedRunnerLifeChangeC2SPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Speed Runner life change server to client
        registerPacket(SpeedRunnerLifeChangeS2CPacket.class, SpeedRunnerLifeChangeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner hunter detection server to client
        registerPacket(HunterDetectionS2CPacket.class, HunterDetectionS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner distance from hunter server to client
        registerPacket(CloseDistanceFromHunterS2CPacket.class, CloseDistanceFromHunterS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner Grace Period server to client
        registerPacket(SpeedRunnerGracePeriodDurationS2CPacket.class, SpeedRunnerGracePeriodDurationS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner Capability tracking
        registerPacket(SpeedRunnerCapabilitySyncS2CPacket.class, SpeedRunnerCapabilitySyncS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void registerHunterPackets()
    {
        // Hunter Change client to server
        registerPacket(HunterChangeC2SPacket.class, HunterChangeC2SPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Hunter Change server to client
        registerPacket(HunterChangeS2CPacket.class, HunterChangeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Speed Runner distance server to client
        registerPacket(ClosestSpeedRunnerS2CPacket.class, ClosestSpeedRunnerS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter release announcement server to client
        registerPacket(HunterReleaseAnnounceS2CPacket.class, HunterReleaseAnnounceS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter Grace Period server to client
        registerPacket(HunterGracePeriodDurationS2CPacket.class, HunterGracePeriodDurationS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter Capability tracking
        registerPacket(HunterCapabilitySyncS2CPacket.class, HunterCapabilitySyncS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static void registerGamePackets()
    {
        // Game State server to client
        registerPacket(GameStateS2CPacket.class, GameStateS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game Time server to client
        registerPacket(GameTimeS2CPacket.class, GameTimeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Game End Announcement server to client
        registerPacket(GameEndAnnounceS2CPacket.class, GameEndAnnounceS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hardcore update server to client
        registerPacket(HardcoreUpdateS2CPacket.class, HardcoreUpdateS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);
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
        INSTANCE.send(message, PacketDistributor.TRACKING_ENTITY.with(trackedEntity));
    }

    /**
     * Sends a {@link MSG} to all tracking the {@code trackedEntity} and the client
     * @param message The {@link MSG} to send
     * @param trackedEntity The {@linkplain Entity entity} being tracked
     * @param <MSG> The {@link MSG} type to send
     */
    public static <MSG> void sendToAllTrackingEntityAndSelf(MSG message, Entity trackedEntity)
    {
        INSTANCE.send(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(trackedEntity));
    }
}
