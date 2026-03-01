package net.unfamily.rep_up.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.rep_up.RepUp;
import net.unfamily.rep_up.RepUpRegistry;
import net.unfamily.rep_up.block.RepImpBlock;
import net.unfamily.rep_up.block.RepImpBlockEntity;
import net.unfamily.rep_up.block.tile.AbyssalMatterTankBlockEntity;
import net.unfamily.rep_up.block.tile.DeepMatterTankBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import org.joml.Matrix4f;

/**
 * Client-only: BER registration and plate model for rep_imp.
 * Uses our own plate model (rep_up:block/rep_imp_plate) with rep_up textures.
 */
@EventBusSubscriber(modid = RepUp.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RepUpClient {

    private static final ResourceLocation PLATE_MODEL_ID = ResourceLocation.fromNamespaceAndPath(RepUp.MODID, "block/rep_imp_plate");
    private static final ModelResourceLocation PLATE_MODEL_KEY = new ModelResourceLocation(PLATE_MODEL_ID, "standalone");

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_REP_IMP.get(), RepImpRenderer::new);
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_DEEP_MATTER_TANK.get(), RepUpMatterTankRenderer::new);
        event.registerBlockEntityRenderer(RepUpRegistry.BLOCK_ENTITY_ABYSSAL_MATTER_TANK.get(), RepUpMatterTankRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        event.register(PLATE_MODEL_KEY);
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        BakedModel baked = event.getModels().get(PLATE_MODEL_KEY);
        if (baked != null && baked != event.getModelManager().getMissingModel()) {
            RepImpRenderer.setPlateModel(baked);
        } else {
            RepUp.LOGGER.warn("rep_imp: could not find baked model {} (key {})", PLATE_MODEL_ID, PLATE_MODEL_KEY);
        }
    }

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        if (event.getTarget() == null) return;
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        var pos = event.getTarget().getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof RepImpBlock repImpBlock)) return;
        var be = level.getBlockEntity(pos);
        if (!(be instanceof RepImpBlockEntity repImpBE)) return;
        var body = repImpBlock.getShapePlate(state).getFirst();
        var plate = repImpBlock.getShapePlate(state).getSecond();
        event.setCanceled(true);
        PoseStack stack = new PoseStack();
        stack.pushPose();
        Camera camera = event.getCamera();
        double dx = camera.getPosition().x();
        double dy = camera.getPosition().y();
        double dz = camera.getPosition().z();
        VertexConsumer buffer = event.getMultiBufferSource().getBuffer(RenderType.LINES);
        drawShape(stack, buffer, body, pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz, 0, 0, 0, 0.4f);
        stack.translate(0, -RepImpBlockEntity.LOWER_PROGRESS, 0);
        float progress = repImpBE.getMaxProgress() <= 0 ? 1f : (repImpBE.getProgress() / (float) repImpBE.getMaxProgress());
        stack.translate(0, RepImpBlockEntity.LOWER_PROGRESS * progress, 0);
        drawShape(stack, buffer, plate, pos.getX() - dx, pos.getY() - dy, pos.getZ() - dz, 0, 0, 0, 0.4f);
        stack.popPose();
    }

    private static void drawShape(PoseStack poseStack, VertexConsumer buffer, VoxelShape shape, double x, double y, double z, float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float fx = (float) (x2 - x1);
            float fy = (float) (y2 - y1);
            float fz = (float) (z2 - z1);
            float len = Mth.sqrt(fx * fx + fy * fy + fz * fz);
            fx /= len;
            fy /= len;
            fz /= len;
            buffer.addVertex(matrix, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).setColor(r, g, b, a).setNormal(pose, fx, fy, fz);
            buffer.addVertex(matrix, (float) (x2 + x), (float) (y2 + y), (float) (z2 + z)).setColor(r, g, b, a).setNormal(pose, fx, fy, fz);
        });
    }
}
