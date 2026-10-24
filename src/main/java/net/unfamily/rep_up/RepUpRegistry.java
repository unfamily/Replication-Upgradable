package net.unfamily.rep_up;

import com.buuz135.replication.block.tile.ReplicationMachine;
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.unfamily.rep_up.block.AbyssalMatterTankBlock;
import net.unfamily.rep_up.block.DeepMatterTankBlock;
import net.unfamily.rep_up.block.EnergyMaterializerBlock;
import net.unfamily.rep_up.block.RepImpBlock;
import net.unfamily.rep_up.block.RepImpBlockEntity;
import net.unfamily.rep_up.block.tile.AbyssalMatterTankBlockEntity;
import net.unfamily.rep_up.block.tile.DeepMatterTankBlockEntity;
import net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity;

import java.util.function.Supplier;

public class RepUpRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RepUp.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RepUp.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RepUp.MODID);

    public static final DeferredBlock<RepImpBlock> REP_IMP = BLOCKS.registerBlock("rep_imp",
            RepImpBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> REP_IMP_ITEM = ITEMS.registerSimpleBlockItem(REP_IMP);
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepImpBlockEntity>> BLOCK_ENTITY_REP_IMP =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<RepImpBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("rep_imp",
                    () -> new BlockEntityType<>(REP_IMP.get().getTileEntityFactory(), REP_IMP.get()));

    private static Supplier<BlockEntityType<DeepMatterTankBlockEntity>> deepMatterTankType;
    private static Supplier<BlockEntityType<AbyssalMatterTankBlockEntity>> abyssalMatterTankType;
    private static Supplier<BlockEntityType<EnergyMaterializerBlockEntity>> energyMatType;

    public static final DeferredBlock<DeepMatterTankBlock> DEEP_MATTER_TANK = BLOCKS.registerBlock("deep_matter_tank",
            props -> new DeepMatterTankBlock(props, () -> deepMatterTankType.get()),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> DEEP_MATTER_TANK_ITEM = ITEMS.registerSimpleBlockItem(DEEP_MATTER_TANK);
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepMatterTankBlockEntity>> BLOCK_ENTITY_DEEP_MATTER_TANK =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepMatterTankBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("deep_matter_tank",
                    () -> new BlockEntityType<>(DEEP_MATTER_TANK.get().getTileEntityFactory(), DEEP_MATTER_TANK.get()));

    public static final DeferredBlock<AbyssalMatterTankBlock> ABYSSAL_MATTER_TANK = BLOCKS.registerBlock("abyssal_matter_tank",
            props -> new AbyssalMatterTankBlock(props, () -> abyssalMatterTankType.get()),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> ABYSSAL_MATTER_TANK_ITEM = ITEMS.registerSimpleBlockItem(ABYSSAL_MATTER_TANK);
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssalMatterTankBlockEntity>> BLOCK_ENTITY_ABYSSAL_MATTER_TANK =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssalMatterTankBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("abyssal_matter_tank",
                    () -> new BlockEntityType<>(ABYSSAL_MATTER_TANK.get().getTileEntityFactory(), ABYSSAL_MATTER_TANK.get()));

    public static final DeferredBlock<EnergyMaterializerBlock> ENERGY_MAT = BLOCKS.registerBlock("energy_mat",
            props -> new EnergyMaterializerBlock(props, () -> energyMatType.get()),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
    public static final DeferredItem<BlockItem> ENERGY_MAT_ITEM = ITEMS.registerSimpleBlockItem(ENERGY_MAT);
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyMaterializerBlockEntity>> BLOCK_ENTITY_ENERGY_MAT =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyMaterializerBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("energy_mat",
                    () -> new BlockEntityType<>(ENERGY_MAT.get().getTileEntityFactory(), ENERGY_MAT.get()));

    public static void register(IEventBus modEventBus) {
        deepMatterTankType = () -> BLOCK_ENTITY_DEEP_MATTER_TANK.get();
        abyssalMatterTankType = () -> BLOCK_ENTITY_ABYSSAL_MATTER_TANK.get();
        energyMatType = () -> BLOCK_ENTITY_ENERGY_MAT.get();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(RepUpRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                BLOCK_ENTITY_REP_IMP.get(),
                (blockEntity, context) -> blockEntity.getEnergyStorage()
        );
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                BLOCK_ENTITY_REP_IMP.get(),
                (blockEntity, context) -> blockEntity.getItemHandler(context)
        );
        event.registerBlock(
                Capabilities.Energy.BLOCK,
                (level, blockPos, blockState, blockEntity, direction) -> {
                    if (blockState.getBlock() instanceof INetworkDirectionalConnection connection
                            && connection.canConnect(level, blockPos, blockState, direction)
                            && blockEntity instanceof ReplicationMachine<?> machine) {
                        return machine.getEnergyStorage();
                    }
                    return null;
                },
                REP_IMP.get()
        );
        event.registerBlockEntity(
                Capabilities.Energy.BLOCK,
                BLOCK_ENTITY_ENERGY_MAT.get(),
                (blockEntity, context) -> blockEntity.getEnergyStorage()
        );
        event.registerBlock(
                Capabilities.Energy.BLOCK,
                (level, blockPos, blockState, blockEntity, direction) -> {
                    if (blockState.getBlock() instanceof INetworkDirectionalConnection connection
                            && connection.canConnect(level, blockPos, blockState, direction)
                            && blockEntity instanceof ReplicationMachine<?> machine) {
                        return machine.getEnergyStorage();
                    }
                    return null;
                },
                ENERGY_MAT.get()
        );
    }
}
