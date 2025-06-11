package net.laserdiamond.ultimatemanhunt.commands.sub;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.api.event.UltimateManhuntGameStateEvent;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.laserdiamond.ultimatemanhunt.util.file.UMGameSettingProfileConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;

public final class SetGameStateSC extends UltimateManhuntCommands.SubCommand {

    public SetGameStateSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("gameState")
                                .then(
                                        Commands.literal("start")
                                                .executes(commandContext -> changeGameState(commandContext, UMGame.State.STARTED))
                                                .then(
                                                        Commands.argument("game_profile_name", StringArgumentType.string())
                                                                .executes(commandContext -> startGameFromProfile(commandContext, StringArgumentType.getString(commandContext, "game_profile_name")))
                                                )
                                )
                                .then(
                                        Commands.literal("pause")
                                                .executes(commandContext -> changeGameState(commandContext, UMGame.State.PAUSED))
                                )
                                .then(
                                        Commands.literal("resume")
                                                .executes(commandContext -> changeGameState(commandContext, UMGame.State.IN_PROGRESS))
                                )
                                .then(
                                        Commands.literal("stop")
                                                .executes(commandContext -> changeGameState(commandContext, UMGame.State.NOT_STARTED))
                                )
                ));
    }

    private static void logGameStateChange(CommandSourceStack source, UMGame.State newGameState, boolean successful)
    {
        if (successful)
        {
            source.sendSuccess(() -> Component.literal("Set the current state of the " + ChatFormatting.GOLD + "Ultimate Manhunt Game" + ChatFormatting.WHITE + " to " + ChatFormatting.AQUA + newGameState.getAsName()), true);

            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers())
            {
                switch (newGameState)
                {
                    case STARTED -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Ultimate Manhunt Game" + ChatFormatting.WHITE + " has started!"));
                    case IN_PROGRESS -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Ultimate Manhunt Game" + ChatFormatting.WHITE + " has resumed!"));
                    case PAUSED -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Ultimate Manhunt Game" + ChatFormatting.WHITE + " has been paused!"));
                    case NOT_STARTED -> player.sendSystemMessage(Component.literal("The " + ChatFormatting.GOLD + "Ultimate Manhunt Game" + ChatFormatting.WHITE + " was forcefully ended!"));
                }
            }
        } else
        {
            source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the current state of the " + ChatFormatting.GOLD + "Ultimate Manhunt Game" + ChatFormatting.RED + " from " + ChatFormatting.AQUA + UMGame.getCurrentGameState().getAsName() + ChatFormatting.RED + " to " + ChatFormatting.AQUA + newGameState.getAsName()));
        }
    }

    private static void logFailStart(CommandSourceStack source, UMGame.State newGameState)
    {
        switch (newGameState)
        {
            case STARTED -> source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot start the game because there are no Hunters!"));
            case IN_PROGRESS -> source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot resume the game because there are no Hunters!"));
        }
    }

    private static int changeGameState(CommandContext<CommandSourceStack> commandContext, UMGame.State newGameState)
    {
        int i = 0;

        List<Player> hunters = UMPlayer.getHunters(false);
        List<Player> speedRunners = UMPlayer.getRemainingSpeedRunners();
//        switch (newGameState)
//        {
//            case STARTED, IN_PROGRESS ->
//            {
//                if (hunters.isEmpty()) // Are there any hunters?
//                {
//                    logFailStart(commandContext.getSource(), newGameState); // No hunters. Game cannot start/resume
//                    return i;
//                }
//            }
//        }

        if (UMGame.setCurrentGameState(newGameState)) // Check that the game state has changed
        {
            switch (newGameState)
            {
                case STARTED -> MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.Start());
                case IN_PROGRESS -> MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.Resume());
                case PAUSED -> MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.Pause());
                case NOT_STARTED -> MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.End(UltimateManhuntGameStateEvent.End.Reason.COMMAND));
            }
            logGameStateChange(commandContext.getSource(), newGameState, true);
            i++;
        } else
        {
            logGameStateChange(commandContext.getSource(), newGameState, false);
        }

        return i;
    }

    private static int startGameFromProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;

        if (!UMGameSettingProfileConfig.doesGameProfileFileExist(profileName))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot start game from Game Profile \"" + profileName + "\" because it does not exist"));
            return i;
        }
        new UMGameSettingProfileConfig(profileName).applySettingsToGame();
        if (UMGame.setCurrentGameState(UMGame.State.STARTED))
        {
            MinecraftForge.EVENT_BUS.post(new UltimateManhuntGameStateEvent.Start());
            logGameStateChange(commandContext.getSource(), UMGame.State.STARTED, true);
            i++;
        }

        return i;
    }
}
