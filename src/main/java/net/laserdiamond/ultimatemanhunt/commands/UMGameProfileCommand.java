package net.laserdiamond.ultimatemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.event.ForgeServerEvents;
import net.laserdiamond.ultimatemanhunt.util.file.UMGameSettingProfileConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class UMGameProfileCommand {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("um_game_profiles")
                        .requires(sourceStack -> ForgeServerEvents.permission(sourceStack, PERMISSION_LEVEL))
                        .then(
                                Commands.argument("game_profile_name", StringArgumentType.string())
                                        .then(
                                                Commands.literal("load")
                                                        .executes(commandContext -> loadGameProfile(commandContext, StringArgumentType.getString(commandContext, "game_profile_name")))
                                        )
                                        .then(
                                                Commands.literal("save")
                                                        .executes(commandContext -> saveGameProfile(commandContext, StringArgumentType.getString(commandContext, "game_profile_name")))
                                        )
                                        .then(
                                                Commands.literal("delete")
                                                        .executes(commandContext -> deleteGameProfile(commandContext, StringArgumentType.getString(commandContext, "game_profile_name")))
                                        )
                        )
        );
    }

    private static int loadGameProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;
        if (UMGame.State.hasGameBeenStarted())
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot load Game Profiles when a game has already been started!"));
            return i;
        }
        if (!UMGameSettingProfileConfig.doesGameProfileFileExist(profileName))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Game Profile \"" + profileName + "\" does not exist"));
            return i;
        }
        new UMGameSettingProfileConfig(profileName).applySettingsToGame();
        commandContext.getSource().sendSuccess(() -> Component.literal("Applied settings from Game Profile \"" + profileName + "\" to game"), true);
        i++;
        return i;
    }

    private static int saveGameProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;
        if (UMGameSettingProfileConfig.doesGameProfileFileExist(profileName))
        {
            commandContext.getSource().sendSuccess(() -> Component.literal("Overwrote settings for Game Profile \"" + profileName + "\""), true);
            i++;
            return i;
        }
        new UMGameSettingProfileConfig(profileName).saveSettingsToFile();
        commandContext.getSource().sendSuccess(() -> Component.literal("Saved new Game Profile \"" + profileName + "\" to file."), true);
        i++;
        return i;
    }

    private static int deleteGameProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;
        if (!UMGameSettingProfileConfig.doesGameProfileFileExist(profileName))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Game Profile \"" + profileName + "\" does not exist"));
            return i;
        }
        new UMGameSettingProfileConfig(profileName).deleteFile();
        commandContext.getSource().sendSuccess(() -> Component.literal("Deleted Game Profile \"" + profileName + "\""), true);
        i++;
        return i;
    }
}
