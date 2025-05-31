package net.laserdiamond.ultimatemanhunt.commands.sub.playerrole;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SetDeadPlayerRoleSC extends UltimateManhuntCommands.SubCommand {

    public SetDeadPlayerRoleSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("roles")
                                .then(
                                        Commands.literal("deadPlayer")
                                                .requires(UltimateManhunt::hasPermission)
                                                .then(
                                                        Commands.literal("spectator")
                                                                .executes(commandContext -> setDeadPlayerRole(commandContext.getSource(), UMGame.PlayerRole.SPECTATOR))
                                                )
                                                .then(
                                                        Commands.literal("hunter")
                                                                .executes(commandContext -> setDeadPlayerRole(commandContext.getSource(), UMGame.PlayerRole.HUNTER))
                                                )
                                )
                )
        );
    }

    private static int setDeadPlayerRole(CommandSourceStack sourceStack, UMGame.PlayerRole playerRole)
    {
        int i = 0;
        if (UMGame.State.hasGameBeenStarted())
        {
            sourceStack.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change how defeated speed runners are declared when a Manhunt game has already been started!"));
            return i;
        }
        UMGame.setDeadSpeedRunnerRole(playerRole);
        sourceStack.sendSuccess(() -> Component.literal("Defeated speed runners will be declared as " + playerRole.getAsName() + " after losing all their lives"), true);
        i++;
        return i;
    }
}
