package net.laserdiamond.ultimatemanhunt.commands.playerrole;

import com.mojang.brigadier.CommandDispatcher;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetNewPlayerRoleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("um_new_player_role")
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
        );
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
