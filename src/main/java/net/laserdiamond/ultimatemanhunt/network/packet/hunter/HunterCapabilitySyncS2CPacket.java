package net.laserdiamond.ultimatemanhunt.network.packet.hunter;

import net.laserdiamond.ultimatemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.ultimatemanhunt.network.packet.CapabilitySyncS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class HunterCapabilitySyncS2CPacket extends CapabilitySyncS2CPacket {

    public HunterCapabilitySyncS2CPacket(int entityId, CompoundTag nbtTag)
    {
        super(entityId, nbtTag);
    }

    public HunterCapabilitySyncS2CPacket(FriendlyByteBuf buf) {
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
            trackedPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                playerHunter.loadNBTData(this.nbtTag);
            });
        }
    }
}
