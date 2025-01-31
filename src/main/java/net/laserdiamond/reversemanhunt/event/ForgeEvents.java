package net.laserdiamond.reversemanhunt.event;

import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.commands.ManageHuntersCommand;
import net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.HunterChangeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.SpeedRunnerLifeChangeS2CPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class ForgeEvents {

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {
        if (RMGameState.State.isGameNotInProgress()) // Is a game in progress?
        {
            return; // No game in progress. End method
        }
        LivingEntity living = event.getEntity();
        if (living == null)
        {
            return;
        }
        if (living instanceof Player deadPlayer) // Check if a player died
        {
            Entity sourceEntity = event.getSource().getEntity();
            deadPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                if (!playerHunter.isHunter()) // Was the dead player NOT a hunter?
                {
                    Level level = deadPlayer.level();
                    if (!level.isClientSide)
                    {
                        deadPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                        {
                            playerSpeedRunner.setWasLastKilledByHunter(false); // Assume that the player wasn't killed by the hunter (for now)
                            RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner.getLives(), false), deadPlayer); // Send packet to client from server
                        });

                        if (sourceEntity == null) {
                            return; // End method if killer is null
                        }
                        if (sourceEntity instanceof Player killer) // Is killer a player?
                        {
                            killer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(killerHunter ->
                            {
                                if (killerHunter.isHunter())
                                {
                                    PlayerSpeedRunner.removeSpeedRunnerLife(deadPlayer, true, LogicalSide.SERVER); // Send packet from server to client

                                    deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You were killed by a Hunter and lost a life!"));
                                }
                            });
                        }
                    }
                } else // Player is a hunter
                {
                    if (RMGameState.areHuntersOnGracePeriod()) // Are hunters on grace period?
                    {
                        // On grace period. Cancel death and set health back
                        event.setCanceled(true);
                        deadPlayer.setHealth(deadPlayer.getMaxHealth());
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        ManageHuntersCommand.register(event.getDispatcher());
        ReverseManhuntGameCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event)
    {
        LivingEntity hurtEntity = event.getEntity();
        if (RMGameState.State.isGameNotInProgress()) // Is the game not in progress?
        {
            if (hurtEntity.getType() == EntityType.ENDER_DRAGON) // Is the hurt entity an Ender Dragon?
            {
                event.setCanceled(true); // Game is not in progress and hurt entity was Ender Dragon. Cancel event
            }
        } else if (RMGameState.State.isGameRunning())
        {
            if (hurtEntity instanceof Player player)
            {

                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    if (playerHunter.isHunter() && RMGameState.areHuntersOnGracePeriod())
                    {
                        event.setCanceled(true); // Hunters cannot be hurt during their grace period
                    }
                });

            }
        }

    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event)
    {
        LivingEntity living = event.getEntity();
        if (living instanceof Player attackedPlayer)
        {
            Entity attacker = event.getSource().getEntity();
            if (attacker == null)
            {
                return;
            }
            if (attacker instanceof Player attackingPlayer)
            {
                attackingPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(attackedHunter ->
                {
                    attackedPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(attackingHunter ->
                    {
                        if (attackingHunter.isHunter()) // Is attacker a hunter
                        {
                            if (attackedHunter.isHunter()) // Is attacked a hunter?
                            {
                                if (!RMGameState.FRIENDLY_FIRE) // Friendly fire?
                                {
                                    event.setCanceled(true); // Hunters cannot hurt each other
                                }
                            } else // attacked is not a hunter
                            {
                                attackedPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(attackedSpeedRunner ->
                                {
                                    if (attackedSpeedRunner.getWasLastKilledByHunter() && RMGameState.isSpeedRunnerOnGracePeriod(attackedPlayer)) // Was the speed runner last killed by a hunter and are they on grace period?
                                    {
                                        event.setCanceled(true); // Hunter cannot attack speed runners on grace period
                                    }
                                });
                            }
                        } else // Attacker is not a hunter
                        {
                            if (!attackedHunter.isHunter()) // Is the target not a hunter?
                            {
                                if (!RMGameState.FRIENDLY_FIRE) // Friendly fire?
                                {
                                    event.setCanceled(true); // Speed runners cannot hurt each other
                                }
                            }
                        }
                    });
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player = event.getEntity();
        Level level = player.level();

        if (!level.isClientSide) // On server?
        {
            player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
            {
                int lives = playerSpeedRunner.getLives();
                boolean wasKilledByHunter= playerSpeedRunner.getWasLastKilledByHunter();
                RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(lives, wasKilledByHunter), player);
            });
            player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
            {
                boolean isHunter = playerHunter.isHunter();
                boolean isBuffed = playerHunter.isBuffed();
                RMPackets.sendToPlayer(new HunterChangeS2CPacket(isHunter, isBuffed), player);
            });
        }

    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        // TODO: Determine if the player was killed by a hunter
        // If so, give them the grace period
        // Otherwise, no grace period
        Player player = event.getEntity();

    }

    @SubscribeEvent
    public static void onGameStart(ReverseManhuntGameStateEvent.Start event)
    {

    }

    @SubscribeEvent
    public static void onGamePause(ReverseManhuntGameStateEvent.Pause event)
    {

    }

    @SubscribeEvent
    public static void onGameResume(ReverseManhuntGameStateEvent.Resume event)
    {

    }

    @SubscribeEvent
    public static void onGameEnd(ReverseManhuntGameStateEvent.End event)
    {

    }

}
