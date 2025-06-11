package net.laserdiamond.ultimatemanhunt.commands.sub.gamerule;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SetHardcoreSC extends UltimateManhuntCommands.SubCommand {

    public SetHardcoreSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder)
    {
        super(argumentBuilder.then(
                Commands.literal("gamerule")
                                .then(
                                        Commands.literal("setHardcore")
                                        .then(
                                                Commands.argument("isHardcore", BoolArgumentType.bool())
                                                        .executes(commandContext -> setHardcore(commandContext, BoolArgumentType.getBool(commandContext, "isHardcore")))
                                        )
                                )

        ));
    }

    private static void logHardcoreUpdate(CommandSourceStack source, boolean isHardcore)
    {
        source.sendSuccess(() -> Component.literal("Set Hardcore to: " + isHardcore), true);
    }

    private static void logFailHardcoreUpdate(CommandSourceStack source, boolean isHardcore)
    {
        source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change game rules when a game has been started. " +
                "Please stop the game and run this command again if you wish to change a game rule. " +
                "\nisHardcore is currently: " + isHardcore));
    }

    private static int setHardcore(CommandContext<CommandSourceStack> commandContext, boolean isHardcore)
    {
        int i = 0;
        if (UMGame.State.hasGameBeenStarted())
        {
            // Game started. Do not change
            logFailHardcoreUpdate(commandContext.getSource(), isHardcore);
            return 0;
        }
        UMGame.setHardcore(isHardcore);
        logHardcoreUpdate(commandContext.getSource(), isHardcore);
        return i;
    }
}
