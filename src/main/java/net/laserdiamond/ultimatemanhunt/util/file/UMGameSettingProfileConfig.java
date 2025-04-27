package net.laserdiamond.ultimatemanhunt.util.file;

import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;

import java.io.File;

public final class UMGameSettingProfileConfig extends JsonConfig
{

    public static boolean doesGameProfileFileExist(String fileName)
    {
        File file = new File(UltimateManhunt.MODID + File.separator + "game_profiles" + File.separator + fileName + ".json");
        return file.exists();
    }

    public UMGameSettingProfileConfig(String fileName)
    {
        super(fileName);
    }

    public boolean saveSettingsToFile()
    {
        this.jsonObject.addProperty("hunter_grace_period_ticks", UMGame.getHunterGracePeriod());
        this.jsonObject.addProperty("speed_runner_grace_period_ticks", UMGame.getSpeedRunnerGracePeriod());
        this.jsonObject.addProperty("friendly_fire", UMGame.isFriendlyFire());
        this.jsonObject.addProperty("hardcore", UMGame.isHardcore());
        this.jsonObject.addProperty("wind_torch_enabled", UMGame.isWindTorchEnabled());
        this.jsonObject.addProperty("buffed_hunters_on_final_death", UMPlayer.getIsBuffedHunterOnFinalDeath());
        this.jsonObject.addProperty("speed_runner_max_lives", UMPlayer.getMaxLives());

        this.jsonObject.addProperty("new_player_role", UMGame.getNewPlayerRole().toString());
        this.jsonObject.addProperty("dead_speed_runner_role", UMGame.getDeadSpeedRunnerRole().toString());

        return this.writeJsonToFile();
    }

    public void applySettingsToGame()
    {
        UMGame.setHunterGracePeriod(this.getHunterGracePeriodTicks());
        UMGame.setSpeedRunnerGracePeriod(this.getSpeedRunnerGracePeriodTicks());
        UMGame.setFriendlyFire(this.getIsFriendlyFireEnabled());
        UMGame.setHardcore(this.getIsHardcore());
        UMGame.setWindTorchEnabled(this.getIsWindTorchEnabled());
        UMPlayer.setIsBuffedHunterOnFinalDeath(this.getIsBuffedHuntersOnFinalDeath());
        UMPlayer.setMaxLives(this.getMaxSpeedRunnerLives());

        UMGame.setNewPlayerRole(this.getNewPlayerRole());
        if (!UMGame.setDeadSpeedRunnerRole(this.getDeadSpeedRunnerRole()))
        {
            UltimateManhunt.LOGGER.info("Dead Speed Runners cannot become Speed Runners again. The role assigned to Dead Speed Runners will not change");
        }
    }

    // TODO: Use default settings if key returns null from file

    public int getHunterGracePeriodTicks()
    {
        if (this.isJsonNotNull("hunter_grace_period_ticks"))
        {
            return this.jsonObject.get("hunter_grace_period_ticks").getAsInt();
        }
        UltimateManhunt.LOGGER.info("Could not find \"hunter_grace_period_ticks\" from file");
        return UMGame.getHunterGracePeriod();
    }

    public int getSpeedRunnerGracePeriodTicks()
    {
        if (this.isJsonNotNull("speed_runner_grace_period_ticks"))
        {
            return this.jsonObject.get("speed_runner_grace_period_ticks").getAsInt();
        }
        UltimateManhunt.LOGGER.info("Could not find \"speed_runner_grace_period_ticks\" from file");
        return UMGame.getSpeedRunnerGracePeriod();
    }

    public boolean getIsFriendlyFireEnabled()
    {
        if (this.isJsonNotNull("friendly_fire"))
        {
            return this.jsonObject.get("friendly_fire").getAsBoolean();
        }
        UltimateManhunt.LOGGER.info("Could not find \"friendly_fire\" from file");
        return UMGame.isFriendlyFire();
    }

    public boolean getIsHardcore()
    {
        if (this.isJsonNotNull("hardcore"))
        {
            return this.jsonObject.get("hardcore").getAsBoolean();
        }
        UltimateManhunt.LOGGER.info("Could not find \"hardcore\" from file");
        return UMGame.isHardcore();
    }

    public boolean getIsWindTorchEnabled()
    {
        if (this.isJsonNotNull("wind_torch_enabled"))
        {
            return this.jsonObject.get("wind_torch_enabled").getAsBoolean();
        }
        UltimateManhunt.LOGGER.info("Could not find \"wind_torch_enabled\" from file");
        return UMGame.isWindTorchEnabled();
    }

    public boolean getIsBuffedHuntersOnFinalDeath()
    {
        if (this.isJsonNotNull("buffed_hunters_on_final_death"))
        {
            return this.jsonObject.get("buffed_hunters_on_final_death").getAsBoolean();
        }
        UltimateManhunt.LOGGER.info("Could not find \"buffed_hunters_on_final_death\" from file");
        return UMPlayer.getIsBuffedHunterOnFinalDeath();
    }

    public int getMaxSpeedRunnerLives()
    {
        if (this.isJsonNotNull("speed_runner_max_lives"))
        {
            return this.jsonObject.get("speed_runner_max_lives").getAsInt();
        }
        UltimateManhunt.LOGGER.info("Could not find \"speed_runner_max_lives\" from file");
        return UMPlayer.getMaxLives();
    }

    public UMGame.PlayerRole getNewPlayerRole()
    {
        if (this.isJsonNotNull("new_player_role"))
        {
            String value = this.jsonObject.get("new_player_role").getAsString();
            UMGame.PlayerRole role = UMGame.PlayerRole.fromString(value);
            if (role == null)
            {
                UltimateManhunt.LOGGER.info("Role \"" + value + "\" does not exist. The role assigned to New Players will not change");
                role = UMGame.getNewPlayerRole();
            }
            return role;
        }
        UltimateManhunt.LOGGER.info("Could not find \"new_player_role\" from file");
        return UMGame.getNewPlayerRole();
    }

    public UMGame.PlayerRole getDeadSpeedRunnerRole()
    {
        if (this.isJsonNotNull("dead_speed_runner_role"))
        {
            String value = this.jsonObject.get("dead_speed_runner_role").getAsString();
            UMGame.PlayerRole role = UMGame.PlayerRole.fromString(value);
            if (role == null)
            {
                UltimateManhunt.LOGGER.info("Role \"" + value + "\" does not exist. The role assigned to Dead Speed Runners will not change");
                role = UMGame.getDeadSpeedRunnerRole();
            }
            return role;
        }
        UltimateManhunt.LOGGER.info("Could not find \"dead_player_role\" from file");
        return UMGame.getDeadSpeedRunnerRole();
    }

    @Override
    protected String folderName() {
        return "game_profiles";
    }
}
