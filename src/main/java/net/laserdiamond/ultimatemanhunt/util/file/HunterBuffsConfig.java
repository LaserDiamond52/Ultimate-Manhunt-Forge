package net.laserdiamond.ultimatemanhunt.util.file;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.io.File;

public final class HunterBuffsConfig extends JsonConfig {

    public static boolean doesHunterBuffFileExist(String fileName)
    {
        File file = new File(UltimateManhunt.MODID + File.separator + "hunter_buffs" + File.separator + fileName + ".json");
        return file.exists();
    }

    public HunterBuffsConfig(String fileName) {
        super(fileName);
    }

    public boolean saveSettingsToFile()
    {
        JsonObject buffedHunter = new JsonObject();

        JsonObject health = new JsonObject();
        health.addProperty("value", UMPlayer.getMaxHealthBonus());
        health.addProperty("modifier", UMPlayer.getMaxHealthModifier().toValue());

        JsonObject armor = new JsonObject();
        armor.addProperty("value", UMPlayer.getArmorBonus());
        armor.addProperty("modifier", UMPlayer.getArmorBonusModifier().toValue());

        JsonObject speed = new JsonObject();
        speed.addProperty("value", UMPlayer.getMovementSpeedBonus());
        speed.addProperty("modifier", UMPlayer.getMovementSpeedBonusModifier().toValue());

        JsonObject damage = new JsonObject();
        damage.addProperty("value", UMPlayer.getAttackDamageBonus());
        damage.addProperty("modifier", UMPlayer.getAttackDamageBonusModifier().toValue());

        JsonObject saturation = new JsonObject();
        saturation.addProperty("value", UMPlayer.getHasInfiniteSaturation());

        JsonObject regen = new JsonObject();
        regen.addProperty("value", UMPlayer.getPassiveRegen());

        buffedHunter.add("health", health);
        buffedHunter.add("armor", armor);
        buffedHunter.add("speed", speed);
        buffedHunter.add("damage", damage);
        buffedHunter.add("saturation", saturation);
        buffedHunter.add("passive_regen", regen);

        this.jsonObject.add("buffed_hunter", buffedHunter);

        return this.writeJsonToFile();
    }

    public void applySettingsToGame()
    {
        UMPlayer.setMaxHealthBonus(this.getMaxHealthBonus());
        UMPlayer.setMaxHealthBonusModifier(this.getMaxHealthBonusModifier());

        UMPlayer.setArmorBonus(this.getArmorBonus());
        UMPlayer.setArmorBonusModifier(this.getArmorBonusModifier());

        UMPlayer.setMovementSpeedBonus(this.getMovementSpeedBonus());
        UMPlayer.setMovementSpeedBonusModifier(this.getMovementSpeedBonusModifier());

        UMPlayer.setAttackDamageBonus(this.getAttackDamageBonus());
        UMPlayer.setAttackDamageBonusModifier(this.getAttackDamageBonusModifier());

        UMPlayer.setHasInfiniteSaturation(this.getHasInfiniteSaturation());
        UMPlayer.setPassiveRegen(this.getPassiveRegen());
    }

    public double getMaxHealthBonus()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get("health").getAsJsonObject().get("value").getAsDouble();
        }
        return UMPlayer.getMaxHealthBonus();
    }

    public AttributeModifier.Operation getMaxHealthBonusModifier()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return AttributeModifier.Operation.fromValue(this.jsonObject.get("buffed_hunter").getAsJsonObject().get("health").getAsJsonObject().get("modifier").getAsInt());
        }
        return UMPlayer.getMaxHealthModifier();
    }

    public double getArmorBonus()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get("armor").getAsJsonObject().get("value").getAsDouble();
        }
        return UMPlayer.getArmorBonus();
    }

    public AttributeModifier.Operation getArmorBonusModifier()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return AttributeModifier.Operation.fromValue(this.jsonObject.get("buffed_hunter").getAsJsonObject().get("armor").getAsJsonObject().get("modifier").getAsInt());
        }
        return UMPlayer.getArmorBonusModifier();
    }

    public double getMovementSpeedBonus()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get("speed").getAsJsonObject().get("value").getAsDouble();
        }
        return UMPlayer.getMovementSpeedBonus();
    }

    public AttributeModifier.Operation getMovementSpeedBonusModifier()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return AttributeModifier.Operation.fromValue(this.jsonObject.get("buffed_hunter").getAsJsonObject().get("speed").getAsJsonObject().get("modifier").getAsInt());
        }
        return UMPlayer.getMovementSpeedBonusModifier();
    }

    public double getAttackDamageBonus()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get("damage").getAsJsonObject().get("value").getAsDouble();
        }
        return UMPlayer.getAttackDamageBonus();
    }

    public AttributeModifier.Operation getAttackDamageBonusModifier()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return AttributeModifier.Operation.fromValue(this.jsonObject.get("buffed_hunter").getAsJsonObject().get("damage").getAsJsonObject().get("modifier").getAsInt());
        }
        return UMPlayer.getAttackDamageBonusModifier();
    }

    public boolean getHasInfiniteSaturation()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get("saturation").getAsJsonObject().get("value").getAsBoolean();
        }
        return UMPlayer.getHasInfiniteSaturation();
    }

    public float getPassiveRegen()
    {
        if (this.isJsonNotNull("buffed_hunter"))
        {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get("passive_regen").getAsJsonObject().get("value").getAsFloat();
        }
        return UMPlayer.getPassiveRegen();
    }

    @Override
    protected String folderName() {
        return "hunter_buffs";
    }
}
