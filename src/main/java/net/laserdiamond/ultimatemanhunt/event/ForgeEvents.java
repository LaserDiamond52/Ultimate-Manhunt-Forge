package net.laserdiamond.ultimatemanhunt.event;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.api.event.RegisterManhuntSubCommandEvent;
import net.laserdiamond.ultimatemanhunt.api.event.SpeedRunnerLifeLossEvent;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayerCapability;
import net.laserdiamond.ultimatemanhunt.commands.*;
import net.laserdiamond.ultimatemanhunt.commands.sub.GameProfileSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.GracePeriodSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.SetGameStateSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.SetSpawnCommand;
import net.laserdiamond.ultimatemanhunt.commands.sub.gamerule.AllowWindTorchesSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.gamerule.BuffedHunterOnFinalDeathSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.gamerule.SetFriendlyFireSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.gamerule.SetHardcoreSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.lives.SetCurrentLivesSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.lives.SetMaxLivesSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.playerrole.SetCurrentPlayerRoleSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.playerrole.SetDeadPlayerRoleSC;
import net.laserdiamond.ultimatemanhunt.commands.sub.playerrole.SetNewPlayerRoleSC;
import net.laserdiamond.ultimatemanhunt.item.UMItems;
import net.laserdiamond.ultimatemanhunt.item.WindTorchItem;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.game.GameStateS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.HardcoreUpdateS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.game.RemainingPlayerCountS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.hunter.HunterGracePeriodDurationS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerDistanceFromHunterS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerGracePeriodDurationS2CPacket;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerMaxLifeChangeS2CPacket;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class ForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        MinecraftForge.EVENT_BUS.post(new RegisterManhuntSubCommandEvent(event));
        UltimateManhuntCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void registerSubCommands(RegisterManhuntSubCommandEvent event)
    {
        event.registerSubCommand(UltimateManhunt.fromUMPath("friendly_fire"), SetFriendlyFireSC::new);
        event.registerSubCommand(UltimateManhunt.fromUMPath("hardcore"), SetHardcoreSC::new);
        event.registerSubCommand(UltimateManhunt.fromUMPath("buffed_hunters_on_final_death"), BuffedHunterOnFinalDeathSC::new);
        event.registerSubCommand(UltimateManhunt.fromUMPath("allow_wind_torches"), AllowWindTorchesSC::new);

        event.registerSubCommand(UltimateManhunt.fromUMPath("dead_player_roles"), SetDeadPlayerRoleSC::new);
        event.registerSubCommand(UltimateManhunt.fromUMPath("new_player_roles"), SetNewPlayerRoleSC::new);
        event.registerSubCommand(UltimateManhunt.fromUMPath("current_player_roles"), SetCurrentPlayerRoleSC::new);

        event.registerSubCommand(UltimateManhunt.fromUMPath("max_lives"), SetMaxLivesSC::new);
        event.registerSubCommand(UltimateManhunt.fromUMPath("current_lives"), SetCurrentLivesSC::new);

        event.registerSubCommand(UltimateManhunt.fromUMPath("game_state"), SetGameStateSC::new);

        event.registerSubCommand(UltimateManhunt.fromUMPath("game_profile"), GameProfileSC::new);

        event.registerSubCommand(UltimateManhunt.fromUMPath("set_spawn"), SetSpawnCommand::new);

        event.registerSubCommand(UltimateManhunt.fromUMPath("grace_period"), GracePeriodSC::new);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event)
    {
        if (UMGame.State.isGameNotInProgress()) // Is a game in progress?
        {
            return; // No game in progress. End method
        }
        LivingEntity living = event.getEntity();
        if (living == null)
        {
            return; // Dead entity is null. End method
        }
        if (living instanceof Player deadPlayer) // Check if a player died
        {
            Level level = deadPlayer.level();
            if (level.isClientSide)
            {
                return; // ensure we are on the server
            }
            Entity sourceEntity = event.getSource().getEntity();
            deadPlayer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(deadUMPlayer ->
            {
                if (deadUMPlayer.isSpeedRunner())
                {
                    if (UMGame.isHardcore())
                    {
                        MinecraftForge.EVENT_BUS.post(new SpeedRunnerLifeLossEvent(deadPlayer, null));
                        return; // Took life from hardcore speed runner, end method
                    }
                    if (sourceEntity instanceof Player killer)
                    {
                        killer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
                        {
                            if (umPlayer.isHunter())
                            {
                                MinecraftForge.EVENT_BUS.post(new SpeedRunnerLifeLossEvent(deadPlayer, killer));
                            } else
                            {
                                if (isNearHunter(deadPlayer))
                                {
                                    MinecraftForge.EVENT_BUS.post(new SpeedRunnerLifeLossEvent(deadPlayer, killer));
                                }
                            }
                        });
                        return;
                    }
                    if (isNearHunter(deadPlayer))
                    {
                        MinecraftForge.EVENT_BUS.post(new SpeedRunnerLifeLossEvent(deadPlayer, null));

                    }
                } else if (deadUMPlayer.isHunter()) // Player is a hunter
                {
                    if (UMGame.areHuntersOnGracePeriod()) // Are hunters on grace period?
                    {
                        // On grace period. Cancel death and set health back
                        event.setCanceled(true);
                        deadPlayer.setHealth(deadPlayer.getMaxHealth());
                    }
                }
            });
        }
    }

    private static boolean isNearHunter(Player playerSpeedRunner)
    {
        for (Player playerHunter : UMPlayer.getHunters(false)) // Loop through all hunters
        {
            return UMGame.isNearHunter(playerSpeedRunner, playerHunter);
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event)
    {
        LivingEntity hurtEntity = event.getEntity();
        if (UMGame.State.isGameNotInProgress()) // Is the game not in progress?
        {
            if (hurtEntity.getType() == EntityType.ENDER_DRAGON) // Is the hurt entity an Ender Dragon?
            {
                event.setCanceled(true); // Game is not in progress and hurt entity was Ender Dragon. Cancel event
            }
        } else if (UMGame.State.isGameRunning())
        {
            if (hurtEntity instanceof Player player)
            {
                player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
                {
                    if (umPlayer.isHunter() && UMGame.areHuntersOnGracePeriod())
                    {
                        event.setCanceled(true);
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
            if (attacker.level().isClientSide)
            {
                return; // Ensure we are on the client
            }
            if (attacker instanceof Player attackingPlayer)
            {
                attackingPlayer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(attackingUMPlayer ->
                {
                    attackedPlayer.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(attackedUMPlayer ->
                    {
                        if (attackingUMPlayer.isHunter())
                        {
                            if (attackedUMPlayer.isHunter())
                            {
                                if (!UMGame.isFriendlyFire())
                                {
                                    event.setCanceled(true);
                                }

                            } else if (attackedUMPlayer.isSpeedRunner())
                            {
                                if (attackedUMPlayer.isWasLastKilledByHunter() && UMPlayer.isSpeedRunnerOnGracePeriodServer(attackedPlayer))
                                {
                                    long duration = (attackedUMPlayer.getGracePeriodTimeStamp() - UMGame.getCurrentGameTime()) / 20;
                                    attackingPlayer.sendSystemMessage(Component.literal(ChatFormatting.BLUE + attackedPlayer.getName().getString() + " is immune to hunters for " + ChatFormatting.YELLOW + duration + ChatFormatting.BLUE + " seconds"));
                                    event.setCanceled(true); // Hunter cannot attack speed runners on grace period
                                }
                            }
                        } else if (attackingUMPlayer.isSpeedRunner())
                        {
                            if (attackedUMPlayer.isSpeedRunner() && !UMGame.isFriendlyFire())
                            {
                                event.setCanceled(true);
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

        UMPackets.sendToPlayer(new GameStateS2CPacket(UMGame.getCurrentGameState()), player); // Let player know the current game state as soon as they join
        UMPackets.sendToPlayer(new HardcoreUpdateS2CPacket(UMGame.isHardcore()), player); // Let the player know whether hardcore is enabled
        UMPackets.sendToPlayer(new HunterGracePeriodDurationS2CPacket(UMGame.getHunterGracePeriod()), player); // Let the player know the hunter grace period
        UMPackets.sendToPlayer(new SpeedRunnerGracePeriodDurationS2CPacket(UMGame.getSpeedRunnerGracePeriod()), player); // Let the player know the speed runner grace period
        UMPackets.sendToPlayer(new SpeedRunnerMaxLifeChangeS2CPacket(UMPlayer.getMaxLives()), player); // Let the player know how many lives speed runners can have
        // Game Time packets and updates are sent every tick. It is redundant to send them at this moment

        if (!UMGame.isWindTorchEnabled())
        {
            player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.getItem() instanceof WindTorchItem, -1, player.inventoryMenu.getCraftSlots());
        }

        if (UMGame.State.hasGameBeenStarted()) // Check if the game has been started
        {
            if (!UMGame.containsLoggedPlayerUUID(player)) // Is the player not part of this iteration (did they join when a game has already started)?
            {
                // Not already part of this iteration
                player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
                {
                    player.sendSystemMessage(Component.literal("You joined a Manhunt game that is already in progress and have been declared as a " + UMGame.getNewPlayerRole().getAsName()));
                    player.getInventory().clearContent(); // Clear items
                    switch (UMGame.getNewPlayerRole())
                    {
                        case SPECTATOR ->
                        {
                            umPlayer.resetToSpectator(player, true);
                        }
                        case SPEED_RUNNER ->
                        {
                            if (player instanceof ServerPlayer serverPlayer)
                            {
                                serverPlayer.setGameMode(GameType.DEFAULT_MODE);
                            }
                            umPlayer.resetToSpeedRunner(player, true);
                            if (UMGame.isWindTorchEnabled()) // Is the Wind Torch enabled?
                            {
                                player.getInventory().add(new ItemStack(UMItems.WIND_TORCH.get())); // Grant the player a Wind Torch
                            }
                        }
                        case HUNTER ->
                        {
                            if (player instanceof ServerPlayer serverPlayer)
                            {
                                serverPlayer.setGameMode(GameType.DEFAULT_MODE);
                            }
                            umPlayer.resetToHunter(player, true);
                        }
                        default -> umPlayer.resetToSpectator(player, true);
                    }
                    umPlayer.sendUpdateFromServerToSelf(player);
                });
            }
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                if (umPlayer.isHunter())
                {
                    if (umPlayer.isBuffedHunter() && UMGame.State.isGameRunning())
                    {
                        player.getAttributes().addTransientAttributeModifiers(UMPlayer.createHunterAttributes()); // Grant player hunter attributes if they are a buffed hunter
                    }
                    if (UMGame.getCurrentGameTime() < UMGame.getHunterGracePeriod())
                    {
                        player.getAbilities().mayfly = true;
                        player.getAbilities().flying = true;
                        player.onUpdateAbilities();
                    }
                }
            });
            UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket()); // Let all player know how many remaining players on each team
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (event.getEntity().level().isClientSide)
        {
            return;
        }
        // Update count for remaining players
        UMPackets.sendToAllClients(new RemainingPlayerCountS2CPacket());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
    {
        Player player = event.getEntity();
        if (!player.level().isClientSide)
        {
            UMSoundEvents.stopFlatlineSound(player); // Stop heart flatline on respawn

            // Player has just respawned. Set their grace period time stamp if they were previously killed by a hunter
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                // Assume the player is not near a hunter anymore
                SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterPlayer(player);
                if (umPlayer.isWasLastKilledByHunter())
                {
                    long timeStamp = UMGame.getCurrentGameTime() + UMGame.getSpeedRunnerGracePeriod();
                    umPlayer.setGracePeriodTimeStamp(timeStamp);
                } else
                {
                    umPlayer.setGracePeriodTimeStamp(0);
                }
                umPlayer.sendUpdateFromServerToSelf(player);
            });
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event)
    {
        final Entity targetEntity = event.getTarget();
        final Player player = event.getEntity();

        if (targetEntity instanceof Player playerTarget)
        {
            playerTarget.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                umPlayer.sendUpdateFromServer(playerTarget, player);
            });
        }
    }

    @SubscribeEvent
    public static void onStopTracking(PlayerEvent.StopTracking event)
    {
        final Entity targetEntity = event.getEntity();
        final Player player = event.getEntity();

        if (targetEntity instanceof Player playerTarget)
        {
            playerTarget.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                umPlayer.sendUpdateFromServer(playerTarget, player);
            });
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
