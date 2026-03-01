package net.unfamily.rep_up.block.tile;

import com.buuz135.replication.ReplicationConfig;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.unfamily.rep_up.Config;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Matter tank with configurable capacity multiplier (default 2x base). */
public class DeepMatterTankBlockEntity extends RepUpBaseMatterTankBlockEntity<DeepMatterTankBlockEntity> {

    public DeepMatterTankBlockEntity(BasicTileBlock<DeepMatterTankBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, BooleanSupplier isCreative) {
        super(base, blockEntityType, pos, state, isCreative, ReplicationConfig.MatterTank.CAPACITY * Config.DEEP_MATTER_TANK_MULTIPLIER.get());
    }

    @NotNull
    @Override
    public DeepMatterTankBlockEntity getSelf() {
        return this;
    }
}
