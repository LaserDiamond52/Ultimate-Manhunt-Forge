package net.laserdiamond.ultimatemanhunt.util.file;

import com.google.gson.JsonObject;
import net.laserdiamond.ultimatemanhunt.client.ClientSettings;

public final class ClientConfig extends JsonConfig {

    private static JsonObject createVolumeJson(ClientSettings.FloatBooleanController controller)
    {
        JsonObject j = new JsonObject();
        j.addProperty("volume", controller.getValue());
        j.addProperty("enabled", controller.isEnabled());
        return j;
    }

    private static JsonObject createIntensityJson(ClientSettings.FloatBooleanController controller)
    {
        JsonObject j = new JsonObject();
        j.addProperty("intensity", controller.getValue());
        j.addProperty("enabled", controller.isEnabled());
        return j;
    }

    private static final ClientConfig INSTANCE = new ClientConfig();

    public static ClientConfig getClientConfig()
    {
        return INSTANCE;
    }

    private ClientConfig()
    {
        super("settings");
    }

    @Override
    protected String folderName() {
        return "client";
    }

    @Override
    public boolean saveSettingsToFile()
    {
        JsonObject gameStart = createVolumeJson(ClientSettings.START_GAME_VOLUME);
        JsonObject gamePause = createVolumeJson(ClientSettings.PAUSE_GAME_VOLUME);
        JsonObject gameResume = createVolumeJson(ClientSettings.RESUME_GAME_VOLUME);
        JsonObject gameEnd = createVolumeJson(ClientSettings.END_GAME_VOLUME);

        JsonObject huntersReleased = createVolumeJson(ClientSettings.HUNTERS_RELEASED_VOLUME);

        JsonObject heartBeat = createVolumeJson(ClientSettings.HEART_BEAT_VOLUME);

        JsonObject heartFlatline = createVolumeJson(ClientSettings.HEART_FLATLINE_VOLUME);

        JsonObject prowler = createVolumeJson(ClientSettings.PROWLER_VOLUME);

        JsonObject hunterVignette = createIntensityJson(ClientSettings.HUNTER_VIGNETTE);

        JsonObject gracePeriodVignette = createIntensityJson(ClientSettings.GRACE_PERIOD_VIGNETTE);

        this.jsonObject.add("game_start", gameStart);
        this.jsonObject.add("game_pause", gamePause);
        this.jsonObject.add("game_resume", gameResume);
        this.jsonObject.add("game_end", gameEnd);
        this.jsonObject.add("hunters_released", huntersReleased);
        this.jsonObject.add("heart_beat", heartBeat);
        this.jsonObject.add("heart_flatline", heartFlatline);
        this.jsonObject.add("prowler_theme", prowler);
        this.jsonObject.add("hunter_vignette", hunterVignette);
        this.jsonObject.add("grace_period_vignette", gracePeriodVignette);
        this.jsonObject.addProperty("is_3d_tracker_enabled", ClientSettings.PLAYER_TRACKER.isEnabled());

        return this.writeJsonToFile();
    }

    @Override
    public void applySettingsToGame()
    {
        ClientSettings.START_GAME_VOLUME.setValue(this.getGameStartVolume());
        ClientSettings.START_GAME_VOLUME.setEnabled(this.getIsGameStartEnabled());

        ClientSettings.PAUSE_GAME_VOLUME.setValue(this.getGamePauseVolume());
        ClientSettings.PAUSE_GAME_VOLUME.setEnabled(this.getIsGamePauseEnabled());

        ClientSettings.RESUME_GAME_VOLUME.setValue(this.getGameResumeVolume());
        ClientSettings.RESUME_GAME_VOLUME.setEnabled(this.getIsGameResumeEnabled());

        ClientSettings.END_GAME_VOLUME.setValue(this.getGameEndVolume());
        ClientSettings.END_GAME_VOLUME.setEnabled(this.getIsGameEndEnabled());

        ClientSettings.HUNTERS_RELEASED_VOLUME.setValue(this.getHuntersReleasedVolume());
        ClientSettings.HUNTERS_RELEASED_VOLUME.setEnabled(this.getIsHuntersReleasedEnabled());

        ClientSettings.HEART_BEAT_VOLUME.setValue(this.getHeartBeatVolume());
        ClientSettings.HEART_BEAT_VOLUME.setEnabled(this.getIsHeartBeatEnabled());

        ClientSettings.HEART_FLATLINE_VOLUME.setValue(this.getHeartFlatlineVolume());
        ClientSettings.HEART_FLATLINE_VOLUME.setEnabled(this.getIsHeartFlatlineEnabled());

        ClientSettings.HEART_FLATLINE_VOLUME.setValue(this.getProwlerVolume());
        ClientSettings.HEART_FLATLINE_VOLUME.setEnabled(this.getIsProwlerEnabled());

        ClientSettings.HUNTER_VIGNETTE.setValue(this.getHunterVignetteIntensity());
        ClientSettings.HUNTER_VIGNETTE.setEnabled(this.getIsHunterVignetteEnabled());

        ClientSettings.HUNTER_VIGNETTE.setValue(this.getGracePeriodVignetteIntensity());
        ClientSettings.HUNTER_VIGNETTE.setEnabled(this.getIsGracePeriodVignetteEnabled());

        ClientSettings.PLAYER_TRACKER.setEnabled(this.getIs3DTrackerEnabled());
    }

    public float getGameStartVolume()
    {
        if (this.isJsonNotNull("game_start"))
        {
            return this.jsonObject.get("game_start").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.START_GAME_VOLUME.getValue();
    }

    public boolean getIsGameStartEnabled()
    {
        if (this.isJsonNotNull("game_start"))
        {
            return this.jsonObject.get("game_start").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.START_GAME_VOLUME.isEnabled();
    }

    public float getGamePauseVolume()
    {
        if (this.isJsonNotNull("game_pause"))
        {
            return this.jsonObject.get("game_pause").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.PAUSE_GAME_VOLUME.getValue();
    }

    public boolean getIsGamePauseEnabled()
    {
        if (this.isJsonNotNull("game_pause"))
        {
            return this.jsonObject.get("game_pause").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.PAUSE_GAME_VOLUME.isEnabled();
    }

    public float getGameResumeVolume()
    {
        if (this.isJsonNotNull("game_resume"))
        {
            return this.jsonObject.get("game_resume").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.RESUME_GAME_VOLUME.getValue();
    }

    public boolean getIsGameResumeEnabled()
    {
        if (this.isJsonNotNull("game_resume"))
        {
            return this.jsonObject.get("game_resume").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.RESUME_GAME_VOLUME.isEnabled();
    }

    public float getGameEndVolume()
    {
        if (this.isJsonNotNull("game_end"))
        {
            return this.jsonObject.get("game_end").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.END_GAME_VOLUME.getValue();
    }

    public boolean getIsGameEndEnabled()
    {
        if (this.isJsonNotNull("game_end"))
        {
            return this.jsonObject.get("game_end").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.END_GAME_VOLUME.isEnabled();
    }

    public float getHuntersReleasedVolume()
    {
        if (this.isJsonNotNull("hunters_released"))
        {
            return this.jsonObject.get("hunters_released").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.HUNTERS_RELEASED_VOLUME.getValue();
    }

    public boolean getIsHuntersReleasedEnabled()
    {
        if (this.isJsonNotNull("hunters_released"))
        {
            return this.jsonObject.get("hunters_released").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.HUNTERS_RELEASED_VOLUME.isEnabled();
    }

    public float getHeartBeatVolume()
    {
        if (this.isJsonNotNull("heart_beat"))
        {
            return this.jsonObject.get("heart_beat").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.HEART_BEAT_VOLUME.getValue();
    }

    public boolean getIsHeartBeatEnabled()
    {
        if (this.isJsonNotNull("heart_beat"))
        {
            return this.jsonObject.get("heart_beat").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.HEART_BEAT_VOLUME.isEnabled();
    }

    public float getHeartFlatlineVolume()
    {
        if (this.isJsonNotNull("heart_flatline"))
        {
            return this.jsonObject.get("heart_flatline").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.HEART_FLATLINE_VOLUME.getValue();
    }

    public boolean getIsHeartFlatlineEnabled()
    {
        if (this.isJsonNotNull("heart_flatline"))
        {
            return this.jsonObject.get("heart_flatline").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.HEART_FLATLINE_VOLUME.isEnabled();
    }

    public float getProwlerVolume()
    {
        if (this.isJsonNotNull("prowler_theme"))
        {
            return this.jsonObject.get("prowler_theme").getAsJsonObject().get("volume").getAsFloat();
        }
        return ClientSettings.PROWLER_VOLUME.getValue();
    }

    public boolean getIsProwlerEnabled()
    {
        if (this.isJsonNotNull("prowler_theme"))
        {
            return this.jsonObject.get("prowler_theme").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.PROWLER_VOLUME.isEnabled();
    }

    public float getHunterVignetteIntensity()
    {
        if (this.isJsonNotNull("hunter_vignette"))
        {
            return this.jsonObject.get("hunter_vignette").getAsJsonObject().get("intensity").getAsFloat();
        }
        return ClientSettings.HUNTER_VIGNETTE.getValue();
    }

    public boolean getIsHunterVignetteEnabled()
    {
        if (this.isJsonNotNull("hunter_vignette"))
        {
            return this.jsonObject.get("hunter_vignette").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.HUNTER_VIGNETTE.isEnabled();
    }

    public float getGracePeriodVignetteIntensity()
    {
        if (this.isJsonNotNull("grace_period_vignette"))
        {
            return this.jsonObject.get("grace_period_vignette").getAsJsonObject().get("intensity").getAsFloat();
        }
        return ClientSettings.GRACE_PERIOD_VIGNETTE.getValue();
    }

    public boolean getIsGracePeriodVignetteEnabled()
    {
        if (this.isJsonNotNull("grace_period_vignette"))
        {
            return this.jsonObject.get("grace_period_vignette").getAsJsonObject().get("enabled").getAsBoolean();
        }
        return ClientSettings.GRACE_PERIOD_VIGNETTE.isEnabled();
    }

    public boolean getIs3DTrackerEnabled()
    {
        if (this.isJsonNotNull("is_3d_tracker_enabled"))
        {
            return this.jsonObject.get("is_3d_tracker_enabled").getAsBoolean();
        }
        return ClientSettings.PLAYER_TRACKER.isEnabled();
    }
}
