package net.unfamily.rep_up.client;

import com.buuz135.replication.Replication;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.unfamily.rep_up.block.RepImpBlockEntity;

/**
 * GUI addon for rep_imp crafting slot/infinite mode display (same as ReplicatorCraftingAddon).
 */
public class RepImpCraftingAddon extends BasicScreenAddon {

    private final RepImpBlockEntity blockEntity;

    public RepImpCraftingAddon(int posX, int posY, RepImpBlockEntity blockEntity) {
        super(posX, posY);
        this.blockEntity = blockEntity;
    }

    @Override
    public int getXSize() {
        return 0;
    }

    @Override
    public int getYSize() {
        return 0;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/replication_terminal_extras.png"), guiX + 41, guiY + 26, 211, 125, 45, 36);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/replication_terminal_extras.png"), guiX + 100, guiY + 58, 250, 161, 6, 3);
        if (!blockEntity.getCraftingStack().isEmpty()) {
            guiGraphics.renderItem(blockEntity.getCraftingStack(), guiX + 67, guiY + 29);
        }
        float scale = 0.6f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("replication.infinite_mode").append(blockEntity.isInfinite() ? Component.translatable("replication.true") : Component.translatable("replication.false")).getString(), (int) ((guiX + 41) / scale), (int) ((guiY + 20) / scale), 0x72e567, false);
        guiGraphics.pose().popPose();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/replication_terminal_extras.png"), guiX + 46, guiY + 32, 232, 164, 12, 9);
        guiGraphics.pose().popPose();

        if (!blockEntity.getCraftingStack().isEmpty() && mouseX > (guiX + 67) && mouseX < (guiX + 67 + 16) && mouseY > (guiY + 29) && mouseY < (guiY + 29 + 16)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, blockEntity.getCraftingStack(), (int) mouseX, (int) mouseY);
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }
}
