package net.laserdiamond.ultimatemanhunt.commands.gamerule;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.event.ForgeServerEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetWindTorchesCommand {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("um_wind_torches")
                        .requires(sourceStack -> ForgeServerEvents.permission(sourceStack, PERMISSION_LEVEL))
                        .then(
                                Commands.argument("enableWindTorches", BoolArgumentType.bool())
                                        .executes(commandContext -> setWindTorches(commandContext, BoolArgumentType.getBool(commandContext, "enableWindTorches")))
                        )
        );
    }

    private static int setWindTorches(CommandContext<CommandSourceStack> commandContext, boolean enabled)
    {
        int i = 0;

        String activeStr;
        if (enabled)
        {
            activeStr = "enable";
        } else
        {
            activeStr = "disable";
        }

        if (UMGame.State.hasGameBeenStarted())
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot " + activeStr + " Wind Torches while a game is active!"));
            return i;
        }

        UMGame.setWindTorchEnabled(enabled);
        commandContext.getSource().sendSuccess(() -> Component.literal("Wind Torches are now " + activeStr + "d for Speed Runners"), true);

        return i;
    }
}
