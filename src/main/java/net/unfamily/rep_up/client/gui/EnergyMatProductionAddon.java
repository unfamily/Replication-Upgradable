package net.unfamily.rep_up.client.gui;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.client.screen.addon.WidgetScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.util.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;

/**
 * Production multiplier input (1..max from config). Same style as Replication's MatterTankPriorityAddon.
 */
public class EnergyMatProductionAddon extends WidgetScreenAddon {

    private static final int BUTTON_ID_PRODUCTION = 3001;

    private final EnergyMaterializerBlockEntity blockEntity;
    private final EditBox editBox;
    private String lastValue = "";

    public EnergyMatProductionAddon(EnergyMaterializerBlockEntity blockEntity, int posX, int posY) {
        super(posX, posY, new EditBox(Minecraft.getInstance().font, 0, 0, 36, 14,
                Component.translatable("gui.rep_up.energy_mat.production")));
        this.blockEntity = blockEntity;
        this.editBox = (EditBox) getWidget();
        this.editBox.setValue(String.valueOf(blockEntity.getProduction()));
        this.editBox.setFilter(s -> {
            if (s.isEmpty()) return true;
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.editBox.setMaxLength(4);
        this.editBox.setBordered(false);
        this.editBox.setVisible(true);
        this.editBox.setTextColor(0x72e567);
    }

    @Override
    public int getXSize() {
        return 80;
    }

    @Override
    public int getYSize() {
        return 14;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        this.editBox.setResponder(s -> {
            if (s.isEmpty()) return;
            if (!this.lastValue.equals(s)) {
                try {
                    int value = Integer.parseInt(s);
                    int max = net.unfamily.rep_up.Config.ENERGY_MAT_MAX_PRODUCTION.get();
                    value = Math.max(1, Math.min(max, value));
                    var compound = new CompoundTag();
                    compound.putInt("Production", value);
                    Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(
                            new TileEntityLocatorInstance(blockEntity.getBlockPos()), BUTTON_ID_PRODUCTION, compound));
                } catch (NumberFormatException ignored) {}
            }
            this.lastValue = s;
        });
        if (!this.editBox.isFocused()) {
            this.editBox.setValue(String.valueOf(blockEntity.getProduction()));
            this.lastValue = this.editBox.getValue();
        }
        String label = Component.translatable("gui.rep_up.energy_mat.production").getString();
        int textWidth = Minecraft.getInstance().font.width(label);
        guiGraphics.drawString(Minecraft.getInstance().font, label, guiX + getPosX(), guiY + getPosY(), 0x72e567, false);
        int boxX = guiX + getPosX() + textWidth + 4;
        int boxY = guiY + getPosY();
        this.editBox.setX(boxX);
        this.editBox.setY(boxY);
        this.editBox.render(guiGraphics, mouseX, mouseY, partialTicks);
        for (int i = 0; i < 18; i++) {
            AssetUtil.drawHorizontalLine(guiGraphics, boxX + i * 2, boxX + i * 2, boxY + 8, 0xff72e567);
        }
    }

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }
}
