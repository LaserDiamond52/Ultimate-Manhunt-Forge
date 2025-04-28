package net.laserdiamond.ultimatemanhunt.commands.playerrole;

import com.mojang.brigadier.CommandDispatcher;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetDeadPlayerRoleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("um_dead_player_role")
                        .requires(UltimateManhunt::hasPermission)
                        .then(
                                Commands.literal("spectator")
                                        .executes(commandContext -> setDeadPlayerRole(commandContext.getSource(), UMGame.PlayerRole.SPECTATOR))
                        )
                        .then(
                                Commands.literal("hunter")
                                        .executes(commandContext -> setDeadPlayerRole(commandContext.getSource(), UMGame.PlayerRole.HUNTER))
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
