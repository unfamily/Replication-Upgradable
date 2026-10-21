package net.unfamily.rep_up.client;

import com.hrznstudio.titanium.block.RotatableBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class EnergyMaterializerRenderer implements BlockEntityRenderer<EnergyMaterializerBlockEntity, EnergyMaterializerRenderer.State> {

    public static final StandaloneModelKey<BlockStateModelPart> BLADE_KEY = new StandaloneModelKey<>(() -> "rep_up:block/energy_mat_blade");
    private static BlockStateModelPart blade;

    public EnergyMaterializerRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void setBlade(BlockStateModelPart model) {
        blade = model;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EnergyMaterializerBlockEntity blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(RotatableBlock.FACING_HORIZONTAL);
        long gameTime = blockEntity.getLevel() == null ? 0 : blockEntity.getLevel().getGameTime();
        state.bladeRotation = (gameTime % (360f / 20F)) * 20F + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (blade == null) {
            return;
        }
        poseStack.pushPose();
        RepUpRenderHelper.rotate(poseStack, state.facing);
        poseStack.translate(0.5f, 0.505f, 0.38f);
        poseStack.translate(0, -0.505f, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.bladeRotation));
        submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(), List.of(blade), BlockModelRenderState.EMPTY_TINTS, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        private Direction facing = Direction.NORTH;
        private float bladeRotation;
    }
}
