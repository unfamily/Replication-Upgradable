package net.unfamily.rep_up.block.tile;

import com.buuz135.replication.ReplicationConfig;
import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.api.IMatterType;
import com.buuz135.replication.api.MatterType;
import com.buuz135.replication.api.matter_fluid.IMatterTank;
import com.buuz135.replication.api.matter_fluid.MatterStack;
import com.buuz135.replication.api.matter_fluid.component.MatterTankComponent;
import com.buuz135.replication.api.network.IMatterTanksConsumer;
import com.buuz135.replication.api.network.IMatterTanksSupplier;
import com.buuz135.replication.block.tile.ReplicationMachine;
import com.buuz135.replication.network.MatterNetwork;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block_network.NetworkManager;
import com.hrznstudio.titanium.block_network.element.NetworkElement;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.unfamily.rep_up.Config;
import net.unfamily.rep_up.data.EnergyMatRecipeLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnergyMaterializerBlockEntity extends ReplicationMachine<EnergyMaterializerBlockEntity> implements IMatterTanksSupplier {

    private static final int MATTER_PER_OPERATION = 1;

    @Save
    private MatterTankComponent<EnergyMaterializerBlockEntity> tank;
    /** Saved as string for NBT; null or empty = no selection. */
    @Save
    private String selectedMatterIdStr = "";
    /** Production multiplier: 1..maxProduction from config. Multiplies RF cost and matter output per tick. */
    @Save
    private int production = 1;
    /** Synced to client: true when a matter is selected but network has no tank that can receive it. Not persisted (reset on load). */
    @Save
    private boolean noTanksAvailable;

    public EnergyMaterializerBlockEntity(BasicTileBlock<EnergyMaterializerBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.tank = new MatterTankComponent<EnergyMaterializerBlockEntity>("tank", ReplicationConfig.MatterTank.CAPACITY, 32, 28)
                .setTankAction(FluidTankComponent.Action.BOTH);
        this.addMatterTank(this.tank);
    }

    @Override
    protected EnergyStorageComponent<EnergyMaterializerBlockEntity> createEnergyStorage() {
        return new EnergyStorageComponent<>(Config.ENERGY_MAT_ENERGY_CAPACITY.get(), 9, 28);
    }

    public MatterTankComponent<EnergyMaterializerBlockEntity> getTank() {
        return tank;
    }

    /** Current selected matter type ID; null or empty means no production. */
    public ResourceLocation getSelectedMatterId() {
        if (selectedMatterIdStr == null || selectedMatterIdStr.isBlank()) return null;
        try {
            return ResourceLocation.parse(selectedMatterIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    public void setSelectedMatterId(ResourceLocation id) {
        this.selectedMatterIdStr = id == null ? "" : id.toString();
    }

    public int getProduction() {
        int max = Config.ENERGY_MAT_MAX_PRODUCTION.get();
        if (production < 1) return 1;
        if (production > max) return max;
        return production;
    }

    public void setProduction(int value) {
        int max = Config.ENERGY_MAT_MAX_PRODUCTION.get();
        this.production = Math.max(1, Math.min(max, value));
    }

    public boolean getNoTanksAvailable() {
        return noTanksAvailable;
    }

    /** Returns the matter network this block is part of, or null if not connected. */
    private MatterNetwork getNetworkSafe() {
        if (level == null) return null;
        var element = NetworkManager.get(level).getElement(worldPosition);
        if (element == null || element.getNetwork() == null) return null;
        return element.getNetwork() instanceof MatterNetwork mn ? mn : null;
    }

    /** True if the network has at least one tank that can receive this matter (empty or same type with space >= amount). */
    private boolean networkHasDestinationFor(IMatterType matterType, double amount) {
        MatterNetwork network = getNetworkSafe();
        if (network == null) return false;
        MatterStack probe = new MatterStack(matterType, amount);
        for (NetworkElement el : network.getMatterStacksConsumers()) {
            if (!el.getLevel().isLoaded(el.getPos())) continue;
            var be = el.getLevel().getBlockEntity(el.getPos());
            if (!(be instanceof IMatterTanksConsumer consumer)) continue;
            for (IMatterTank t : consumer.getTanks()) {
                if (!t.isMatterValid(probe)) continue;
                double free = t.getCapacity() - t.getMatterAmount();
                if (free < amount) continue;
                if (t.getMatter().isEmpty() || t.getMatter().getMatterType().equals(matterType))
                    return true;
            }
        }
        for (NetworkElement el : network.getMatterStacksHolders()) {
            if (!el.getLevel().isLoaded(el.getPos())) continue;
            var be = el.getLevel().getBlockEntity(el.getPos());
            if (!(be instanceof IMatterTanksConsumer consumer)) continue;
            for (IMatterTank t : consumer.getTanks()) {
                if (!t.isMatterValid(probe)) continue;
                double free = t.getCapacity() - t.getMatterAmount();
                if (free < amount) continue;
                if (t.getMatter().isEmpty() || t.getMatter().getMatterType().equals(matterType))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, EnergyMaterializerBlockEntity blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        int prod = getProduction();
        ResourceLocation selected = getSelectedMatterId();
        if (selected == null) {
            if (noTanksAvailable) {
                noTanksAvailable = false;
                syncObject(noTanksAvailable);
            }
            return;
        }
        IMatterType matterType = ReplicationRegistry.MATTER_TYPES_REGISTRY.get(selected);
        if (matterType == null || matterType == MatterType.EMPTY) return;
        int rfCost = EnergyMatRecipeLoader.getRfForMatter(selected);
        if (rfCost <= 0) return;
        int totalRf = rfCost * prod;
        double matterAmount = MATTER_PER_OPERATION * prod;
        boolean hasDestination = networkHasDestinationFor(matterType, matterAmount);
        if (!hasDestination) {
            if (!noTanksAvailable) {
                noTanksAvailable = true;
                syncObject(noTanksAvailable);
            }
            return;
        }
        if (noTanksAvailable) {
            noTanksAvailable = false;
            syncObject(noTanksAvailable);
        }
        if (getEnergyStorage().getEnergyStored() < totalRf) return;
        MatterStack toAdd = new MatterStack(matterType, matterAmount);
        double filled = tank.fillForced(toAdd, IFluidHandler.FluidAction.SIMULATE);
        if (filled < matterAmount) return;
        getEnergyStorage().extractEnergy(totalRf, false);
        tank.fillForced(toAdd, IFluidHandler.FluidAction.EXECUTE);
        markComponentDirty();
        if (level instanceof ServerLevel serverLevel) {
            RandomSource rng = level.getRandom();
            double px = pos.getX() + 0.25 + rng.nextDouble() * 0.5;
            double py = pos.getY() + 0.3 + rng.nextDouble() * 0.4;
            double pz = pos.getZ() + 0.25 + rng.nextDouble() * 0.5;
            float r = 0.4f + rng.nextFloat() * 0.6f;
            float g = 0.4f + rng.nextFloat() * 0.6f;
            float b = 0.4f + rng.nextFloat() * 0.6f;
            serverLevel.sendParticles(new DustParticleOptions(new org.joml.Vector3f(r, g, b), 1.0f), px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void handleButtonMessage(int id, Player player, CompoundTag compound) {
        super.handleButtonMessage(id, player, compound);
        if (id == 3000 && compound.contains("MatterId")) {
            String idStr = compound.getString("MatterId");
            setSelectedMatterId(idStr == null || idStr.isBlank() ? null : ResourceLocation.parse(idStr));
            markComponentDirty();
            markForUpdate();
        }
        if (id == 3001 && compound.contains("Production")) {
            setProduction(compound.getInt("Production"));
            syncObject(production);
            markComponentDirty();
            markForUpdate();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initClient() {
        super.initClient();
        addGuiAddonFactory(() -> new net.unfamily.rep_up.client.gui.EnergyMatScreenAddon(176, 0, this));
        addGuiAddonFactory(() -> new net.unfamily.rep_up.client.gui.EnergyMatProductionAddon(this, 55, 56));
    }

    @NotNull
    @Override
    public EnergyMaterializerBlockEntity getSelf() {
        return this;
    }

    @Override
    public List<? extends IMatterTank> getTanks() {
        return List.of(tank);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getTitleColor() {
        return 0x72e567;
    }

    @Override
    public float getTitleXPos(float titleWidth, float screenWidth, float screenHeight, float guiWidth, float guiHeight) {
        return super.getTitleXPos(titleWidth, screenWidth, screenHeight, guiWidth, guiHeight) + 8;
    }

    @Override
    public float getTitleYPos(float titleWidth, float screenWidth, float screenHeight, float guiWidth, float guiHeight) {
        return super.getTitleYPos(titleWidth, screenWidth, screenHeight, guiWidth, guiHeight) - 16;
    }
}
