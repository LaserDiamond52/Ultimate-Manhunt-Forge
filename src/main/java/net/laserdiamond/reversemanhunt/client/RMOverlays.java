package net.laserdiamond.reversemanhunt.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.laserdiamond.reversemanhunt.RMGameState;
import net.laserdiamond.reversemanhunt.ReverseManhunt;
import net.laserdiamond.reversemanhunt.client.game.ClientGameState;
import net.laserdiamond.reversemanhunt.client.game.ClientGameTime;
import net.laserdiamond.reversemanhunt.client.game.ClientHardcore;
import net.laserdiamond.reversemanhunt.client.hunter.ClientHunter;
import net.laserdiamond.reversemanhunt.client.hunter.ClientHunterGracePeriod;
import net.laserdiamond.reversemanhunt.client.hunter.ClientSpeedRunnerDistance;
import net.laserdiamond.reversemanhunt.client.speedrunner.ClientDistanceFromHunter;
import net.laserdiamond.reversemanhunt.client.speedrunner.ClientSpeedRunnerHunterDetection;
import net.laserdiamond.reversemanhunt.client.speedrunner.ClientSpeedRunnerLives;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4fStack;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = ReverseManhunt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RMOverlays implements LayeredDraw.Layer {

    private static final ResourceLocation SPEED_RUNNER_EMPTY_HEART_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore");
    private static final ResourceLocation SPEED_RUNNER_FULL_HEART_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation SPEED_RUNNER_FULL_HEART_HARDCORE_TEXTURE = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full");

    @SubscribeEvent
    public static void registerOverlays(FMLClientSetupEvent event)
    {
        event.enqueueWork(RMOverlays::new);
    }

    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    private RMOverlays()
    {
        LayeredDraw hudOverlay = new LayeredDraw()
                .add(this);

        LayeredDraw detectionOverlay = new LayeredDraw()
                .add(new SpeedRunnerHunterDetectionOverlay());

        LayeredDraw mcLayers = MINECRAFT.gui.layers;
        mcLayers.add(hudOverlay, () -> !MINECRAFT.options.hideGui)
                .add(detectionOverlay, () -> true);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker)
    {
        final float partialTicks = pDeltaTracker.getGameTimeDeltaPartialTick(true);
        LocalPlayer player = MINECRAFT.player;
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
        boolean hasStarted = ClientGameState.hasGameBeenStarted();
        long gameTime = ClientGameTime.getGameTime();

        if (!hasStarted)
        {
            return; // Game has not been started. End method
        }

        RenderSystem.enableBlend();

        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!isHunter) // Is the player a hunter?
        {
            if (lives > 0) // Does the speed runner have lives remaining?
            {
                this.renderSpeedRunnerLives(pGuiGraphics, lives);
            }
        } else // Player is a hunter
        {
            this.renderHunterTracker(pGuiGraphics, player); // Render tracker
        }
        if (gameTime < ClientHunterGracePeriod.getGracePeriodDuration())
        {
            this.renderHunterGracePeriodProgression(pGuiGraphics, isHunter);
        }

        RenderSystem.disableBlend();

    }

    private void renderSpeedRunnerLives(GuiGraphics guiGraphics, int lives)
    {

        // Position for the speed runner lives is right next to
        int drawX = (guiGraphics.guiWidth() / 2 - 104);
        int drawY = (guiGraphics.guiHeight() - 39);

        for (int i = 0; i < RMGameState.SPEED_RUNNER_LIVES; i++)
        {
            guiGraphics.blitSprite(SPEED_RUNNER_EMPTY_HEART_TEXTURE, drawX, drawY - (i * 10), 9, 9);
            if (i < lives)
            {
                if (ClientHardcore.isHardcore()) // Hardcore?
                {
                    guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_HARDCORE_TEXTURE, drawX, drawY - (i * 10), 9, 9);
                } else // Not hardcore
                {
                    guiGraphics.blitSprite(SPEED_RUNNER_FULL_HEART_TEXTURE, drawX, drawY - (i * 10), 9, 9);
                }
            }
        }

    }

    private void renderHunterGracePeriodProgression(GuiGraphics guiGraphics, boolean isHunter)
    {
        int drawX = (guiGraphics.guiWidth() / 2);
        int drawY = (guiGraphics.guiHeight() - 487);

        Component component = getHunterGracePeriodComponent(isHunter);
        guiGraphics.drawCenteredString(MINECRAFT.font, component, drawX, drawY, ChatFormatting.RED.getColor());

    }

    @NotNull
    private Component getHunterGracePeriodComponent(boolean isHunter) {

        double seconds = (double) (ClientHunterGracePeriod.getGracePeriodDuration() - ClientGameTime.getGameTime()) / 20;

        DecimalFormat format = new DecimalFormat("0.00");

        if (isHunter)
        {
            return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "You will be released in " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.RED + " seconds");
        } else
        {
            return Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Hunters will be released in " + ChatFormatting.YELLOW + format.format(seconds) + ChatFormatting.RED + " seconds");
        }
    }

    private void renderHunterTracker(GuiGraphics guiGraphics, LocalPlayer player)
    {
        int drawX = guiGraphics.guiWidth() / 2;
        int drawY = guiGraphics.guiHeight() - 77;

        boolean areSpeedRunnersPresent = ClientSpeedRunnerDistance.areSpeedRunnersPresent();
        float distance = ClientSpeedRunnerDistance.getDistance();
        Player trackedPlayer = ClientSpeedRunnerDistance.getTrackedSpeedRunner();

        Camera camera = MINECRAFT.gameRenderer.getMainCamera();

//        Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
//        matrix4fstack.pushMatrix();
//        matrix4fstack.mul(guiGraphics.pose().last().pose());
//        matrix4fstack.translate((float) (guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);
//
//        // Set rotations for default position of the tracker
//        // If the tracker is roughly in this position, you are heading towards the speed runner (line is flat on the screen)
//        matrix4fstack.rotateX((float) ((-(camera.getXRot()) * Mth.DEG_TO_RAD))); // Rotate tracker to locate player on y-axis
//
//        matrix4fstack.rotateY((float) (((135F) * Mth.DEG_TO_RAD)));
//
//        matrix4fstack.scale(-1.0F, -1.0F, -1.0F);
//        RenderSystem.applyModelViewMatrix();
//        renderTrackerLines(30, -16777216, -16711936, true, true);
//
//        matrix4fstack.rotateY((float) (Math.PI / 4));
//        RenderSystem.applyModelViewMatrix();
//        renderTrackerLines(50, -16777216, -65536, false, true);
//
//
//        matrix4fstack.popMatrix();
//        RenderSystem.applyModelViewMatrix();

        DecimalFormat format = new DecimalFormat("0.00");
        if (areSpeedRunnersPresent && trackedPlayer != null)
        {
            guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.GREEN + "Nearest Speed Runner is " + ChatFormatting.YELLOW + format.format(distance) + ChatFormatting.GREEN + " blocks away"), drawX, drawY, ChatFormatting.GREEN.getColor());

            Vec3 speedRunnerPos = trackedPlayer.getEyePosition();
            Vec3 hunterCameraPos = camera.getPosition();

            double cameraDistanceToPlayer = hunterCameraPos.distanceTo(speedRunnerPos);
            double xDif = hunterCameraPos.x - speedRunnerPos.x;
            double yDif = hunterCameraPos.y - speedRunnerPos.y;
            double zDif = hunterCameraPos.z - speedRunnerPos.z;

            double yRot = Math.acos(yDif / cameraDistanceToPlayer) + (Math.PI * 3 / 2); // Angle to track vertical
            double xRotTan = Math.atan2(zDif, -xDif) + (Math.PI); // Angle to track horizontal axis

            if (speedRunnerPos.z < hunterCameraPos.z) // Use z axis to determine if the player is behind us
            {
                yRot *= -1; // Controls tracking vertical (flip arrow)
            }

            Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
            matrix4fstack.pushMatrix();
            matrix4fstack.mul(guiGraphics.pose().last().pose());
            matrix4fstack.translate((float) (guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);

            // Set rotations for default position of the tracker
            // If the tracker is roughly in this position, you are heading towards the speed runner (line is flat on the screen)
            matrix4fstack.rotateX((float) ((-(camera.getXRot()) * Mth.DEG_TO_RAD) + yRot)); // Rotate tracker to locate player on y-axis
            matrix4fstack.rotateY((float) (((45F + camera.getYRot()) * Mth.DEG_TO_RAD) + xRotTan)); // Track on X and Z axis

            matrix4fstack.scale(-1.0F, -1.0F, -1.0F);
            RenderSystem.applyModelViewMatrix();
            renderTrackerLines(30, -16777216, -16711936, true, true);

            matrix4fstack.rotateY((float) (Math.PI / 4));
            matrix4fstack.rotateZ((float) (Math.PI / 2));

            RenderSystem.applyModelViewMatrix();
            renderTrackerLines(50, -16777216, -65536, false, true);

            matrix4fstack.popMatrix();
            RenderSystem.applyModelViewMatrix();

        } else
        {
            guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.RED + "There are no Speed Runners nearby"), drawX, drawY, ChatFormatting.RED.getColor());
        }
    }

    private void renderTrackerLines(int lineLength, int outerColor, int innerColor, boolean drawX, boolean drawZ)
    {
        RenderSystem.assertOnRenderThread();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderSystem.lineWidth(4.0F);

        if (drawX)
        {
            bufferbuilder.addVertex((float) 0, (float) 0, (float) 0).setColor(outerColor).setNormal(1.0F, 0.0F, 0.0F);
            bufferbuilder.addVertex((float) lineLength, (float) 0, (float) 0).setColor(outerColor).setNormal(1.0F, 0.0F, 0.0F);
        }

        if (drawZ)
        {
            bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(outerColor).setNormal(0.0F, 0.0F, 1.0F);
            bufferbuilder.addVertex(0.0F, 0.0F, (float) lineLength).setColor(outerColor).setNormal(0.0F, 0.0F, 1.0F);
        }

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.lineWidth(2.0F);
        bufferbuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        if (drawX)
        {
            bufferbuilder.addVertex((float) 0, (float) 0, (float) 0).setColor(innerColor).setNormal(1.0F, 0.0F, 0.0F);
            bufferbuilder.addVertex((float) lineLength, (float) 0, (float) 0).setColor(innerColor).setNormal(1.0F, 0.0F, 0.0F);
        }

        if (drawZ)
        {
            bufferbuilder.addVertex(0.0F, 0.0F, 0.0F).setColor(innerColor).setNormal(0.0F, 0.0F, 1.0F);
            bufferbuilder.addVertex(0.0F, 0.0F, (float) lineLength).setColor(innerColor).setNormal(0.0F, 0.0F, 1.0F);
        }

        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.lineWidth(1.0F);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
    }

    /**
     * Hunter detection overlay for speed runners
     */
    private static class SpeedRunnerHunterDetectionOverlay implements LayeredDraw.Layer
    {

        @Override
        public void render(GuiGraphics pGuiGraphics, DeltaTracker pDeltaTracker)
        {
            LocalPlayer player = MINECRAFT.player;
            if (player == null)
            {
                return; // Player is null. End method
            }
            if (player.isSpectator())
            {
                return; // Don't display if player is in spectator mode
            }

            boolean isHunter = ClientHunter.isHunter();
            int lives = ClientSpeedRunnerLives.getLives();
            boolean isNearHunter = ClientSpeedRunnerHunterDetection.isNearHunter();
            boolean isRunning = ClientGameState.isGameRunning();
            float distanceFromHunter = ClientDistanceFromHunter.getDistance();

            int width = pGuiGraphics.guiWidth();
            int height = pGuiGraphics.guiHeight();

            RenderSystem.enableBlend();

            RenderSystem.setShader(GameRenderer::getPositionShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (!isRunning) // Is the game running?
            {
                return; // Hasn't started. End
            }

            if (!isHunter) // Is the player a hunter?
            {
                if (lives > 0) // Does the speed runner have lives remaining?
                {
                    if (isNearHunter)
                    {
                        float red = -(distanceFromHunter / 110F) + 0.9F;

                        RenderSystem.disableDepthTest();
                        RenderSystem.depthMask(false);
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                        pGuiGraphics.setColor(red, 0.1F, 0.1F, 1.0F);
                        pGuiGraphics.blit(GameRenderer.NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, width, height, width, height);
                        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.disableBlend();
                        RenderSystem.depthMask(true);
                        RenderSystem.enableDepthTest();
                    }
                }
            }

            RenderSystem.disableBlend();

        }
    }

}
