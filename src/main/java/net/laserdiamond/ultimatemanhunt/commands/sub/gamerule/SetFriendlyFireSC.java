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

public final class SetFriendlyFireSC extends UltimateManhuntCommands.SubCommand {

    public SetFriendlyFireSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder)
    {
        super(argumentBuilder.then(
                Commands.literal("gamerule")
                                .then(Commands.literal("setFriendlyFire")
                                        .then(
                                                Commands.argument("isFriendlyFire", BoolArgumentType.bool())
                                                        .executes(commandContext -> setFriendlyFire(commandContext, BoolArgumentType.getBool(commandContext, "isFriendlyFire")))
                                        ))

                ));
    }

    private static void logFriendlyFireUpdate(CommandSourceStack source, boolean isFriendlyFire)
    {
        source.sendSuccess(() -> Component.literal("Set Friendly Fire to: " + isFriendlyFire), true);
    }

    private static void logFailFriendlyFireUpdate(CommandSourceStack source)
    {
        source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change game rules when a game has been started. " +
                "Please stop the game and run this command again if you wish to change a game rule. " +
                "\nisFriendlyFire is currently: " + UMGame.isFriendlyFire()));
    }

    private static int setFriendlyFire(CommandContext<CommandSourceStack> commandContext, boolean isFriendlyFire)
    {
        int i = 0;
        if (UMGame.State.hasGameBeenStarted())
        {
            // Game started. Do not change
            logFailFriendlyFireUpdate(commandContext.getSource());
            return 0;
        }
        UMGame.setFriendlyFire(isFriendlyFire);
        logFriendlyFireUpdate(commandContext.getSource(), isFriendlyFire);
        i++;
        return i;
    }
}
