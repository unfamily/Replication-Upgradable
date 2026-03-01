package net.unfamily.rep_up.client;

import com.hrznstudio.titanium.block.RotatableBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;

/**
 * Renders the rotating blade/pivot in the center of the Energy Materializer.
 * Uses rep_up:block/energy_mat_blade model (our texture).
 */
public class EnergyMaterializerRenderer implements BlockEntityRenderer<EnergyMaterializerBlockEntity> {

    public static BakedModel BLADE = null;

    public EnergyMaterializerRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void setBladeModel(BakedModel model) {
        BLADE = model;
    }

    @Override
    public void render(EnergyMaterializerBlockEntity tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (BLADE == null) return;

        Direction facing = tile.getBlockState().getValue(RotatableBlock.FACING_HORIZONTAL);
        if (facing == Direction.EAST) {
            poseStack.translate(1, 0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        } else if (facing == Direction.SOUTH) {
            poseStack.translate(1, 0, 1);
            poseStack.mulPose(Axis.YP.rotationDegrees(-180));
        } else if (facing == Direction.WEST) {
            poseStack.translate(0, 0, 1);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        poseStack.translate(0.5f, 0.505f, 0.38f);

        float speed = 20F;
        poseStack.pushPose();
        poseStack.translate(0, -0.505f, 0);
        long time = tile.getLevel().getGameTime() % 36000;
        poseStack.mulPose(Axis.YP.rotationDegrees((time % (360f / speed)) * speed + partialTicks));
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()),
                null,
                BLADE,
                255, 255, 255,
                combinedLight,
                combinedOverlay
        );
        poseStack.popPose();
    }
}
