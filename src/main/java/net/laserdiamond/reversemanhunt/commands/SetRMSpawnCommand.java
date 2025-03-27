package net.laserdiamond.reversemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.laserutils.util.raycast.ServerRayCast;
import net.laserdiamond.reversemanhunt.RMGame;
import net.laserdiamond.reversemanhunt.event.ForgeServerEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SetRMSpawnCommand {

    private static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("rm_spawn_location")
                        .requires(sourceStack -> ForgeServerEvents.permission(sourceStack, PERMISSION_LEVEL))
                        .then(
                                Commands.argument("location", Vec2Argument.vec2())
                                        .executes(commandContext -> modifyRMSpawn(commandContext, Vec2Argument.getVec2(commandContext, "location"), true))
                                        .then(
                                                Commands.argument("moveWorldSpawn", BoolArgumentType.bool())
                                                        .executes(commandContext -> modifyRMSpawn(commandContext, Vec2Argument.getVec2(commandContext, "location"), BoolArgumentType.getBool(commandContext, "moveWorldSpawn")))
                                        )
                        )

        );
    }

    private static int modifyRMSpawn(CommandContext<CommandSourceStack> commandContext, Vec2 pos, boolean moveWorldSpawn)
    {
        int i = 0;

        float x = pos.x;
        float z = pos.y;

        ServerLevel sl = commandContext.getSource().getLevel();

        if (sl.dimension() != Level.OVERWORLD)
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot set the spawn point of the Reverse Manhunt game outside of the Overworld!"));
            return i;
        }

        if (RMGame.State.hasGameBeenStarted())
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change the Reverse Manhunt game spawn when a game has already been started!"));
            return i;
        }

        ServerRayCast<Player, Double, Double> src = ServerRayCast.create(sl, new Vec3(x, sl.getMaxBuildHeight() + 2, z), Entity::isAttackable, Player.class, List.of());
        src.setCanPierceEntities()
                .setStepIncrement(1)
                .fireAtVec3D(new Vec3(x, sl.getMinBuildHeight(), z), 0);

        Vec3 lastPos = src.getCurrentPosition();
        RMGame.setXAndZSpawnCoordinate((int) x, (int) z);
        BlockPos spawnPos = new BlockPos(((int) lastPos.x), ((int) lastPos.y), ((int) lastPos.z));
        commandContext.getSource().sendSuccess(() -> Component.literal("Set Reverse Manhunt spawn to: X:" + spawnPos.getX() + ", Y: " + spawnPos.getY() + ", Z: " + spawnPos.getZ()), true);

        if (moveWorldSpawn)
        {
            sl.setDefaultSpawnPos(spawnPos, 0);
            commandContext.getSource().sendSuccess(() -> Component.translatable("commands.setworldspawn.success", spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0), true);
        }


        i++;

        return i;
    }
}
