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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.UUID;

public final class HunterTrackerOverlay implements UMHUDOverlay {

    private static final Minecraft MINECRAFT = Minecraft.getInstance();

    @Override
    public void onRender(LocalPlayer player, UMPlayer umPlayer, GuiGraphics guiGraphics, float partialTick)
    {

        int drawX = guiGraphics.guiWidth() / 2;
        int drawY = guiGraphics.guiHeight() - 77;

        boolean areSpeedRunnersPresent = ClientTrackedSpeedRunner.areSpeedRunnersPresent();
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
                    Vec3 hunterCameraPos = camera.getPosition();

                    Vec3 speedRunnerPosLerp = ClientTrackedSpeedRunner.getLerpedSpeedRunnerPosition(partialTick).add(0, ClientTrackedSpeedRunner.getEyeHeight(), 0);

                    Vec3 direction = hunterCameraPos.subtract(speedRunnerPosLerp);
                    double distance = Math.sqrt(Math.pow(direction.x, 2) + Math.pow(direction.y, 2) + Math.pow(direction.z, 2));

                    guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.GREEN + trackedPlayerName + " is " + ChatFormatting.YELLOW + format.format(distance) + ChatFormatting.GREEN + " blocks away"), drawX, drawY, ChatFormatting.GREEN.getColor());

                    double xRot = -Math.acos(direction.y / distance) + (Math.PI * 3 / 2); // Angle to track vertical
                    double yRot = Math.atan2(direction.z, -direction.x) + (Math.PI); // Angle to track horizontal axis

                    PoseStack poseStack = RenderSystem.getModelViewStack();
                    poseStack.pushPose();
                    poseStack.mulPoseMatrix(guiGraphics.pose().last().pose());
                    poseStack.translate((float) (guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);

                    // Set rotations for default position of the tracker
                    // If the tracker is roughly in this position, you are heading towards the speed runner (line is flat on the screen)
                    poseStack.mulPose(Axis.XN.rotationDegrees((float) (camera.getXRot() - (xRot * 180.0F / Math.PI) + 180))); // Rotate tracker to locate player on y-axis
                    poseStack.mulPose(Axis.YP.rotation((float) (((45F + camera.getYRot()) * Mth.DEG_TO_RAD) + yRot))); // Track on X and Z axis

                    poseStack.scale(-1.0F, -1.0F, -1.0F);
                    RenderSystem.applyModelViewMatrix();
                    renderTrackerLines(30, -16777216, -16711936, true, true);

                    poseStack.mulPose(Axis.YP.rotation((float) (Math.PI / 4)));
                    poseStack.mulPose(Axis.ZP.rotation((float) (Math.PI / 2)));

                    RenderSystem.applyModelViewMatrix();
                    renderTrackerLines(50, -16777216, -65536, false, true);

                    poseStack.popPose();
                    RenderSystem.applyModelViewMatrix();
                } else
                {
                    guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.RED + "This Speed Runner cannot be tracked from your current position"), drawX, drawY, ChatFormatting.RED.getColor());
                }
            } else
            {
                guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.RED + "This Speed Runner cannot be tracked from your current position"), drawX, drawY, ChatFormatting.RED.getColor());
            }
        }

    }

    private void testTracker(GuiGraphics guiGraphics, Camera camera)
    {
//        guiGraphics.drawCenteredString(MINECRAFT.font, Component.literal(ChatFormatting.GREEN + trackedPlayerName + " is " + ChatFormatting.YELLOW + format.format(distance) + ChatFormatting.GREEN + " blocks away"), drawX, drawY, ChatFormatting.GREEN.getColor());

        Vec3 speedRunnerPos = new Vec3(-100,100,50); // Set the position to track
        Vec3 hunterCameraPos = camera.getPosition();

        double cameraDistanceToPlayer = hunterCameraPos.distanceTo(speedRunnerPos);
        double xDif = hunterCameraPos.x - speedRunnerPos.x;
        double yDif = hunterCameraPos.y - speedRunnerPos.y;
        double zDif = hunterCameraPos.z - speedRunnerPos.z;

        double yRot = -Math.acos(yDif / cameraDistanceToPlayer) + (Math.PI * 3 / 2); // Angle to track vertical
        double xRotTan = Math.atan2(zDif, -xDif) + (Math.PI); // Angle to track horizontal axis

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.mulPoseMatrix(guiGraphics.pose().last().pose());
        poseStack.translate((float) (guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);

        // Set rotations for default position of the tracker
        // If the tracker is roughly in this position, you are heading towards the speed runner (line is flat on the screen)
        poseStack.mulPose(Axis.XN.rotationDegrees((float) (camera.getXRot() - (yRot * 180.0F / Math.PI) + 180))); // Rotate tracker to locate player on y-axis
        poseStack.mulPose(Axis.YP.rotation((float) (((45F + camera.getYRot()) * Mth.DEG_TO_RAD) + xRotTan))); // Track on X and Z axis

        poseStack.scale(-1.0F, -1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        renderTrackerLines(30, -16777216, -16711936, true, true);

        poseStack.mulPose(Axis.YP.rotation((float) (Math.PI / 4)));
        poseStack.mulPose(Axis.ZP.rotation((float) (Math.PI / 2)));

        RenderSystem.applyModelViewMatrix();
        renderTrackerLines(50, -16777216, -65536, false, true);

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * Helper method for creating the lines that make up the tracking arrow
     *
     * @param lineLength The length of the line
     * @param outerColor The outer color of the line
     * @param innerColor The inner color of the line
     * @param drawX      If a line should be drawn on the x-axis
     * @param drawZ      If a line should be drawn on the y-axis
     */
    private void renderTrackerLines(int lineLength, int outerColor, int innerColor, boolean drawX, boolean drawZ)
    {
        RenderSystem.assertOnRenderThread();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.lineWidth(4.0F);
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        if (drawX)
        {
            bufferbuilder.vertex(0, 0, 0).color(outerColor).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.vertex((float) lineLength, 0, 0).color(outerColor).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        if (drawZ)
        {
            bufferbuilder.vertex(0.0F, 0.0F, 0.0F).color(outerColor).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(0.0F, 0.0F, (float) lineLength).color(outerColor).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        tesselator.end();
        RenderSystem.lineWidth(2.0F);
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (drawX)
        {
            bufferbuilder.vertex(0, 0, 0).color(innerColor).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.vertex((float) lineLength, 0, 0).color(innerColor).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        if (drawZ)
        {
            bufferbuilder.vertex(0.0F, 0.0F, 0.0F).color(innerColor).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(0.0F, 0.0F, (float) lineLength).color(innerColor).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        tesselator.end();
        RenderSystem.lineWidth(1.0F);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
    }
}
