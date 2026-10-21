package net.unfamily.rep_up.client.gui;

import com.buuz135.replication.Replication;
import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.MatterType;
import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.network.locator.instance.TileEntityLocatorInstance;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class EnergyMatScreenAddon extends BasicScreenAddon {

    private static final int BUTTON_ID_SELECT_MATTER = 3000;
    private static final int ROWS_PER_COLUMN = 8;
    private static final int CELL_SIZE = 20;
    private static final int GAP_ABOVE_FIRST_ROW = 7;
    private static final int SIDEBAR_PANEL_Y = 8;
    private static final int FIRST_ROW_Y = SIDEBAR_PANEL_Y + GAP_ABOVE_FIRST_ROW;
    private static final int SIDEBAR_STRIP_WIDTH = 27;
    private static final int SIDEBAR_STRIP_HEIGHT = 174;
    private static final int SELECTOR_U = 245;
    private static final int SELECTOR_V = 0;
    private static final int SELECTOR_W = 11;
    private static final int SELECTOR_H = 5;
    private static final Identifier BUTTONS = Identifier.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/replication_terminal_extras.png");

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

    private int getSelectedIndex() {
        Identifier currentId = blockEntity.getSelectedMatterId();
        if (currentId == null) return -1;
        for (int i = 0; i < matterTypesOrdered.size(); i++) {
            Object key = ReplicationRegistry.MATTER_TYPES_REGISTRY.getKey(matterTypesOrdered.get(i));
            Identifier typeId = key != null ? (key instanceof Identifier r ? r : Identifier.parse(key.toString())) : null;
            if (currentId.equals(typeId)) return i;
        }
        return -1;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider assets, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        int sidebarX = guiX + getPosX();
        int sidebarY = guiY + getPosY();
        for (int i = 0; i < getColumnCount(); i++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS, sidebarX + i * CELL_SIZE, sidebarY + SIDEBAR_PANEL_Y, 0, 0, SIDEBAR_STRIP_WIDTH, SIDEBAR_STRIP_HEIGHT, 256, 256);
        }

        int selectedIdx = getSelectedIndex();
        for (int idx = 0; idx < matterTypesOrdered.size(); idx++) {
            int col = idx / ROWS_PER_COLUMN;
            int row = idx % ROWS_PER_COLUMN;
            int cellX = sidebarX + col * CELL_SIZE;
            int cellY = sidebarY + FIRST_ROW_Y + row * CELL_SIZE;
            IMatterType type = matterTypesOrdered.get(idx);
            float[] colorValues = type.getColor().get();
            int color = new Color(colorValues[0], colorValues[1], colorValues[2], colorValues[3]).getRGB();
            Identifier iconPath = Identifier.fromNamespaceAndPath(Replication.MOD_ID, "textures/gui/mattertypes/" + type.getName().toLowerCase() + ".png");
            graphics.blit(RenderPipelines.GUI_TEXTURED, iconPath, cellX + 2, cellY + 2, 0, 0, 16, 16, 16, 16, color);

            if (idx == selectedIdx) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTONS, cellX + CELL_SIZE - SELECTOR_W - 1, cellY + CELL_SIZE - SELECTOR_H - 1, SELECTOR_U, SELECTOR_V, SELECTOR_W, SELECTOR_H, 256, 256);
            }
        }
        if (blockEntity.getNoTanksAvailable() && blockEntity.getSelectedMatterId() != null) {
            graphics.text(screen.getFont(),
                    Component.translatable("gui.rep_up.energy_mat.no_tanks_available"),
                    guiX + NO_TANKS_MSG_X, guiY + NO_TANKS_MSG_Y, 0xFFFF6666, false);
        }
    }

    private static final int NO_TANKS_MSG_X = 55;
    private static final int NO_TANKS_MSG_Y = 28 + 16 + 2;

    @Override
    public void drawForegroundLayer(GuiGraphicsExtractor graphics, Screen screen, IAssetProvider assets, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
    }

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
        Identifier id = keyObj instanceof Identifier rl ? rl : Identifier.parse(keyObj.toString());
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
