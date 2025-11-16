package net.laserdiamond.ultimatemanhunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.laserdiamond.laserutils.util.registry.RegistryMap;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class UltimateManhuntCommands {

    private static final RegistryMap<ResourceLocation, Function<LiteralArgumentBuilder<CommandSourceStack>, SubCommand>> ARGUMENT_BUILDER_REGISTRY_MAP = new RegistryMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(UltimateManhunt.MODID).requires(UltimateManhunt::hasPermission);
        addArgs(command);
        dispatcher.register(command);
    }

    private static void addArgs(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder)
    {
        for (Function<LiteralArgumentBuilder<CommandSourceStack>, SubCommand> commandFunction : UltimateManhuntCommands.ARGUMENT_BUILDER_REGISTRY_MAP.getMap().values())
        {
            commandFunction.apply(argumentBuilder);
        }
    }

    public static void registerSubCommand(ResourceLocation resourceLocation, Function<LiteralArgumentBuilder<CommandSourceStack>, SubCommand> subCommandFunction)
    {
        try {
            ARGUMENT_BUILDER_REGISTRY_MAP.addEntry(resourceLocation, subCommandFunction);
        } catch (IllegalArgumentException e)
        {
            UltimateManhunt.LOGGER.warn("Command already built");
        }

    }

    public static class SubCommand
    {
        protected final LiteralArgumentBuilder<CommandSourceStack> argumentBuilder;

        public SubCommand(LiteralArgumentBuilder<CommandSourceStack> argumentBuilder)
        {
            this.argumentBuilder = argumentBuilder;
        }
    }
}
