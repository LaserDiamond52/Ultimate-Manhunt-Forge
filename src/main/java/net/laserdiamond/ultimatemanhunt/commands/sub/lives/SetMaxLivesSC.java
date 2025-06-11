package net.laserdiamond.ultimatemanhunt.commands.sub.lives;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SetMaxLivesSC extends UltimateManhuntCommands.SubCommand {

    public SetMaxLivesSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("lives")
                                .then(
                                        Commands.literal("setMax")
                                                .then(
                                                        Commands.argument("amount", IntegerArgumentType.integer(1, UMPlayer.MAX_LIVES))
                                                                .executes(commandContext -> modifyMaxLives(commandContext, IntegerArgumentType.getInteger(commandContext, "amount")))
                                                )
                                )
                ));
    }

    private static void logMaxLifeChange(CommandSourceStack sourceStack, int maxLives)
    {
        sourceStack.sendSuccess(() -> Component.literal("Maximum speed runner lives has been set to: " + maxLives), true);
    }

    private static void logFailMaxLifeChange(CommandSourceStack sourceStack)
    {
        sourceStack.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the maximum amount of speed runner lives when a game has already been started!"));
    }

    private static int modifyMaxLives(CommandContext<CommandSourceStack> commandContext, int maxLives)
    {
        int i = 0;
        if (UMPlayer.setMaxLives(maxLives))
        {
            logMaxLifeChange(commandContext.getSource(), maxLives);
            return i;
        }
        logFailMaxLifeChange(commandContext.getSource());
        i++;
        return i;
    }
}
