package net.laserdiamond.reversemanhunt.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.capability.client.ClientHunter;
import net.laserdiamond.reversemanhunt.capability.client.ClientSpeedRunnerLives;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RMOverlays implements LayeredDraw.Layer {

    private static final ResourceLocation SPEED_RUNNER_EMPTY_HEART_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore");
    private static final ResourceLocation SPEED_RUNNER_FULL_HEART_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full");

    @SubscribeEvent
    public static void registerOverlays(FMLClientSetupEvent event)
    {
        event.enqueueWork(RMOverlays::new);
    }

    private final Minecraft minecraft;

    private RMOverlays()
    {
        this.minecraft = Minecraft.getInstance();

        LayeredDraw layeredDraw = new LayeredDraw()
                .add(this);

        LayeredDraw mcLayers = this.minecraft.gui.layers;
        mcLayers.add(layeredDraw, () -> !this.minecraft.options.hideGui);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
        {
            return; // Player is null. End method
        }
        if (player.isSpectator())
        {
            return; // Don't display if player is in spectator mode
        }
        int lives = ClientSpeedRunnerLives.getLives();
        boolean isHunter = ClientHunter.isHunter();
        boolean hasStarted = RMGameState.State.hasGameBeenStarted();

        if (!hasStarted)
        {
            return; // Game has not been started. End method
        }
        if (!isHunter) // Is the player a hunter?
        {
            if (lives != 0) // Does the speed runner have lives remaining?
            {
                this.renderSpeedRunnerLives(pGuiGraphics, lives);
                this.renderSpeedRunnerGracePeriodProgression(player, pGuiGraphics);
            }
        }
        this.renderHunterGracePeriodProgression(pGuiGraphics, isHunter);

    }

    private void renderSpeedRunnerLives(GuiGraphics guiGraphics, int lives)
    {
        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Position for the speed runner lives is right next to
        int drawX = (guiGraphics.guiWidth() / 2 - 104);
        int drawY = (guiGraphics.guiHeight() - 39);

        for (int i = 0; i < RMGameState.SPEED_RUNNER_LIVES; i++)
        {
            guiGraphics.blitSprite(SPEED_RUNNER_EMPTY_HEART_TEXTURE, drawX, drawY - (i * 10), 9, 9);
            if (i <= lives)
            {
                guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_TEXTURE, drawX, drawY - (i * 10), 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }

    private void renderHunterGracePeriodProgression(GuiGraphics guiGraphics, boolean isHunter)
    {
        // TODO: Render progression bar for Hunter's grace period
        // Player should be able to tell how long until the Hunters are released
        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int drawX = (guiGraphics.guiWidth() / 2 - 2);
        int drawY = (guiGraphics.guiHeight() - 487);

        Component component = getHunterGracePeriodComponent(isHunter);
        guiGraphics.drawCenteredString(this.minecraft.font, component, drawX, drawY, ChatFormatting.RED.getColor());

        RenderSystem.disableBlend();
    }

    @NotNull
    private static Component getHunterGracePeriodComponent(boolean isHunter) {
        Component component;

        double seconds = (double) (RMGameState.HUNTER_GRACE_PERIOD_TICKS - RMGameState.getCurrentGameTime()) / 20;

        DecimalFormat format = new DecimalFormat("0.00");

        if (isHunter)
        {
            component = Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "You will be released in " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.RED + " seconds");
        } else
        {
            component = Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Hunters will be released in " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.RED + " seconds");
        }
        return component;
    }

    private void renderSpeedRunnerGracePeriodProgression(LocalPlayer player, GuiGraphics guiGraphics)
    {
        // TODO: Render progression bar for Hunter's grace period
        // Player should be able to tell how long their grace period will last
    }

}
