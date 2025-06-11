package net.laserdiamond.ultimatemanhunt.api.event;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.laserdiamond.ultimatemanhunt.commands.UltimateManhuntCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Function;

/**
 * {@linkplain Event Event} that is called when registering sub commands for the {@linkplain UltimateManhuntCommands main Manhunt command}
 */
public class RegisterManhuntSubCommandEvent extends Event {

    private final RegisterCommandsEvent registerCommandsEvent;

    public RegisterManhuntSubCommandEvent(RegisterCommandsEvent event)
    {
        this.registerCommandsEvent = event;
    }

    /**
     * Registers a new sub command
     * @param resLoc The {@linkplain ResourceLocation identifier} used to identity the sub command
     * @param subCommandFunction The {@linkplain UltimateManhuntCommands.SubCommand sub command}
     */
    public void registerSubCommand(ResourceLocation resLoc, Function<LiteralArgumentBuilder<CommandSourceStack>, UltimateManhuntCommands.SubCommand> subCommandFunction)
    {
        UltimateManhuntCommands.registerSubCommand(resLoc, subCommandFunction);
    }

    public RegisterCommandsEvent getRegisterCommandsEvent()
    {
        return this.registerCommandsEvent;
    }
}
