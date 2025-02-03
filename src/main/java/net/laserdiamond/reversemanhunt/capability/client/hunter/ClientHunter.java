package net.laserdiamond.reversemanhunt.capability.client.hunter;

public class ClientHunter {

    private static boolean hunter;
    private static boolean buffed;

    public static void setHunter(boolean isHunter)
    {
        ClientHunter.hunter = isHunter;
    }

    public static boolean isHunter()
    {
        return ClientHunter.hunter;
    }

    public static void setBuffed(boolean isBuffedHunter)
    {
        ClientHunter.buffed = isBuffedHunter;
    }

    public static boolean isBuffedHunter()
    {
        return ClientHunter.buffed;
    }
}
