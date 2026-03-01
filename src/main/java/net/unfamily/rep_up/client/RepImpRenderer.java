package net.unfamily.rep_up.client;

import com.buuz135.replication.calculation.MatterValue;
import com.buuz135.replication.calculation.client.ClientReplicationCalculation;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import net.unfamily.rep_up.block.RepImpBlockEntity;

/**
 * Renders the rep_imp replicator: same logic as Replication's ReplicatorRenderer
 * (facing transform, matter plane, plate model, item).
 */
public class RepImpRenderer implements BlockEntityRenderer<RepImpBlockEntity> {

    private static RenderType AREA_TYPE = createRenderType();

    public static RenderType createRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                })).createCompositeState(true);
        return RenderType.create("rep_up_working_area", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    private static BakedModel PLATE = null;

    public static void setPlateModel(BakedModel model) {
        PLATE = model;
    }

    public RepImpRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RepImpBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLightIn, int combinedOverlayIn) {
        var facing = entity.getBlockState().getValue(RotatableBlock.FACING_HORIZONTAL);
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
        poseStack.pushPose();

        var color = new float[]{1f, 1f, 1f, 0f};
        if (!entity.getCraftingStack().isEmpty() && entity.getAction() == 0) {
            var matterCompound = ClientReplicationCalculation.getMatterCompound(entity.getCraftingStack());
            if (matterCompound != null) {
                var total = 0D;
                for (MatterValue matterValue : matterCompound.getValues().values()) {
                    total += matterValue.getAmount();
                }
                var currentProgress = entity.getProgress() / (float) entity.getMaxProgress() * 1.4f;
                var progressTotal = 0D;
                for (MatterValue matterValue : matterCompound.getValues().values()) {
                    if ((progressTotal + matterValue.getAmount()) / total >= currentProgress) {
                        color = matterValue.getMatter().getColor().get();
                        break;
                    }
                    progressTotal += matterValue.getAmount();
                }
            }
        }
        renderPlane(poseStack, multiBufferSource, Block.box(2, 0, 2, 14, 1, 12).bounds(), 0, 0.15, 0, color[0], color[1], color[2], color[3] == 0 ? 0 : 0.75f);
        renderFaces(poseStack, multiBufferSource, Block.box(4, 0, 2, 12, 4, 12).bounds(), 0, -0.2, 0, 1, 1, 1, 0.005f);

        poseStack.translate(0, -RepImpBlockEntity.LOWER_PROGRESS, 0);

        var progress = (entity.getProgress() + partialTicks / 100f) / (float) entity.getMaxProgress();
        poseStack.translate(0, RepImpBlockEntity.LOWER_PROGRESS * progress - 0.001f, 0);

        if (PLATE != null) {
            BlockState state = entity.getBlockState();
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(),
                    multiBufferSource.getBuffer(RenderType.solid()), state, PLATE, 1f, 1f, 1f, combinedLightIn, combinedOverlayIn);
        }

        poseStack.translate(0.5f, 0.56f, 0.45f);
        var scale = 0.4f;
        var model = Minecraft.getInstance().getItemRenderer().getModel(entity.getCraftingStack(), Minecraft.getInstance().level, null, 0);
        if (model.isGui3d()) {
            scale = 0.75f;
        }
        poseStack.scale(scale, scale, scale);

        if (entity.getAction() == 0 && !entity.isCurrentTaskAFailure()) {
            Minecraft.getInstance().getItemRenderer().renderStatic(entity.getCraftingStack(), ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, poseStack, multiBufferSource, entity.getLevel(), 0);
        }
        poseStack.popPose();
    }

    private void renderPlane(PoseStack stack, MultiBufferSource renderTypeBuffer, AABB pos, double x, double y, double z, float red, float green, float blue, float alpha) {
        float x1 = (float) (pos.minX + x);
        float x2 = (float) (pos.maxX + x);
        float y2 = (float) (pos.maxY + y);
        float z1 = (float) (pos.minZ + z);
        float z2 = (float) (pos.maxZ + z);
        Matrix4f matrix = stack.last().pose();
        VertexConsumer buffer = renderTypeBuffer.getBuffer(AREA_TYPE);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
    }

    private void renderFaces(PoseStack stack, MultiBufferSource renderTypeBuffer, AABB pos, double x, double y, double z, float red, float green, float blue, float alpha) {
        float x1 = (float) (pos.minX + x);
        float x2 = (float) (pos.maxX + x);
        float y1 = (float) (pos.minY + y);
        float y2 = (float) (pos.maxY + y);
        float z1 = (float) (pos.minZ + z);
        float z2 = (float) (pos.maxZ + z);
        Matrix4f matrix = stack.last().pose();
        VertexConsumer buffer = renderTypeBuffer.getBuffer(AREA_TYPE);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(matrix, x2, y1, z2).setColor(red, green, blue, alpha);
    }
}
