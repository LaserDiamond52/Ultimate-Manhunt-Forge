package net.laserdiamond.reversemanhunt.client.speedrunner;

public class ClientDistanceFromHunter {

    private static float distance;

    public static void setDistance(float distance)
    {
        ClientDistanceFromHunter.distance = distance;
    }

    public static float getDistance()
    {
        return ClientDistanceFromHunter.distance;
    }
}
