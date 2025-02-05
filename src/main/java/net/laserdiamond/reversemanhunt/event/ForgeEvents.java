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
import net.laserdiamond.reversemanhunt.network.packet.game.GameStateS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.GameTimeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerLifeChangeS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID)
public class ForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        ManageHuntersCommand.register(event.getDispatcher());
        ReverseManhuntGameCommands.register(event.getDispatcher());
    }

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
                    if (!level.isClientSide) // Ensure we are on the server
                    {
                        deadPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                        {
                            playerSpeedRunner.setWasLastKilledByHunter(false); // Assume that the player wasn't killed by the hunter (for now)
                            RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner.getLives(), false), deadPlayer); // Send packet to client from server

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

                                        deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.RED + "You were killed by a Hunter and lost a life!"));

                                        if (playerSpeedRunner.getLives() <= 0) // Does the speed runner still have lives remaining?
                                        {
                                            PlayerHunter.setCapabilityHunterValues(deadPlayer, true, PlayerSpeedRunner.BUFFED_HUNTER_ON_FINAL_DEATH, LogicalSide.SERVER); // No lives remaining. They are now a hunter

                                            deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Hunter!")); // Tell player they are now a hunter

                                            if (PlayerSpeedRunner.getRemainingSpeedRunners().isEmpty()) // Check if there are any remaining speed runners
                                            {
                                                MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.End(ReverseManhuntGameStateEvent.End.Reason.HUNTER_WIN, PlayerSpeedRunner.getRemainingSpeedRunners(), PlayerHunter.getHunters())); // No more speed runners. Hunters win!
                                            }
                                        }
                                    }
                                });
                            }
                        });
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
            RMPackets.sendToPlayer(new GameStateS2CPacket(RMGameState.getCurrentGameState()), player); // Let player know the current game state as soon as they join
            if (RMGameState.State.hasGameBeenStarted()) // Check if the game has been started
            {
                if (!RMGameState.containsLoggedPlayerUUID(player)) // Is the player not part of this iteration (did they join when a game has already started)?
                {
                    // Not already part of this iteration
                    RMGameState.logPlayerUUID(player); // Log them
                    player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                    {
                        // Assign new player their lives (assume they are a speed runner by default)
                        RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(RMGameState.SPEED_RUNNER_LIVES, false), player);
                    });
                    player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                    {
                        // Newly-joined player is not to be a hunter.
                        playerHunter.setHunter(false);
                        playerHunter.setBuffed(false);
                        RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter.isHunter(), playerHunter.isBuffed()), player);
                    });
                }

            }
        }

    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        Player player = event.getEntity();
        if (!player.level().isClientSide)
        {
            RMSoundEvents.stopFlatlineSound(player); // Stop heart flatline on respawn
        }
    }

//    @SubscribeEvent
//    public static void onGameStart(ReverseManhuntGameStateEvent.Start event)
//    {
//
//    }
//
//    @SubscribeEvent
//    public static void onGamePause(ReverseManhuntGameStateEvent.Pause event)
//    {
//
//    }
//
//    @SubscribeEvent
//    public static void onGameResume(ReverseManhuntGameStateEvent.Resume event)
//    {
//
//    }
//
//    @SubscribeEvent
//    public static void onGameEnd(ReverseManhuntGameStateEvent.End event)
//    {
//
//    }

}
