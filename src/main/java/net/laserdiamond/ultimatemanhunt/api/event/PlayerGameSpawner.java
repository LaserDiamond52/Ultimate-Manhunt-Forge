package net.laserdiamond.ultimatemanhunt.api.event;

import com.google.common.base.Predicates;
import net.laserdiamond.laserutils.util.raycast.ServerRayCast;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Interface used for Events that move players into the specified spawn position at the start of the game
 */
public interface PlayerGameSpawner {

    default void spawn(Player player)
    {
        Level level = player.level();
        if (!level.isClientSide) // Ensure we are on the server
        {
            int xSpawn = UMGame.getXSpawnCoordinate();
            int zSpawn = UMGame.getZSpawnCoordinate();
            MinecraftServer mcServer = player.getServer();
            if (mcServer == null)
            {
                return; // End if server is null
            }
            ServerLevel overworld = moveToOverworld(player, mcServer); // Overworld
            int yMax = overworld.getMaxBuildHeight() + 2;
            int yMin = overworld.getMinBuildHeight();
            Set<RelativeMovement> relativeMovements = EnumSet.noneOf(RelativeMovement.class);
            // Ray cast to find spawn position to start the game
            ServerRayCast<Player, Double, Double> src = ServerRayCast.create(overworld, new Vec3(xSpawn, yMax, zSpawn), Predicates.alwaysFalse(), Player.class, List.of());
            src.setCanPierceEntities() // Go through entities
                    .setStepIncrement(1)
                    .fireAtVec3D(new Vec3(xSpawn, yMin, zSpawn), 0); // Fire straight down

            Vec3 lastPos = src.getCurrentPosition(); // Get last position
            player.teleportTo(overworld, lastPos.x, lastPos.y + 1, lastPos.z, relativeMovements, player.getYRot(), player.getXRot()); // teleport player

            if (!(player.isCreative() || player.isSpectator())) // Check that the player is not in creative or spectator
            {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    default ServerLevel moveToOverworld(Player player, MinecraftServer mcServer)
    {
        ServerLevel overworld = mcServer.overworld();

        return overworld;
    }
}
