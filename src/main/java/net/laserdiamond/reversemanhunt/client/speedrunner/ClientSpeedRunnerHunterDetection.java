package net.laserdiamond.reversemanhunt.client.speedrunner;

public class ClientSpeedRunnerHunterDetection {

    private static boolean isNearHunter;
    private static int soundTime;

    public static void setIsNearHunter(boolean isNearHunter)
    {
        ClientSpeedRunnerHunterDetection.isNearHunter = isNearHunter;
    }

    public static boolean isNearHunter()
    {
        return ClientSpeedRunnerHunterDetection.isNearHunter;
    }

    public static void setSoundTime(int soundTime)
    {
        ClientSpeedRunnerHunterDetection.soundTime = soundTime;
    }

    public static int getSoundTime()
    {
        return ClientSpeedRunnerHunterDetection.soundTime;
    }

}
