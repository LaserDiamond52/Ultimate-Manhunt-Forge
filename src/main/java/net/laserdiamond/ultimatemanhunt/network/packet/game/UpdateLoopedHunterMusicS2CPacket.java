package net.laserdiamond.ultimatemanhunt.network.packet.game;

import net.laserdiamond.laserutils.network.NetworkPacket;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.sound.HunterDetectionMusic;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.network.CustomPayloadEvent;

/**
 * Packet send from Server to Client to play Ultimate Manhunt sounds based on the client's settings
 */
public final class UpdateLoopedHunterMusicS2CPacket extends NetworkPacket {

    public static void updateLoopedHunterMusic(ServerPlayer player, Operation operation)
    {
        if (operation.equals(Operation.START))
        {
            UMPackets.sendToPlayer(new UpdateLoopedHunterMusicS2CPacket(), player);
            return;
        }
        player.connection.send(new ClientboundStopSoundPacket(UMSoundEvents.HUNTER_DETECTED.get().getLocation(), SoundSource.MUSIC));
    }

    private UpdateLoopedHunterMusicS2CPacket() {}

    public UpdateLoopedHunterMusicS2CPacket(FriendlyByteBuf buf) {}

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null)
        {
            return;
        }
        HunterDetectionMusic.ensureLooping();
    }

    public enum Operation
    {
        START,
        STOP;
    }

}
