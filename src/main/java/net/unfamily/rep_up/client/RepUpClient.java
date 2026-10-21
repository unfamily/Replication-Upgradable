package net.unfamily.rep_up.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.rep_up.RepUp;
import net.unfamily.rep_up.RepUpRegistry;
import net.unfamily.rep_up.block.RepImpBlock;
import net.unfamily.rep_up.block.RepImpBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

@EventBusSubscriber(modid = RepUp.MODID, value = Dist.CLIENT)
public class RepUpClient {

    private static final Identifier PLATE_MODEL_ID = Identifier.fromNamespaceAndPath(RepUp.MODID, "block/rep_imp_plate");
    private static final Identifier BLADE_MODEL_ID = Identifier.fromNamespaceAndPath(RepUp.MODID, "block/energy_mat_blade");

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_REP_IMP.get(), RepImpRenderer::new);
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_DEEP_MATTER_TANK.get(), RepUpMatterTankRenderer::new);
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_ABYSSAL_MATTER_TANK.get(), RepUpMatterTankRenderer::new);
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_ENERGY_MAT.get(), EnergyMaterializerRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterStandalone(ModelEvent.RegisterStandalone event) {
        event.register(RepImpRenderer.PLATE_KEY, SimpleUnbakedStandaloneModel.simpleModelWrapper(PLATE_MODEL_ID));
        event.register(EnergyMaterializerRenderer.BLADE_KEY, SimpleUnbakedStandaloneModel.simpleModelWrapper(BLADE_MODEL_ID));
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        RepImpRenderer.setPlate(event.getModelManager().getStandaloneModel(RepImpRenderer.PLATE_KEY));
        EnergyMaterializerRenderer.setBlade(event.getModelManager().getStandaloneModel(EnergyMaterializerRenderer.BLADE_KEY));
    }

    @SubscribeEvent
    public static void onBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        BlockState state = event.getBlockState();
        if (!(state.getBlock() instanceof RepImpBlock repImpBlock)) return;
        if (!(event.getLevel().getBlockEntity(event.getBlockPos()) instanceof RepImpBlockEntity repImpBE)) return;

        var shapes = repImpBlock.getShapePlate(state);
        VoxelShape body = shapes.getFirst();
        VoxelShape plate = shapes.getSecond();
        BlockPos pos = event.getBlockPos().immutable();
        Camera camera = event.getCamera();
        double dx = camera.position().x();
        double dy = camera.position().y();
        double dz = camera.position().z();
        float progress = repImpBE.getMaxProgress() <= 0 ? 1f
                : (repImpBE.getProgress() / (float) repImpBE.getMaxProgress());
        float plateOffset = -RepImpBlockEntity.LOWER_PROGRESS + RepImpBlockEntity.LOWER_PROGRESS * progress;

        BlockOutlineRenderState outlineState = event.getLevelRenderState().blockOutlineRenderState;
        int outlineColor = outlineState != null && outlineState.highContrast() ? -11010079 : ARGB.black(102);
        float lineWidth = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;

        event.addCustomRenderer((renderState, buffer, poseStack, translucentPass, levelRenderState) -> {
            VertexConsumer builder = buffer.getBuffer(RenderTypes.lines());
            ShapeRenderer.renderShape(poseStack, builder, body,
                    pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz, outlineColor, lineWidth);
            poseStack.pushPose();
            poseStack.translate(0, plateOffset, 0);
            ShapeRenderer.renderShape(poseStack, builder, plate,
                    pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz, outlineColor, lineWidth);
            poseStack.popPose();
            return true;
        });
    }
}
