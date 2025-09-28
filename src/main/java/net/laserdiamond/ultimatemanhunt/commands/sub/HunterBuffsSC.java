package net.laserdiamond.ultimatemanhunt.commands.sub;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.laserdiamond.ultimatemanhunt.UMGame;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.laserdiamond.ultimatemanhunt.util.file.HunterBuffsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class HunterBuffsSC extends UltimateManhuntCommands.SubCommand {

    public HunterBuffsSC(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder) {
        super(argumentBuilder
                .then(
                        Commands.literal("hunterBuffs")
                                .then(
                                        Commands.literal("setBuffs")
                                                .then(
                                                        createBuffArgs("maxHealth", HunterBuffsSC::setMaxHealthBuff)
                                                )
                                                .then(
                                                        createBuffArgs("armor", HunterBuffsSC::setArmorBuff)
                                                )
                                                .then(
                                                        createBuffArgs("movementSpeed", HunterBuffsSC::setSpeedBuff)
                                                )
                                                .then(
                                                        createBuffArgs("attackDamage", HunterBuffsSC::setAttackDamageBuff)
                                                )
                                                .then(
                                                        Commands.literal("saturation")
                                                                .then(
                                                                        Commands.argument("has_saturation", BoolArgumentType.bool())
                                                                                .executes(context -> setSaturationBuff(context, BoolArgumentType.getBool(context, "has_saturation")))
                                                                )
                                                )
                                                .then(
                                                        Commands.literal("passiveRegen")
                                                                .then(
                                                                        Commands.argument("amount", FloatArgumentType.floatArg(0))
                                                                                .executes(commandContext -> setPassiveRegenBuff(commandContext, FloatArgumentType.getFloat(commandContext, "amount")))
                                                                )
                                                )
                                )
                                .then(
                                        Commands.literal("getBuffs")
                                                .then(
                                                        Commands.argument("hunter_buffs_profile_name", StringArgumentType.string())
                                                                .then(
                                                                        Commands.literal("load")
                                                                                .executes(context -> loadHunterBuffProfile(context, StringArgumentType.getString(context, "hunter_buffs_profile_name")))
                                                                )
                                                                .then(
                                                                        Commands.literal("save")
                                                                                .executes(context -> saveHunterBuffProfile(context, StringArgumentType.getString(context, "hunter_buffs_profile_name")))
                                                                )
                                                                .then(
                                                                        Commands.literal("delete")
                                                                                .executes(context -> deleteHunterBuffProfile(context, StringArgumentType.getString(context, "hunter_buffs_profile_name")))
                                                                )
                                                )
                                )
                ));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createBuffArgs(String name, IBuffFunc buffFunc)
    {
        return Commands.literal(name)
                .then(
                        Commands.argument("amount", DoubleArgumentType.doubleArg())
                                .then(
                                        Commands.literal("modifier")
                                                .then(
                                                        Commands.literal("add")
                                                                .executes(context -> buffFunc.execute(context, DoubleArgumentType.getDouble(context, "amount"), AttributeModifier.Operation.ADDITION))
                                                )
                                                .then(
                                                        Commands.literal("multiplyBase")
                                                                .executes(context -> buffFunc.execute(context, DoubleArgumentType.getDouble(context, "amount"), AttributeModifier.Operation.MULTIPLY_BASE))
                                                )
                                                .then(
                                                        Commands.literal("multiplyTotal")
                                                                .executes(context -> buffFunc.execute(context, DoubleArgumentType.getDouble(context, "amount"), AttributeModifier.Operation.MULTIPLY_TOTAL))
                                                )
                                )
                );
    }

    private static int loadHunterBuffProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;
        if (UMGame.State.hasGameBeenStarted())
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot load Hunter Buff Profiles when a game has already been started!"));
            return i;
        }
        if (!HunterBuffsConfig.doesHunterBuffFileExist(profileName))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Hunter Buff Profile \"" + profileName + "\" does not exist"));
            return i;
        }
        new HunterBuffsConfig(profileName).applySettingsToGame();
        commandContext.getSource().sendSuccess(() -> Component.literal("Applied settings from Hunter Buff Profile \"" + profileName + "\" to game"), true);
        i++;
        return i;
    }

    private static int saveHunterBuffProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;
        if (HunterBuffsConfig.doesHunterBuffFileExist(profileName))
        {
            commandContext.getSource().sendSuccess(() -> Component.literal("Overwrote settings for Hunter Buff Profile \"" + profileName + "\""), true);
            new HunterBuffsConfig(profileName).saveSettingsToFile();
            i++;
            return i;
        }
        new HunterBuffsConfig(profileName).saveSettingsToFile();
        commandContext.getSource().sendSuccess(() -> Component.literal("Saved new Hunter Buff Profile \"" + profileName + "\" to file"), true);
        i++;
        return i;
    }

    private static int deleteHunterBuffProfile(CommandContext<CommandSourceStack> commandContext, String profileName)
    {
        int i = 0;
        if (!HunterBuffsConfig.doesHunterBuffFileExist(profileName))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Hunter Buff Profile \"" + profileName + "\" does not exist"));
            return i;
        }
        new HunterBuffsConfig(profileName).deleteFile();
        commandContext.getSource().sendSuccess(() -> Component.literal("Deleted Hunter Buff Profile \"" + profileName + "\""), true);
        i++;
        return i;
    }

    private static int setMaxHealthBuff(CommandContext<CommandSourceStack> commandContext, double amount, AttributeModifier.Operation operation)
    {
        int i = 0;
        if (!(UMPlayer.setMaxHealthBonus(amount) && UMPlayer.setMaxHealthBonusModifier(operation)))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change hunter buffs when a game has already been started!"));
            return i;
        }
        commandContext.getSource().sendSuccess(() -> Component.literal("Set max health buff to: " + amount + " " + operation.name()), true);
        i++;
        return i;
    }

    private static int setArmorBuff(CommandContext<CommandSourceStack> commandContext, double amount, AttributeModifier.Operation operation)
    {
        int i = 0;
        if (!(UMPlayer.setArmorBonus(amount) && UMPlayer.setArmorBonusModifier(operation)))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change hunter buffs when a game has already been started!"));
            return i;
        }
        commandContext.getSource().sendSuccess(() -> Component.literal("Set armor buff to: " + amount + " " + operation.name()), true);
        i++;
        return i;
    }

    private static int setSpeedBuff(CommandContext<CommandSourceStack> commandContext, double amount, AttributeModifier.Operation operation)
    {
        int i = 0;
        if (!(UMPlayer.setMovementSpeedBonus(amount) && UMPlayer.setMovementSpeedBonusModifier(operation)))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change hunter buffs when a game has already been started!"));
            return i;
        }
        commandContext.getSource().sendSuccess(() -> Component.literal("Set movement speed buff to: " + amount + " " + operation.name()), true);
        i++;
        return i;
    }

    private static int setAttackDamageBuff(CommandContext<CommandSourceStack> commandContext, double amount, AttributeModifier.Operation operation)
    {
        int i = 0;
        if (!(UMPlayer.setAttackDamageBonus(amount) && UMPlayer.setAttackDamageBonusModifier(operation)))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change hunter buffs when a game has already been started!"));
            return i;
        }
        commandContext.getSource().sendSuccess(() -> Component.literal("Set attack damage buff to: " + amount + " " + operation.name()), true);
        i++;
        return i;
    }

    private static int setSaturationBuff(CommandContext<CommandSourceStack> commandContext, boolean hasSaturation)
    {
        int i = 0;
        if (!UMPlayer.setHasInfiniteSaturation(hasSaturation))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change hunter buffs when a game has already been started!"));
            return i;
        }
        commandContext.getSource().sendSuccess(() -> Component.literal("Set hunter saturation to: " + hasSaturation), true);
        i++;
        return i;
    }

    private static int setPassiveRegenBuff(CommandContext<CommandSourceStack> commandContext, float amount)
    {
        int i = 0;
        if (!UMPlayer.setPassiveRegen(amount))
        {
            commandContext.getSource().sendFailure(Component.literal(ChatFormatting.RED + "Cannot change hunter buffs when a game has already been started!"));
            return i;
        }
        commandContext.getSource().sendSuccess(() -> Component.literal("Set hunter passive regen to: " + amount), true);
        i++;
        return i;
    }

    @FunctionalInterface
    private interface IBuffFunc
    {

        int execute(CommandContext<CommandSourceStack> context, double amount, AttributeModifier.Operation operation);
    }

}
