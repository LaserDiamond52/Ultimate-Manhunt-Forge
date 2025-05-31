package net.laserdiamond.ultimatemanhunt.commands.sub;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.laserutils.util.raycast.ServerRayCast;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class SetSpawnCommand extends UltimateManhuntCommands.SubCommand {

    public SetSpawnCommand(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("setSpawn")
                                .then(
                                        Commands.argument("location", Vec2Argument.vec2())
                                                .executes(commandContext -> modifySpawn(commandContext, Vec2Argument.getVec2(commandContext, "location"), true))
                                                .then(
                                                        Commands.argument("moveWorldSpawn", BoolArgumentType.bool())
                                                                .executes(commandContext -> modifySpawn(commandContext, Vec2Argument.getVec2(commandContext, "location"), BoolArgumentType.getBool(commandContext, "moveWorldSpawn")))
                                                )
                                )
                ));
    }

    private static int modifySpawn(CommandContext<CommandSourceStack> commandContext, Vec2 pos, boolean moveWorldSpawn)
    {
        int i = 0;

        float x = pos.x;
        float z = pos.y;

        ServerLevel sl = commandContext.getSource().getLevel();

        if (sl.dimension() != Level.OVERWORLD)
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot set the spawn point of the Ultimate Manhunt game outside of the Overworld!"));
            return i;
        }

        if (UMGame.State.hasGameBeenStarted())
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the Ultimate Manhunt game spawn when a game has already been started!"));
            return i;
        }

        ServerRayCast<Player, Double, Double> src = ServerRayCast.create(sl, new Vec3(x, sl.getMaxBuildHeight() + 2, z), Entity::isAttackable, Player.class, List.of());
        src.setCanPierceEntities()
                .setStepIncrement(1)
                .fireAtVec3D(new Vec3(x, sl.getMinBuildHeight(), z), 0);

        Vec3 lastPos = src.getCurrentPosition();
        UMGame.setXAndZSpawnCoordinate((int) x, (int) z);
        BlockPos spawnPos = new BlockPos(((int) lastPos.x), ((int) lastPos.y), ((int) lastPos.z));
        commandContext.getSource().sendSuccess(() -> Component.literal("Set Ultimate Manhunt spawn to: X:" + spawnPos.getX() + ", Y: " + spawnPos.getY() + ", Z: " + spawnPos.getZ()), true);

        if (moveWorldSpawn)
        {
            sl.setDefaultSpawnPos(spawnPos, 0);
            commandContext.getSource().sendSuccess(() -> Component.translatable("commands.setworldspawn.success", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0), true);
        }


        i++;

        return i;
    }
}
