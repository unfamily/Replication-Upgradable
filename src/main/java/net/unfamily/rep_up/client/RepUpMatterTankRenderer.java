package net.unfamily.rep_up.client;

import com.buuz135.replication.Replication;
import com.buuz135.replication.api.IMatterType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import net.unfamily.rep_up.block.tile.RepUpBaseMatterTankBlockEntity;
import org.joml.Matrix4f;

public class RepUpMatterTankRenderer<T extends RepUpBaseMatterTankBlockEntity<T>> implements BlockEntityRenderer<T> {

    public RepUpMatterTankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        float padding = 0.2f;
        var matterStack = tile.getTanks().get(0).getMatter();
        if (matterStack.isEmpty()) return;
        var tank = tile.getTanks().get(0);
        double fill = (Math.min(matterStack.getAmount(), tank.getCapacity()) / (double) tank.getCapacity()) * 0.5;
        AABB bounds = new AABB(padding, 0.255, padding, 1 - padding, 0.255 + fill, 1 - padding);
        renderFaces(poseStack, bufferSource, bounds, LightTexture.FULL_BRIGHT, combinedOverlay, matterStack.getMatterType());
    }

    private void renderFaces(PoseStack matrixStack, MultiBufferSource bufferIn, AABB bounds, int combinedLight, int combinedOverlay, IMatterType matterType) {
        matrixStack.pushPose();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "block/matter");
        TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());

        float[] color = matterType.getColor().get();
        float red = color[0];
        float green = color[1];
        float blue = color[2];
        float alpha = color[3];

        float x1 = (float) bounds.minX;
        float x2 = (float) bounds.maxX;
        float y1 = (float) bounds.minY;
        float y2 = (float) bounds.maxY;
        float z1 = (float) bounds.minZ;
        float z2 = (float) bounds.maxZ;
        float bx1 = (float) bounds.minX;
        float bx2 = (float) bounds.maxX;
        float by1 = (float) bounds.minY;
        float by2 = (float) bounds.maxY;
        float bz1 = (float) bounds.minZ;
        float bz2 = (float) bounds.maxZ;

        Matrix4f posMat = matrixStack.last().pose();

        float u1 = still.getU(bx1);
        float u2 = still.getU(bx2);
        float v1 = still.getV(bz1);
        float v2 = still.getV(bz2);
        builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
        builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
        builder.addVertex(posMat, x2, y2, z1).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);
        builder.addVertex(posMat, x1, y2, z1).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 1f, 0f);

        v1 = still.getV(by1);
        v2 = still.getV(by2);
        builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);

        matrixStack.translate(0, 0, 1);
        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);

        matrixStack.translate(0, 0, 1);
        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);

        matrixStack.translate(0, 0, 1);
        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
        builder.addVertex(posMat, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);
        builder.addVertex(posMat, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0f, 0f, 1f);

        matrixStack.popPose();
    }
}
