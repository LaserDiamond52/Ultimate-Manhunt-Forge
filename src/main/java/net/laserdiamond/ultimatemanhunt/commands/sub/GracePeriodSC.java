package net.laserdiamond.ultimatemanhunt.commands.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GracePeriodSC extends UltimateManhuntCommands.SubCommand {

    public GracePeriodSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("gracePeriod")
                                .requires(UltimateManhunt::hasPermission)
                                .then(
                                        Commands.literal("hunter")
                                                .then(
                                                        Commands.argument("durationTicks", IntegerArgumentType.integer(1))
                                                                .executes(commandContext -> setGracePeriod(commandContext, Team.HUNTERS, IntegerArgumentType.getInteger(commandContext, "durationTicks")))
                                                )
                                )
                                .then(
                                        Commands.literal("speed_runner")
                                                .then(
                                                        Commands.argument("durationTicks", IntegerArgumentType.integer(1))
                                                                .executes(commandContext -> setGracePeriod(commandContext, Team.SPEED_RUNNERS, IntegerArgumentType.getInteger(commandContext, "durationTicks")))
                                                )
                                )
                ));
    }

    private static void logGracePeriodChange(CommandSourceStack source, @NotNull Team team, int newDuration)
    {
        String teamName = switch (team)
        {
            case HUNTERS -> "Hunters";
            case SPEED_RUNNERS -> "Speed Runners";
        };
        source.sendSuccess(() -> Component.literal("Set " + teamName + " grace period to: " + newDuration + " ticks"), true);
    }

    private static void logFailGracePeriodChange(CommandSourceStack source, FailReason reason, int newDuration)
    {
        switch (reason)
        {
            case GAME_STARTED ->
            {
                source.sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the hunter's grace period while the game is active!"));
            }
            case INVALID_VALUE ->
            {
                source.sendFailure(Component.literal(ChatFormatting.RED + "Hunter grace period must be greater than 0 ticks long. " +
                        "\n Value entered: " + newDuration));
            }
        }
    }

    private static int setGracePeriod(CommandContext<CommandSourceStack> commandContext, @NotNull Team team, int newDuration)
    {
        int i = 0;
        if (UMGame.State.hasGameBeenStarted())
        {
            logFailGracePeriodChange(commandContext.getSource(), FailReason.GAME_STARTED, newDuration);
            return 0;
        }
        if (newDuration <= 0)
        {
            logFailGracePeriodChange(commandContext.getSource(), FailReason.INVALID_VALUE, newDuration);
            return 0;
        }
        switch (team)
        {
            case HUNTERS ->
            {
                UMGame.setHunterGracePeriod(newDuration);
                logGracePeriodChange(commandContext.getSource(), team, newDuration);
            }
            case SPEED_RUNNERS ->
            {
                UMGame.setSpeedRunnerGracePeriod(newDuration);
                logGracePeriodChange(commandContext.getSource(), team, newDuration);
            }
        }

        return i;
    }

    private enum Team
    {
        HUNTERS,
        SPEED_RUNNERS;
    }

    private enum FailReason
    {
        GAME_STARTED,
        INVALID_VALUE;
    }
}
