package net.laserdiamond.ultimatemanhunt.client.game;

public class ClientRemainingPlayers {

    private static int remainingSpeedRunners;
    private static int remainingHunters;

    public static void setRemainingSpeedRunners(int remainingSpeedRunners)
    {
        ClientRemainingPlayers.remainingSpeedRunners = remainingSpeedRunners;
    }

    public static int getRemainingSpeedRunners() {
        return ClientRemainingPlayers.remainingSpeedRunners;
    }

    public static void setRemainingHunters(int remainingHunters)
    {
        ClientRemainingPlayers.remainingHunters = remainingHunters;
    }

    public static int getRemainingHunters()
    {
        return ClientRemainingPlayers.remainingHunters;
    }
}
