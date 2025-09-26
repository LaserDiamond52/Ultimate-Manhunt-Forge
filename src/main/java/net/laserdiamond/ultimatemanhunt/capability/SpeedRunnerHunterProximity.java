package net.laserdiamond.ultimatemanhunt.capability;

import net.laserdiamond.laserutils.util.raycast.AbstractRayCast;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.network.UMPackets;
import net.laserdiamond.ultimatemanhunt.network.packet.speedrunner.SpeedRunnerDistanceFromHunterS2CPacket;
import net.laserdiamond.ultimatemanhunt.sound.UMSoundEvents;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = UltimateManhunt.MODID)
public class SpeedRunnerHunterProximity {

    /**
     * Detection range for hunters for speed runners
     */
    public static final int HUNTER_DETECTION_RANGE = 50;

    @SubscribeEvent
    public static void trackPlayerDistancesTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.START || event.side == LogicalSide.CLIENT || !UMGame.State.isGameRunning())
        {
            return;
        }

        Level level = player.level();
        if (level.isClientSide)
        {
            return;
        }
        MinecraftServer mcServer = player.getServer();
        if (mcServer == null)
        {
            return;
        }
        player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
        {
            if (umPlayer.isSpeedRunner())
            {
                if (!player.isAlive() || umPlayer.isSpeedRunnerOnGracePeriodServer())
                {
                    SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterPlayer(player);
                    // Player is dead, so they are no longer near a hunter
                    // Player could also be on grace period, so do not continue
                    return;
                }
                float distanceToNearestHunter = getDistanceToClosestHunter(player); // Get the distance to the closest hunter for the speed runner
                if (distanceToNearestHunter != -1) // Is there a nearby hunter?
                {
                    if (mcServer.getTickCount() % 5 != 0) // Time to update distance from hunter?
                    {
                        UMPackets.sendToPlayer(new SpeedRunnerDistanceFromHunterS2CPacket(distanceToNearestHunter), player);
                    }
                    if (distanceToNearestHunter < HUNTER_DETECTION_RANGE) // Is the player near a hunter?
                    {
                        if (player instanceof ServerPlayer serverPlayer)
                        {
                            int rate = (int) ((distanceToNearestHunter / 12.5) + 6); // Rate ranges from 6 (closest) to 10 (furthest)
                            if (player.tickCount % rate == 0) // ~180 bpm
                            {
                                serverPlayer.connection.send(new ClientboundSoundPacket(UMSoundEvents.HEART_BEAT.getHolder().get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 100, 1.0F, level.getRandom().nextLong()));
                            }
                            UMSoundEvents.playDetectionSound(player); // Play detection sound
                        }
                    } else if (mcServer.getTickCount() % 5 != 0) // Not close enough to a hunter
                    {
                        UMSoundEvents.stopDetectionSound(player);
                        SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterPlayer(player);
                    }
                } else if (mcServer.getTickCount() % 5 != 0) // Not near a hunter
                {
                    UMSoundEvents.stopDetectionSound(player);
                    SpeedRunnerDistanceFromHunterS2CPacket.sendNotNearHunterPlayer(player);
                }
            }
        });
    }

    private static float getDistanceToClosestHunter(Player speedRunnerPlayer)
    {
        float closestDist = -1;
        Level level = speedRunnerPlayer.level();
        if (level.isClientSide)
        {
            return closestDist;
        }

        for (Player nearbyPlayer : level.getEntitiesOfClass(Player.class, AbstractRayCast.createBBLivingEntity(speedRunnerPlayer, HUNTER_DETECTION_RANGE), player ->
        {
            AtomicBoolean bool = new AtomicBoolean(false);
            player.getCapability(UMPlayerCapability.UM_PLAYER).ifPresent(umPlayer ->
            {
                bool.set(umPlayer.isHunter());
            });
            return bool.get();
        }))
        {
            float distance = speedRunnerPlayer.distanceTo(nearbyPlayer); // Distance from the hunter to the speed runner
            if (closestDist == -1 || distance < closestDist) // Was a previous distance set, or is this distance a closer one?
            {
                closestDist = distance; // This is the closest hunter
            }
        }
        return closestDist;
    }
}
