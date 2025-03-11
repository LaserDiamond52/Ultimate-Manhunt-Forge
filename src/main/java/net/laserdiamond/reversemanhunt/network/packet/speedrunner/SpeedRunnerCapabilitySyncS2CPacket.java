package net.laserdiamond.reversemanhunt.network.packet.speedrunner;

import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.network.packet.CapabilitySyncS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class SpeedRunnerCapabilitySyncS2CPacket extends CapabilitySyncS2CPacket {

    public SpeedRunnerCapabilitySyncS2CPacket(int entityId, CompoundTag nbtTag)
    {
        super(entityId, nbtTag);
    }

    public SpeedRunnerCapabilitySyncS2CPacket(FriendlyByteBuf buf) {
        super(buf);
    }

    @Override
    public void packetWork(CustomPayloadEvent.Context context)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
        {
            return;
        }
        Level level = player.level();
        Entity trackedEntity = level.getEntity(this.entityId);
        if (trackedEntity instanceof Player trackedPlayer)
        {
            trackedPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
            {
                playerSpeedRunner.loadNBTData(this.nbtTag);
            });
        }
    }
}
