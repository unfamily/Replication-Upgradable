package net.unfamily.rep_up.block;

import com.buuz135.replication.ReplicationAttachments;
import com.buuz135.replication.block.shapes.MatterTankShapes;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.rep_up.block.tile.AbyssalMatterTankBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class AbyssalMatterTankBlock extends RotatableBlock<AbyssalMatterTankBlockEntity> implements INetworkDirectionalConnection {

    private final Supplier<BlockEntityType<AbyssalMatterTankBlockEntity>> typeGetter;

    public AbyssalMatterTankBlock(String name, Supplier<BlockEntityType<AbyssalMatterTankBlockEntity>> typeGetter) {
        super(name, Properties.ofFullCopy(Blocks.IRON_BLOCK), AbyssalMatterTankBlockEntity.class);
        this.typeGetter = typeGetter;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (pos, blockState) -> new AbyssalMatterTankBlockEntity(this, typeGetter.get(), pos, blockState, () -> false);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return MatterTankShapes.SHAPE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return MatterTankShapes.SHAPE;
    }

    @Override
    public boolean canConnect(Level level, BlockPos pos, BlockState state, Direction direction) {
        return direction == Direction.UP || direction == Direction.DOWN;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity tankTile = builder.getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (tankTile instanceof AbyssalMatterTankBlockEntity tile) {
            if (!tile.getTanks().get(0).getMatter().isEmpty()) {
                stack.set(ReplicationAttachments.TILE, NBTManager.getInstance().writeTileEntity(tile, new CompoundTag()));
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (stack.has(ReplicationAttachments.TILE) && entity instanceof AbyssalMatterTankBlockEntity tile) {
            entity.loadCustomOnly(stack.get(ReplicationAttachments.TILE), entity.getLevel().registryAccess());
            tile.markForUpdate();
        }
    }
}
