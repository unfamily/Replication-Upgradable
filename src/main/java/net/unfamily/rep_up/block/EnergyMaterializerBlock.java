package net.unfamily.rep_up.block;

import com.buuz135.replication.block.shapes.DisintegratorShapes;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection;
import com.hrznstudio.titanium.util.FacingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EnergyMaterializerBlock extends RotatableBlock<EnergyMaterializerBlockEntity> implements INetworkDirectionalConnection {

    private final Supplier<BlockEntityType<EnergyMaterializerBlockEntity>> typeGetter;

    public EnergyMaterializerBlock(String name, Supplier<BlockEntityType<EnergyMaterializerBlockEntity>> typeGetter) {
        super(name, Properties.ofFullCopy(Blocks.IRON_BLOCK), EnergyMaterializerBlockEntity.class);
        this.typeGetter = typeGetter;
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (pos, blockState) -> new EnergyMaterializerBlockEntity(this, typeGetter.get(), pos, blockState);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        Direction rotation = state.getValue(FACING_HORIZONTAL);
        return switch (rotation) {
            case NORTH -> DisintegratorShapes.NORTH;
            case SOUTH -> DisintegratorShapes.SOUTH;
            case EAST -> DisintegratorShapes.EAST;
            case WEST -> DisintegratorShapes.WEST;
            default -> super.getCollisionShape(state, world, pos, selectionContext);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction rotation = state.getValue(FACING_HORIZONTAL);
        return switch (rotation) {
            case NORTH -> DisintegratorShapes.NORTH;
            case SOUTH -> DisintegratorShapes.SOUTH;
            case EAST -> DisintegratorShapes.EAST;
            case WEST -> DisintegratorShapes.WEST;
            default -> super.getShape(state, level, pos, context);
        };
    }

    @Override
    public boolean canConnect(Level level, BlockPos pos, BlockState state, Direction direction) {
        var sideness = FacingUtil.getFacingRelative(direction, state.getValue(FACING_HORIZONTAL));
        return sideness == FacingUtil.Sideness.BOTTOM || sideness == FacingUtil.Sideness.BACK;
    }
}
