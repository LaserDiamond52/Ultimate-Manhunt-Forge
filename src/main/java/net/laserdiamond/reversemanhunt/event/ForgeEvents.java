package net.laserdiamond.reversemanhunt.event;

import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.PlayerHunter;
import net.laserdiamond.reversemanhunt.capability.PlayerHunterCapability;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunner;
import net.laserdiamond.reversemanhunt.capability.PlayerSpeedRunnerCapability;
import net.laserdiamond.reversemanhunt.commands.ManageHuntersCommand;
import net.laserdiamond.reversemanhunt.commands.ReverseManhuntGameCommands;
import net.laserdiamond.reversemanhunt.commands.SetGracePeriodCommand;
import net.laserdiamond.reversemanhunt.commands.gamerule.SetFriendlyFireCommand;
import net.laserdiamond.reversemanhunt.commands.gamerule.SetHardcoreCommand;
import net.laserdiamond.reversemanhunt.network.RMPackets;
import net.laserdiamond.reversemanhunt.network.packet.game.GameStateS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.game.HardcoreUpdateS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterChangeS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.hunter.HunterGracePeriodDurationS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerGracePeriodDurationS2CPacket;
import net.laserdiamond.reversemanhunt.network.packet.speedrunner.SpeedRunnerLifeChangeS2CPacket;
import net.laserdiamond.reversemanhunt.sound.RMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
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
        SetFriendlyFireCommand.register(event.getDispatcher());
        SetHardcoreCommand.register(event.getDispatcher());
        SetGracePeriodCommand.register(event.getDispatcher());
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
            Level level = deadPlayer.level();
            if (level.isClientSide)
            {
                return; // ensure we are on the server
            }
            Entity sourceEntity = event.getSource().getEntity();
            deadPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(deadHunter ->
            {
                if (!deadHunter.isHunter()) // Was the dead player NOT a hunter?
                {
                    deadPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(deadSpeedRunner ->
                    {
                        deadSpeedRunner.setWasLastKilledByHunter(false);
                        RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(deadSpeedRunner), deadPlayer);

                        if (RMGameState.isHardcore()) // Hardcore?
                        {
                            // Remove a player life
                            deadSpeedRunner.subtractLife();
                            RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(deadSpeedRunner), deadPlayer);

                            RMSoundEvents.playFlatlineSound(deadPlayer);
                            deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.RED + "You died and lost a life!"));
                            if (deadSpeedRunner.getLives() <= 0)
                            {
                                deadHunter.setHunter(true);
                                deadHunter.setBuffed(PlayerSpeedRunner.BUFFED_HUNTER_ON_FINAL_DEATH);
                                RMPackets.sendToPlayer(new HunterChangeS2CPacket(deadHunter), deadPlayer);

                                deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Hunter!")); // Tell player they are now a hunter

                                if (PlayerSpeedRunner.getRemainingSpeedRunners().isEmpty()) // Check if there are any remaining speed runners
                                {
                                    MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.End(ReverseManhuntGameStateEvent.End.Reason.HUNTER_WIN, PlayerSpeedRunner.getRemainingSpeedRunners(), PlayerHunter.getHunters())); // No more speed runners. Hunters win!
                                }
                            }
                            return; // End method
                        }

                        if (sourceEntity == null)
                        {
                            if (PlayerSpeedRunner.ServerHunterMarker.INSTANCE.getIsNearHunter(deadPlayer)) // Check if the dead player was near a hunter
                            {
                                deadSpeedRunner.subtractLife();
                                deadSpeedRunner.setWasLastKilledByHunter(true);
                                RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(deadSpeedRunner), deadPlayer);

                                RMSoundEvents.playFlatlineSound(deadPlayer);
                                deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.RED + "You died and lost a life!"));

                                if (deadSpeedRunner.getLives() <= 0)
                                {
                                    deadHunter.setHunter(true);
                                    deadHunter.setBuffed(PlayerSpeedRunner.BUFFED_HUNTER_ON_FINAL_DEATH);
                                    RMPackets.sendToPlayer(new HunterChangeS2CPacket(deadHunter), deadPlayer);

                                    deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Hunter!")); // Tell player they are now a hunter

                                    if (PlayerSpeedRunner.getRemainingSpeedRunners().isEmpty()) // Check if there are any remaining speed runners
                                    {
                                        MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.End(ReverseManhuntGameStateEvent.End.Reason.HUNTER_WIN, PlayerSpeedRunner.getRemainingSpeedRunners(), PlayerHunter.getHunters())); // No more speed runners. Hunters win!
                                    }
                                }
                            }
                            return; // End method if killer is null
                        }
                        if (sourceEntity instanceof Player killer) // Is killer a player?
                        {
                            killer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(killerHunter ->
                            {
                                if (killerHunter.isHunter())
                                {
                                    deadSpeedRunner.subtractLife();
                                    deadSpeedRunner.setWasLastKilledByHunter(true);
                                    RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(deadSpeedRunner), deadPlayer);

                                    RMSoundEvents.playFlatlineSound(deadPlayer);
                                    deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.RED + "You were killed by a Hunter and lost a life!"));

                                    if (deadSpeedRunner.getLives() <= 0) // Does the speed runner still have lives remaining?
                                    {
//                                        PlayerHunter.setCapabilityHunterValues(deadPlayer, true, PlayerSpeedRunner.BUFFED_HUNTER_ON_FINAL_DEATH); // No lives remaining. They are now a hunter
                                        deadHunter.setHunter(true);
                                        deadHunter.setBuffed(PlayerSpeedRunner.BUFFED_HUNTER_ON_FINAL_DEATH);
                                        RMPackets.sendToPlayer(new HunterChangeS2CPacket(deadHunter), deadPlayer);

                                        deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Hunter!")); // Tell player they are now a hunter

                                        if (PlayerSpeedRunner.getRemainingSpeedRunners().isEmpty()) // Check if there are any remaining speed runners
                                        {
                                            MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.End(ReverseManhuntGameStateEvent.End.Reason.HUNTER_WIN, PlayerSpeedRunner.getRemainingSpeedRunners(), PlayerHunter.getHunters())); // No more speed runners. Hunters win!
                                        }
                                    }
                                }
                            });
                        } else // Killer is not a player
                        {
                            if (PlayerSpeedRunner.ServerHunterMarker.INSTANCE.getIsNearHunter(deadPlayer)) // Check if the dead player was near a hunter
                            {
                                deadSpeedRunner.subtractLife();
                                deadSpeedRunner.setWasLastKilledByHunter(true);
                                RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(deadSpeedRunner), deadPlayer);

                                RMSoundEvents.playFlatlineSound(deadPlayer);
                                deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.RED + "You died and lost a life!"));

                                if (deadSpeedRunner.getLives() <= 0)
                                {
                                    deadHunter.setHunter(true);
                                    deadHunter.setBuffed(PlayerSpeedRunner.BUFFED_HUNTER_ON_FINAL_DEATH);
                                    RMPackets.sendToPlayer(new HunterChangeS2CPacket(deadHunter), deadPlayer);

                                    deadPlayer.sendSystemMessage(Component.literal(ChatFormatting.DARK_RED + "You have lost all your lives and are now a Hunter!")); // Tell player they are now a hunter

                                    if (PlayerSpeedRunner.getRemainingSpeedRunners().isEmpty()) // Check if there are any remaining speed runners
                                    {
                                        MinecraftForge.EVENT_BUS.post(new ReverseManhuntGameStateEvent.End(ReverseManhuntGameStateEvent.End.Reason.HUNTER_WIN, PlayerSpeedRunner.getRemainingSpeedRunners(), PlayerHunter.getHunters())); // No more speed runners. Hunters win!
                                    }
                                }
                            }
                        }
                    });
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
                attackingPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(attackingHunter ->
                {
                    attackedPlayer.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(attackedHunter ->
                    {
                        if (attackingHunter.isHunter()) // Is attacker a hunter
                        {
                            if (attackedHunter.isHunter()) // Is attacked a hunter?
                            {
                                if (!RMGameState.isFriendlyFire()) // Friendly fire?
                                {
                                    event.setCanceled(true); // Hunters cannot hurt each other
                                }
                            } else // attacked is not a hunter
                            {
                                attackedPlayer.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(attackedSpeedRunner ->
                                {
                                    if (attackedSpeedRunner.getWasLastKilledByHunter() && RMGameState.isSpeedRunnerOnGracePeriod(attackedPlayer)) // Was the speed runner last killed by a hunter and are they on grace period?
                                    {
                                        int duration = (RMGameState.getSpeedRunnerGracePeriod() - attackedPlayer.tickCount) / 20;
                                        attackingPlayer.sendSystemMessage(Component.literal(ChatFormatting.BLUE + "Player is immune to hunters for " + ChatFormatting.YELLOW + duration + ChatFormatting.BLUE + " seconds"));
                                        event.setCanceled(true); // Hunter cannot attack speed runners on grace period
                                    }
                                });
                            }
                        } else // Attacker is not a hunter
                        {
                            if (!attackedHunter.isHunter()) // Is the target not a hunter?
                            {
                                if (!RMGameState.isFriendlyFire()) // Friendly fire?
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
        if (level.isClientSide)
        {
            return;
        }

        RMPackets.sendToPlayer(new GameStateS2CPacket(RMGameState.getCurrentGameState()), player); // Let player know the current game state as soon as they join
        RMPackets.sendToPlayer(new HardcoreUpdateS2CPacket(RMGameState.isHardcore()), player); // Let the player know whether hardcore is enabled
        RMPackets.sendToPlayer(new HunterGracePeriodDurationS2CPacket(RMGameState.getHunterGracePeriod()), player); // Let the player know the hunter grace period
        RMPackets.sendToPlayer(new SpeedRunnerGracePeriodDurationS2CPacket(RMGameState.getSpeedRunnerGracePeriod()), player); // Let the player know the speed runner grace period

        if (RMGameState.State.hasGameBeenStarted()) // Check if the game has been started
        {
            if (!RMGameState.containsLoggedPlayerUUID(player)) // Is the player not part of this iteration (did they join when a game has already started)?
            {
                // Not already part of this iteration
                RMGameState.logPlayerUUID(player); // Log them
                player.getCapability(PlayerSpeedRunnerCapability.PLAYER_SPEED_RUNNER_LIVES).ifPresent(playerSpeedRunner ->
                {
                    // Assign new player their lives (assume they are a speed runner by default)
                    playerSpeedRunner.setLives(RMGameState.SPEED_RUNNER_LIVES);
                    playerSpeedRunner.setWasLastKilledByHunter(false);
                    RMPackets.sendToPlayer(new SpeedRunnerLifeChangeS2CPacket(playerSpeedRunner), player);
                });
                player.getCapability(PlayerHunterCapability.PLAYER_HUNTER).ifPresent(playerHunter ->
                {
                    // Newly-joined player is not to be a hunter.
                    playerHunter.setHunter(false);
                    playerHunter.setBuffed(false);
                    RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter), player);
//                    RMPackets.sendToPlayer(new HunterChangeS2CPacket(playerHunter.isHunter(), playerHunter.isBuffed()), player);
                });
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
