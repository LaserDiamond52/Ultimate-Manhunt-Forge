package net.laserdiamond.reversemanhunt.capability.client.speedrunner;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientSpeedRunners {

    private static final List<Player> speedRunners = new ArrayList<>();

    public static void addSpeedRunner(Player player)
    {
        ClientSpeedRunners.speedRunners.add(player);

    }

    public static void removeSpeedRunner(Player player)
    {
        ClientSpeedRunners.speedRunners.remove(player);
    }

    public static List<Player> getSpeedRunners()
    {
        return ClientSpeedRunners.speedRunners;
    }


}
