package net.laserdiamond.ultimatemanhunt.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public final class GracePeriodArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

    public GracePeriodArmorModel(ModelPart pRoot) {
        super(pRoot, resourceLocation -> RenderType.beaconBeam(resourceLocation, true));
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition humanoidMesh = HumanoidModel.createMesh(new CubeDeformation(1.05F), 0.0F);
        return LayerDefinition.create(humanoidMesh, 64, 64);
    }

    public static <T extends LivingEntity, M extends HumanoidModel<T>> void render(M parentModel, GracePeriodArmorModel<T> model, ResourceLocation texture, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, int color)
    {
        poseStack.pushPose();

        if (entity instanceof AbstractClientPlayer abstractClientPlayer)
        {
            parentModel.copyPropertiesTo(model);

            HumanoidModel.ArmPose itemPose1 = PlayerRenderer.getArmPose(abstractClientPlayer, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose itemPose2 = PlayerRenderer.getArmPose(abstractClientPlayer, InteractionHand.OFF_HAND);

            if (itemPose1.isTwoHanded())
            {
                itemPose2 = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }
            if (entity.getMainArm() == HumanoidArm.RIGHT)
            {
                model.rightArmPose = itemPose1;
                model.leftArmPose = itemPose2;
            } else
            {
                model.rightArmPose = itemPose2;
                model.leftArmPose = itemPose1;
            }
        }

        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(texture));
//                .setColor(FastColor.ARGB32.color(32, color));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

}
