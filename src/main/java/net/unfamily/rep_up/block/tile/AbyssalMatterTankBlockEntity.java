package net.unfamily.rep_up.block.tile;

import com.buuz135.replication.ReplicationConfig;
import com.hrznstudio.titanium.block.BasicTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.unfamily.rep_up.Config;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Matter tank with configurable capacity multiplier (default 4x base). */
public class AbyssalMatterTankBlockEntity extends RepUpBaseMatterTankBlockEntity<AbyssalMatterTankBlockEntity> {

    public AbyssalMatterTankBlockEntity(BasicTileBlock<AbyssalMatterTankBlockEntity> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, BooleanSupplier isCreative) {
        super(base, blockEntityType, pos, state, isCreative, ReplicationConfig.MatterTank.CAPACITY * Config.ABYSSAL_MATTER_TANK_MULTIPLIER.get());
    }

    @NotNull
    @Override
    public AbyssalMatterTankBlockEntity getSelf() {
        return this;
    }
}
