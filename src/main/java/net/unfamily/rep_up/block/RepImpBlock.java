package net.unfamily.rep_up.block;

import com.buuz135.replication.ReplicationRegistry;
import com.buuz135.replication.block.shapes.ReplicatorShapes;
import com.hrznstudio.titanium.block.RotatableBlock;
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection;
import com.hrznstudio.titanium.util.FacingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.unfamily.rep_up.RepUpRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Replicator variant (rep_imp) with configurable duration (default half of classic).
 * Same behaviour and model as Replication replicator, accepts motor and enclosure upgrades.
 */
public class RepImpBlock extends RotatableBlock<RepImpBlockEntity> implements INetworkDirectionalConnection {

    public static final BooleanProperty HAS_ENCLOSURE = BooleanProperty.create("has_enclosure");
    public static final BooleanProperty HAS_MOTOR = BooleanProperty.create("has_motor");

    public RepImpBlock() {
        super("rep_imp", Properties.ofFullCopy(Blocks.IRON_BLOCK), RepImpBlockEntity.class);
        registerDefaultState(defaultBlockState().setValue(HAS_ENCLOSURE, false).setValue(HAS_MOTOR, false));
    }

    @Override
    public BlockEntityType.BlockEntitySupplier<?> getTileEntityFactory() {
        return (pos, blockState) -> new RepImpBlockEntity(this, RepUpRegistry.BLOCK_ENTITY_REP_IMP.get(), pos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_ENCLOSURE, HAS_MOTOR);
    }

    @NotNull
    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext) {
        return switch (state.getValue(FACING_HORIZONTAL)) {
            case NORTH -> ReplicatorShapes.NORTH_FULL;
            case SOUTH -> ReplicatorShapes.SOUTH_FULL;
            case EAST -> ReplicatorShapes.EAST_FULL;
            case WEST -> ReplicatorShapes.WEST_FULL;
            default -> super.getCollisionShape(state, world, pos, selectionContext);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING_HORIZONTAL)) {
            case NORTH -> ReplicatorShapes.NORTH_FULL;
            case SOUTH -> ReplicatorShapes.SOUTH_FULL;
            case EAST -> ReplicatorShapes.EAST_FULL;
            case WEST -> ReplicatorShapes.WEST_FULL;
            default -> super.getShape(state, level, pos, context);
        };
    }

    public com.mojang.datafixers.util.Pair<VoxelShape, VoxelShape> getShapePlate(BlockState state) {
        return switch (state.getValue(FACING_HORIZONTAL)) {
            case NORTH -> com.mojang.datafixers.util.Pair.of(ReplicatorShapes.NORTH, ReplicatorShapes.NORTH_PLATE);
            case SOUTH -> com.mojang.datafixers.util.Pair.of(ReplicatorShapes.SOUTH, ReplicatorShapes.SOUTH_PLATE);
            case EAST -> com.mojang.datafixers.util.Pair.of(ReplicatorShapes.EAST, ReplicatorShapes.EAST_PLATE);
            case WEST -> com.mojang.datafixers.util.Pair.of(ReplicatorShapes.WEST, ReplicatorShapes.WEST_PLATE);
            default -> com.mojang.datafixers.util.Pair.of(ReplicatorShapes.NORTH, ReplicatorShapes.NORTH_PLATE);
        };
    }

    @Override
    public boolean canConnect(Level level, BlockPos pos, BlockState state, Direction direction) {
        var sideness = FacingUtil.getFacingRelative(direction, state.getValue(FACING_HORIZONTAL));
        if (direction == Direction.UP) return false;
        return sideness == FacingUtil.Sideness.BOTTOM || sideness == FacingUtil.Sideness.BACK;
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        NonNullList<ItemStack> list = super.getDynamicDrops(state, level, pos, newState, moving);
        if (state.getValue(HAS_MOTOR)) {
            list.add(new ItemStack(ReplicationRegistry.Items.REPLICATOR_MOTOR.get()));
        }
        if (state.getValue(HAS_ENCLOSURE)) {
            list.add(new ItemStack(ReplicationRegistry.Items.REPLICATOR_ENCLOSURE.get()));
        }
        return list;
    }
}
