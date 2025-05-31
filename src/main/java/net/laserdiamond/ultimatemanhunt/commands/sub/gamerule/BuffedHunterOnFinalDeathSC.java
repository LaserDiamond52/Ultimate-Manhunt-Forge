package net.laserdiamond.ultimatemanhunt.commands.sub.gamerule;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class BuffedHunterOnFinalDeathSC extends UltimateManhuntCommands.SubCommand
{
    public BuffedHunterOnFinalDeathSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder)
    {
        super(argumentBuilder.then(
                        Commands.literal("gamerule")
                                .then(
                                        Commands.literal("buffedHuntersOnFinalDeath")
                                                .then(
                                                        Commands.argument("isBuffedHunter", BoolArgumentType.bool())
                                                                .executes(commandContext -> setIsBuffedHunterOnFinalDeath(commandContext.getSource(), BoolArgumentType.getBool(commandContext, "isBuffedHunter")))
                                                )
                                )
                ));
    }

    private static void logBuffedHunterOnFinalDeathUpdate(CommandSourceStack sourceStack, boolean isBuffedOnDeath)
    {
        if (isBuffedOnDeath)
        {
            sourceStack.sendSuccess(() -> Component.literal("Set Speed Runners to become buffed hunters on their final death"), true);
        } else
        {
            sourceStack.sendSuccess(() -> Component.literal("Set Speed Runners to not become buffed hunters on their final death"), true);
            return;
        }
        sourceStack.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change game rules when a game has been started. " +
                "Please stop the game and run this command again if you wish to change a game rule. " +
                "\nisBuffedHunterOnFinalDeath is currently: " + UMPlayer.getIsBuffedHunterOnFinalDeath()));
    }

    private static int setIsBuffedHunterOnFinalDeath(CommandSourceStack sourceStack, boolean isBuffedHunter)
    {
        int i = 0;

        logBuffedHunterOnFinalDeathUpdate(sourceStack, isBuffedHunter);
        if (UMPlayer.setIsBuffedHunterOnFinalDeath(isBuffedHunter))
        {
            i++;
            return i;
        }

        return i;
    }
}
