package net.unfamily.rep_up.client.gui;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.client.screen.addon.WidgetScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.unfamily.rep_up.Config;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;

public class EnergyMatProductionAddon extends WidgetScreenAddon {

    private static final int BUTTON_ID_PRODUCTION = 3001;

    private final EnergyMaterializerBlockEntity blockEntity;
    private final EditBox editBox;
    private String lastValue = "";

    public EnergyMatProductionAddon(EnergyMaterializerBlockEntity blockEntity, int posX, int posY) {
        super(posX, posY, new EditBox(Minecraft.getInstance().font, 36, 12,
                Component.translatable("gui.rep_up.energy_mat.production")));
        this.blockEntity = blockEntity;
        this.editBox = (EditBox) getWidget();
        this.lastValue = String.valueOf(blockEntity.getProduction());
        this.editBox.setValue(this.lastValue);
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
        this.editBox.setTextColor(0xFF72E567);
        this.editBox.setResponder(this::sendProduction);
    }

    private void sendProduction(String s) {
        if (s.isEmpty() || s.equals(this.lastValue)) return;
        try {
            int value = Integer.parseInt(s);
            int max = Config.ENERGY_MAT_MAX_PRODUCTION.get();
            value = Math.max(1, Math.min(max, value));
            CompoundTag compound = new CompoundTag();
            compound.putInt("Production", value);
            Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(
                    new TileEntityLocatorInstance(blockEntity.getBlockPos()), BUTTON_ID_PRODUCTION, compound));
            this.lastValue = s;
        } catch (NumberFormatException ignored) {
        }
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
    public void drawBackgroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider assets, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (!this.editBox.isFocused()) {
            String current = String.valueOf(blockEntity.getProduction());
            if (!current.equals(this.editBox.getValue())) {
                this.editBox.setValue(current);
                this.lastValue = current;
            }
        }
        Component label = Component.translatable("gui.rep_up.energy_mat.production");
        int textWidth = screen.getFont().width(label);
        int x = guiX + getPosX();
        int y = guiY + getPosY();
        graphics.text(screen.getFont(), label, x, y, 0xFF72E567, false);
        int boxX = x + textWidth + 4;
        this.editBox.setX(boxX);
        this.editBox.setY(y - 2);
        this.editBox.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        graphics.horizontalLine(boxX, boxX + 35, y + 9, 0xFF72E567);
    }

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider assets, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }
}
