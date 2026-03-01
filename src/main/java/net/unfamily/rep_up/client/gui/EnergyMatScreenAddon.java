package net.unfamily.rep_up.client.gui;

import com.buuz135.replication.Replication;
import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.MatterType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * GUI addon: matter type list sidebar (Replication Terminal style), no scrollbar, grid layout.
 * Tank is drawn by MatterTankComponent's addon.
 */
public class EnergyMatScreenAddon extends BasicScreenAddon {

    private static final int BUTTON_ID_SELECT_MATTER = 3000;
    /** Terminal: strip blit (0,0,27,174), first row at 26 (7px below strip at 19). We use strip at 8, first row 15. */
    private static final int ROWS_PER_COLUMN = 8;
    private static final int CELL_SIZE = 20;
    private static final int GAP_ABOVE_FIRST_ROW = 7;
    private static final int SIDEBAR_PANEL_Y = 8;
    private static final int FIRST_ROW_Y = SIDEBAR_PANEL_Y + GAP_ABOVE_FIRST_ROW;
    private static final int SIDEBAR_PANEL_X_OFFSET = 0;
    private static final int SIDEBAR_STRIP_WIDTH = 27;
    /** Same height as terminal (27, 174) so the strip texture is not cut off. */
    private static final int SIDEBAR_STRIP_HEIGHT = 174;
    /** Scrollbar thumb texture in replication_terminal_extras.png: selection indicator in selected matter cell. */
    private static final int SELECTOR_U = 245;
    private static final int SELECTOR_V = 0;
    private static final int SELECTOR_W = 11;
    private static final int SELECTOR_H = 5;
    private static final ResourceLocation BUTTONS = ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/replication_terminal_extras.png");

    private final EnergyMaterializerBlockEntity blockEntity;
    private final List<IMatterType> matterTypesOrdered = new ArrayList<>();

    public EnergyMatScreenAddon(int posX, int posY, EnergyMaterializerBlockEntity blockEntity) {
        super(posX, posY);
        this.blockEntity = blockEntity;
        if (ReplicationRegistry.MATTER_TYPES_REGISTRY != null) {
            StreamSupport.stream(ReplicationRegistry.MATTER_TYPES_REGISTRY.spliterator(), false)
                    .filter(t -> t != MatterType.EMPTY)
                    .forEach(matterTypesOrdered::add);
        }
    }

    /** Number of columns needed for all matter types (terminal style: 8 per column). */
    private int getColumnCount() {
        if (matterTypesOrdered.isEmpty()) return 1;
        return (matterTypesOrdered.size() + ROWS_PER_COLUMN - 1) / ROWS_PER_COLUMN;
    }

    @Override
    public int getXSize() {
        return SIDEBAR_STRIP_WIDTH + (getColumnCount() - 1) * CELL_SIZE;
    }

    @Override
    public int getYSize() {
        return SIDEBAR_PANEL_Y + SIDEBAR_STRIP_HEIGHT;
    }

    /** Index of currently selected matter in matterTypesOrdered, or -1 if none. */
    private int getSelectedIndex() {
        ResourceLocation currentId = blockEntity.getSelectedMatterId();
        if (currentId == null) return -1;
        for (int i = 0; i < matterTypesOrdered.size(); i++) {
            Object key = ReplicationRegistry.MATTER_TYPES_REGISTRY.getKey(matterTypesOrdered.get(i));
            ResourceLocation typeId = key != null ? (key instanceof ResourceLocation r ? r : ResourceLocation.parse(key.toString())) : null;
            if (currentId.equals(typeId)) return i;
        }
        return -1;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        int sidebarX = guiX + getPosX();
        int sidebarY = guiY + getPosY();
        // Terminal-style strips: one 27x174 per column
        for (int i = 0; i < getColumnCount(); i++) {
            guiGraphics.blit(BUTTONS, sidebarX + i * CELL_SIZE, sidebarY + SIDEBAR_PANEL_Y, 0, 0, SIDEBAR_STRIP_WIDTH, SIDEBAR_STRIP_HEIGHT);
        }

        int selectedIdx = getSelectedIndex();
        for (int idx = 0; idx < matterTypesOrdered.size(); idx++) {
            int col = idx / ROWS_PER_COLUMN;
            int row = idx % ROWS_PER_COLUMN;
            int cellX = sidebarX + col * CELL_SIZE + SIDEBAR_PANEL_X_OFFSET;
            int cellY = sidebarY + FIRST_ROW_Y + row * CELL_SIZE;
            // Icon at +2,+2 like TerminalMatterValueDisplay
            IMatterType type = matterTypesOrdered.get(idx);
            float[] color = type.getColor().get();
            guiGraphics.setColor(color[0], color[1], color[2], color[3]);
            RenderSystem.enableBlend();
            ResourceLocation iconPath = ResourceLocation.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/mattertypes/" + type.getName().toLowerCase() + ".png");
            guiGraphics.blit(iconPath, cellX + 2, cellY + 2, 0, 0, 16, 16, 16, 16);
            RenderSystem.disableBlend();
            guiGraphics.setColor(1, 1, 1, 1);

            if (idx == selectedIdx) {
                guiGraphics.blit(BUTTONS, cellX + CELL_SIZE - SELECTOR_W - 1, cellY + CELL_SIZE - SELECTOR_H - 1, SELECTOR_U, SELECTOR_V, SELECTOR_W, SELECTOR_H);
            }
        }
        if (blockEntity.getNoTanksAvailable() && blockEntity.getSelectedMatterId() != null) {
            guiGraphics.drawString(Minecraft.getInstance().font,
                    Component.translatable("gui.rep_up.energy_mat.no_tanks_available"),
                    guiX + NO_TANKS_MSG_X, guiY + NO_TANKS_MSG_Y, 0xff6666, false);
        }
    }

    /** Matter tank at (32, 28). "No tanks" and Production further right, x=55. */
    private static final int NO_TANKS_MSG_X = 55;
    private static final int NO_TANKS_MSG_Y = 28 + 16 + 2;

    @Override
    public void drawForegroundLayer(GuiGraphics guiGraphics, Screen screen, IAssetProvider iAssetProvider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }

    /** Convert screen coords to gui-relative (like other Titanium addons). */
    private static boolean toGuiRelative(double screenMouseX, double screenMouseY, double[] outGui) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen<?> acs) {
            outGui[0] = screenMouseX - acs.getGuiLeft();
            outGui[1] = screenMouseY - acs.getGuiTop();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double[] gui = new double[2];
        if (!toGuiRelative(mouseX, mouseY, gui)) return false;
        double gx = gui[0];
        double gy = gui[1];
        if (!isMouseOver(gx, gy)) return false;
        if (button == 1) {
            var compound = new net.minecraft.nbt.CompoundTag();
            compound.putString("MatterId", "");
            Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(new TileEntityLocatorInstance(blockEntity.getBlockPos()), BUTTON_ID_SELECT_MATTER, compound));
            return true;
        }
        if (button != 0) return false;
        int relX = (int) (gx - getPosX());
        int relY = (int) (gy - getPosY());
        if (relY < FIRST_ROW_Y) return false;
        int row = (relY - FIRST_ROW_Y) / CELL_SIZE;
        int col = relX / CELL_SIZE;
        if (row >= ROWS_PER_COLUMN) return false;
        int idx = col * ROWS_PER_COLUMN + row;
        if (idx >= matterTypesOrdered.size()) return false;
        IMatterType type = matterTypesOrdered.get(idx);
        Object keyObj = ReplicationRegistry.MATTER_TYPES_REGISTRY.getKey(type);
        if (keyObj == null) return false;
        ResourceLocation id = keyObj instanceof ResourceLocation rl ? rl : ResourceLocation.parse(keyObj.toString());
        var compound = new net.minecraft.nbt.CompoundTag();
        compound.putString("MatterId", id.toString());
        Titanium.NETWORK.sendToServer(new ButtonClickNetworkMessage(new TileEntityLocatorInstance(blockEntity.getBlockPos()), BUTTON_ID_SELECT_MATTER, compound));
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }
}
