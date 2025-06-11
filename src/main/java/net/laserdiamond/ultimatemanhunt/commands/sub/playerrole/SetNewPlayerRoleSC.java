package net.laserdiamond.ultimatemanhunt.commands.sub.playerrole;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SetNewPlayerRoleSC extends UltimateManhuntCommands.SubCommand
{

    public SetNewPlayerRoleSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("roles")
                                .then(
                                        Commands.literal("newPlayerRole")
                                                .requires(UltimateManhunt::hasPermission)
                                                .then(
                                                        Commands.literal("speed_runner")
                                                                .executes(commandContext -> setNewRole(commandContext.getSource(), UMGame.PlayerRole.SPEED_RUNNER))
                                                ).then(
                                                        Commands.literal("spectator")
                                                                .executes(commandContext -> setNewRole(commandContext.getSource(), UMGame.PlayerRole.SPECTATOR))
                                                ).then(
                                                        Commands.literal("hunter")
                                                                .executes(commandContext -> setNewRole(commandContext.getSource(), UMGame.PlayerRole.HUNTER))
                                                )
                                )
                ));
    }

    private static int setNewRole(CommandSourceStack sourceStack, UMGame.PlayerRole playerRole)
    {
        int i = 0;

        if (UMGame.State.hasGameBeenStarted())
        {
            sourceStack.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change how new players are declared when a Manhunt game has already been started!"));
            return i;
        }
        UMGame.setNewPlayerRole(playerRole);
        sourceStack.sendSuccess(() -> Component.literal("Newly-joined players will now be declared as a " + playerRole.getAsName() + " after the Manhunt game has started"), true);
        i++;
        return i;
    }
}
