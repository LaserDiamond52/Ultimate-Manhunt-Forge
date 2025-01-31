package net.laserdiamond.reversemanhunt.network;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.laserutils.network.NetworkPackets;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.network.packet.HunterChangeC2SPacket;
import net.laserdiamond.reversemanhunt.network.packet.HunterChangeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.SpeedRunnerLifeChangeC2SPacket;
import net.laserdiamond.reversemanhunt.network.packet.SpeedRunnerLifeChangeS2CPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RMPackets {

    @SubscribeEvent
    public static void registerPackets(FMLCommonSetupEvent event)
    {
        RMPackets.registerPackets();
    }

    public static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id()
    {
        return packetId++;
    }

    public static void registerPackets()
    {
        INSTANCE = ChannelBuilder.named(ReverseManhunt.fromRMPath("main"))
                .serverAcceptedVersions((status, version) -> true)
                .clientAcceptedVersions((status, version) -> true)
                .networkProtocolVersion(1)
                .simpleChannel();

        // Speed Runner client to server
        registerPacket(SpeedRunnerLifeChangeC2SPacket.class, SpeedRunnerLifeChangeC2SPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Speed Runner server to client
        registerPacket(SpeedRunnerLifeChangeS2CPacket.class, SpeedRunnerLifeChangeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);

        // Hunter Change client to server
        registerPacket(HunterChangeC2SPacket.class, HunterChangeC2SPacket::new, NetworkDirection.PLAY_TO_SERVER);

        // Hunter Change server to client
        registerPacket(HunterChangeS2CPacket.class, HunterChangeS2CPacket::new, NetworkDirection.PLAY_TO_CLIENT);
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

}
