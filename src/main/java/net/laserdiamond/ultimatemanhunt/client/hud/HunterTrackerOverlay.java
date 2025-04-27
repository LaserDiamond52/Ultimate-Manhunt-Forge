package net.laserdiamond.ultimatemanhunt.client.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.laserdiamond.ultimatemanhunt.capability.UMPlayer;
import net.laserdiamond.ultimatemanhunt.client.game.ClientGameTime;
import net.laserdiamond.ultimatemanhunt.client.hunter.ClientHunterGracePeriod;
import net.laserdiamond.ultimatemanhunt.client.hunter.ClientTrackedSpeedRunner;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fStack;

import java.text.DecimalFormat;
import java.util.UUID;

public final class HunterTrackerOverlay implements UMHUDOverlay {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();


    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, DeltaTracker deltaTracker)
    {
        int drawX = guiGraphics.guiWidth() / 2;
        int drawY = guiGraphics.guiHeight() - 77;

        boolean areSpeedRunnersPresent = ClientTrackedSpeedRunner.areSpeedRunnersPresent();
        float distance = ClientTrackedSpeedRunner.getDistance();
        String trackedPlayerName = ClientTrackedSpeedRunner.getTrackedPlayerName();
        UUID trackedUUID = ClientTrackedSpeedRunner.getTrackedPlayerUUID();
        long gameTime = ClientGameTime.getGameTime();

        Camera camera = MINECRAFT.gameRenderer.getMainCamera();

        DecimalFormat format = new DecimalFormat("0.00");

        if (!umPlayer.isHunter())
        {
            return;
        }
        if (gameTime >= ClientHunterGracePeriod.getGracePeriodDuration())
        {

            if (areSpeedRunnersPresent)
            {
                if (trackedUUID != player.getUUID()) // Do not track self
                {
                    guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.GREEN + trackedPlayerName + " is " + ChatFormatting.YELLOW + format.format(distance) + ChatFormatting.GREEN + " blocks away"), drawX, drawY, ChatFormatting.GREEN.getColor());

                    Vec3 speedRunnerPos = ClientTrackedSpeedRunner.getPosition(); // Set the position to track
                    Vec3 hunterCameraPos = camera.getPosition();

                    double cameraDistanceToPlayer = hunterCameraPos.distanceTo(speedRunnerPos);
                    double xDif = hunterCameraPos.x - speedRunnerPos.x;
                    double yDif = hunterCameraPos.y - speedRunnerPos.y;
                    double zDif = hunterCameraPos.z - speedRunnerPos.z;

                    double yRot = Math.acos(yDif / cameraDistanceToPlayer) + (Math.PI * 3 / 2); // Angle to track vertical
                    double xRotTan = Math.atan2(zDif, -xDif) + (Math.PI); // Angle to track horizontal axis

                    Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
                    matrix4fstack.pushMatrix();
                    matrix4fstack.mul(guiGraphics.pose().last().pose());
                    matrix4fstack.translate((float) (guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);

                    // Set rotations for default position of the tracker
                    // If the tracker is roughly in this position, you are heading towards the speed runner (line is flat on the screen)
                    matrix4fstack.rotateX((float) ((-(camera.getXRot()) * Mth.DEG_TO_RAD) - yRot)); // Rotate tracker to locate player on y-axis
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
                    guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.RED + "This Speed Runner cannot be tracked from your current position"), drawX, drawY, ChatFormatting.RED.getColor());
                }
            } else
            {
//                guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.RED + "There are no Speed Runners nearby"), drawX, drawY, ChatFormatting.RED.getColor());
                guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.RED + "This Speed Runner cannot be tracked from your current position"), drawX, drawY, ChatFormatting.RED.getColor());
            }
        }
    }

    /**
     * Helper method for creating the lines that make up the tracking arrow
     * @param lineLength The length of the line
     * @param outerColor The outline color of the line
     * @param innerColor The inner color of the line
     * @param drawX If a line should be drawn on the x-axis
     * @param drawZ If a line should be drawn on the y-axis
     */
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
}
