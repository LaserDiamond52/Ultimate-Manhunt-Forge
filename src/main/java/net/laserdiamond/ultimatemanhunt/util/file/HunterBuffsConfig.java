package net.laserdiamond.ultimatemanhunt.util.file;

import com.google.gson.JsonObject;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.io.File;

public final class HunterBuffsConfig extends JsonConfig {

    public static boolean doesHunterBuffFileExist(String fileName) {
        File file = new File(UltimateManhunt.MODID + File.separator + "hunter_buffs" + File.separator + fileName + ".json");
        return file.exists();
    }

    private static JsonObject createValueModifierObj(double value, int modifierId)
    {
        JsonObject ret = new JsonObject();
        ret.addProperty("value", value);
        ret.addProperty("modifier", modifierId);
        return ret;
    }

    public HunterBuffsConfig(String fileName) {
        super(fileName);
    }

    public boolean saveSettingsToFile() {
        JsonObject buffedHunter = new JsonObject();

        JsonObject health = createValueModifierObj(UMPlayer.getMaxHealthBonus(), UMPlayer.getMaxHealthModifier().id());

        JsonObject armor = createValueModifierObj(UMPlayer.getArmorBonus(), UMPlayer.getArmorBonusModifier().id());

        JsonObject speed = createValueModifierObj(UMPlayer.getMovementSpeedBonus(), UMPlayer.getMovementSpeedBonusModifier().id());

        JsonObject movementEfficiency = createValueModifierObj(UMPlayer.getMovementEfficiencyBonus(), UMPlayer.getMovementEfficiencyModifier().id());

        JsonObject waterMovementEfficiency = createValueModifierObj(UMPlayer.getWaterMovementEfficiencyBonus(), UMPlayer.getWaterMovementEfficiencyModifier().id());

        JsonObject miningEfficiency = createValueModifierObj(UMPlayer.getMiningEfficiencyBonus(), UMPlayer.getMiningEfficiencyModifier().id());

        JsonObject submergedMiningEfficiency = createValueModifierObj(UMPlayer.getSubmergedMiningEfficiencyBonus(), UMPlayer.getSubmergedMiningEfficiencyModifier().id());

        JsonObject damage = createValueModifierObj(UMPlayer.getAttackDamageBonus(), UMPlayer.getAttackDamageBonusModifier().id());

        JsonObject saturation = new JsonObject();
        saturation.addProperty("value", UMPlayer.getHasInfiniteSaturation());

        JsonObject regen = new JsonObject();
        regen.addProperty("value", UMPlayer.getPassiveRegen());

        buffedHunter.add("health", health);
        buffedHunter.add("armor", armor);
        buffedHunter.add("speed", speed);
        buffedHunter.add("movement_efficiency", movementEfficiency);
        buffedHunter.add("water_movement_efficiency", waterMovementEfficiency);
        buffedHunter.add("mining_efficiency", miningEfficiency);
        buffedHunter.add("submerged_mining_efficiency", submergedMiningEfficiency);
        buffedHunter.add("damage", damage);
        buffedHunter.add("saturation", saturation);
        buffedHunter.add("passive_regen", regen);

        this.jsonObject.add("buffed_hunter", buffedHunter);

        return this.writeJsonToFile();
    }

    public void applySettingsToGame() {
        UMPlayer.setMaxHealthBonus(this.getMaxHealthBonus());
        UMPlayer.setMaxHealthBonusModifier(this.getMaxHealthBonusModifier());

        UMPlayer.setArmorBonus(this.getArmorBonus());
        UMPlayer.setArmorBonusModifier(this.getArmorBonusModifier());

        UMPlayer.setMovementSpeedBonus(this.getMovementSpeedBonus());
        UMPlayer.setMovementSpeedBonusModifier(this.getMovementSpeedBonusModifier());

        UMPlayer.setMovementEfficiencyBonus(this.getMovementEfficiencyBonus());
        UMPlayer.setMovementEfficiencyBonusModifier(this.getMovementEfficiencyBonusModifier());

        UMPlayer.setWaterMovementEfficiencyBonus(this.getWaterMovementEfficiencyBonus());
        UMPlayer.setWaterMovementEfficiencyBonusModifier(this.getWaterMovementEfficiencyBonusModifier());

        UMPlayer.setMiningEfficiencyBonus(this.getMiningEfficiencyBonus());
        UMPlayer.setMiningEfficiencyBonusModifier(this.getMiningEfficiencyBonusModifier());

        UMPlayer.setSubmergedMiningEfficiencyBonus(this.getSubmergedMiningEfficiencyBonus());
        UMPlayer.setSubmergedMiningEfficiencyBonusModifier(this.getSubmergedMiningEfficiencyBonusModifier());

        UMPlayer.setAttackDamageBonus(this.getAttackDamageBonus());
        UMPlayer.setAttackDamageBonusModifier(this.getAttackDamageBonusModifier());

        UMPlayer.setHasInfiniteSaturation(this.getHasInfiniteSaturation());
        UMPlayer.setPassiveRegen(this.getPassiveRegen());
    }

    private double getBuffedHunterDoubleValue(String key, double defaultValue)
    {
        if (this.isJsonNotNull("buffed_hunter")) {
            return this.jsonObject.get("buffed_hunter").getAsJsonObject().get(key).getAsJsonObject().get("value").getAsDouble();
        }
        return defaultValue;
    }

    private AttributeModifier.Operation getBuffedHunterAttributeModifier(String key, AttributeModifier.Operation defaultOperation)
    {
        if (this.isJsonNotNull("buffed_hunter")) {
            return AttributeModifier.Operation.BY_ID.apply(this.jsonObject.get("buffed_hunter").getAsJsonObject().get(key).getAsJsonObject().get("modifier").getAsInt());
        }
        return defaultOperation;
    }

    public double getMaxHealthBonus() {
        return getBuffedHunterDoubleValue("health", UMPlayer.getMaxHealthBonus());
    }

    public AttributeModifier.Operation getMaxHealthBonusModifier() {
        return getBuffedHunterAttributeModifier("health", UMPlayer.getMaxHealthModifier());
    }

    public double getArmorBonus() {
        return getBuffedHunterDoubleValue("armor", UMPlayer.getArmorBonus());
    }

    public AttributeModifier.Operation getArmorBonusModifier() {
        return getBuffedHunterAttributeModifier("armor", UMPlayer.getArmorBonusModifier());
    }

    public double getMovementSpeedBonus() {
        return getBuffedHunterDoubleValue("speed", UMPlayer.getMovementSpeedBonus());
    }

    public AttributeModifier.Operation getMovementSpeedBonusModifier() {
        return getBuffedHunterAttributeModifier("speed", UMPlayer.getMovementSpeedBonusModifier());
    }

    public double getMovementEfficiencyBonus()
    {
        return getBuffedHunterDoubleValue("movement_efficiency", UMPlayer.getMovementEfficiencyBonus());
    }

    public AttributeModifier.Operation getMovementEfficiencyBonusModifier()
    {
        return getBuffedHunterAttributeModifier("movement_efficiency", UMPlayer.getMovementEfficiencyModifier());
    }

    public double getWaterMovementEfficiencyBonus()
    {
        return getBuffedHunterDoubleValue("water_movement_efficiency", UMPlayer.getWaterMovementEfficiencyBonus());
    }

    public AttributeModifier.Operation getWaterMovementEfficiencyBonusModifier()
    {
        return getBuffedHunterAttributeModifier("water_movement_efficiency", UMPlayer.getWaterMovementEfficiencyModifier());
    }

    public double getMiningEfficiencyBonus()
    {
        return getBuffedHunterDoubleValue("mining_efficiency", UMPlayer.getMiningEfficiencyBonus());
    }

    public AttributeModifier.Operation getMiningEfficiencyBonusModifier()
    {
        return getBuffedHunterAttributeModifier("mining_efficiency", UMPlayer.getMiningEfficiencyModifier());
    }

    public double getSubmergedMiningEfficiencyBonus()
    {
        return getBuffedHunterDoubleValue("submerged_mining_efficiency", UMPlayer.getSubmergedMiningEfficiencyBonus());
    }

    public AttributeModifier.Operation getSubmergedMiningEfficiencyBonusModifier()
    {
        return getBuffedHunterAttributeModifier("submerged_mining_efficiency", UMPlayer.getSubmergedMiningEfficiencyModifier());
    }

    public double getAttackDamageBonus() {
        return getBuffedHunterDoubleValue("damage", UMPlayer.getAttackDamageBonus());
    }

    public AttributeModifier.Operation getAttackDamageBonusModifier() {
        return getBuffedHunterAttributeModifier("damage", UMPlayer.getAttackDamageBonusModifier());
    }

    public boolean getHasInfiniteSaturation() {
        if (this.isJsonNotNull("buffed_hunter")) {
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
