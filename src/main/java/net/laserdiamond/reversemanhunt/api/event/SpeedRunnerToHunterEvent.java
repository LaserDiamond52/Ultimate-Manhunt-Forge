package net.laserdiamond.reversemanhunt.api.event;

import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.hunter.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.capability.speedrunner.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.item.WindTorchItem;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.RemainingPlayerCountS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterCapabilitySyncS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Event called when a Speed Runner loses all their lives and becomes a hunter
 */
public class SpeedRunnerToHunterEvent extends PlayerEvent {

    private final boolean isBuffedHunter;
    private final boolean wasKilled;

    public SpeedRunnerToHunterEvent(Player player, boolean isBuffedHunter, boolean wasKilled)
    {
        super(player);
        this.isBuffedHunter = isBuffedHunter;
        this.wasKilled = wasKilled;
        if (player.level().isClientSide)
        {
            return;
        }
        player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER).ifPresent(playerSpeedRunner ->
        {
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                playerHunter.setHunter(true);
                playerHunter.setBuffed(this.isBuffedHunter);
                RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
                RMPackets.sendToAllTrackingEntityAndSelf(new HunterCapabilitySyncS2CPacket(player.getId(), playerHunter.toNBT()), player);

                if (this.wasKilled)
                {
                    player.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Hunter!")); // Tell player they are now a hunter
                }

                // Hunters should not have Wind Torches
                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());

                RMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket(PlayerSpeedRunner.getRemainingSpeedRunners().size(), PlayerHunter.getHunters().size()));
            });
        });

    }


}
