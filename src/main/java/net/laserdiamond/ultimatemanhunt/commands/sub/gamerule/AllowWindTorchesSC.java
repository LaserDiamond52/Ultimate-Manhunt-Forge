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

public final class AllowWindTorchesSC extends UltimateManhuntCommands.SubCommand {

    public AllowWindTorchesSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder)
    {
        super(argumentBuilder.then(
                Commands.literal("gamerule")
                                .then(
                                        Commands.literal("allowWindTorches")
                                        .then(
                                                Commands.argument("enableWindTorches", BoolArgumentType.bool())
                                                        .executes(commandContext -> setWindTorches(commandContext, BoolArgumentType.getBool(commandContext, "enableWindTorches")))
                                        )
                                )

        ));
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
