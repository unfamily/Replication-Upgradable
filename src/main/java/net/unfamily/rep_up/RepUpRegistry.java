package net.unfamily.rep_up;

import com.buuz135.replication.block.tile.ReplicationMachine;
import com.hrznstudio.titanium.block_network.INetworkDirectionalConnection;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.unfamily.rep_up.block.AbyssalMatterTankBlock;
import net.unfamily.rep_up.block.DeepMatterTankBlock;
import net.unfamily.rep_up.block.RepImpBlock;
import net.unfamily.rep_up.block.RepImpBlockEntity;
import net.unfamily.rep_up.block.tile.AbyssalMatterTankBlockEntity;
import net.unfamily.rep_up.block.tile.DeepMatterTankBlockEntity;

import java.util.function.Supplier;

public class RepUpRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, RepUp.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, RepUp.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RepUp.MODID);

    public static final DeferredHolder<Block, Block> REP_IMP = BLOCKS.register("rep_imp", RepImpBlock::new);
    public static final DeferredHolder<Item, Item> REP_IMP_ITEM = ITEMS.register("rep_imp", () -> new BlockItem(REP_IMP.get(), new Item.Properties()));
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RepImpBlockEntity>> BLOCK_ENTITY_REP_IMP =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<RepImpBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("rep_imp",
                    () -> BlockEntityType.Builder.of(((RepImpBlock) REP_IMP.get()).getTileEntityFactory(), REP_IMP.get()).build(null));

    private static Supplier<BlockEntityType<DeepMatterTankBlockEntity>> deepMatterTankType;
    private static Supplier<BlockEntityType<AbyssalMatterTankBlockEntity>> abyssalMatterTankType;

    public static final DeferredHolder<Block, Block> DEEP_MATTER_TANK = BLOCKS.register("deep_matter_tank",
            () -> new DeepMatterTankBlock("deep_matter_tank", () -> deepMatterTankType.get()));
    public static final DeferredHolder<Item, Item> DEEP_MATTER_TANK_ITEM = ITEMS.register("deep_matter_tank",
            () -> new BlockItem(DEEP_MATTER_TANK.get(), new Item.Properties()));
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepMatterTankBlockEntity>> BLOCK_ENTITY_DEEP_MATTER_TANK =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<DeepMatterTankBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("deep_matter_tank",
                    () -> BlockEntityType.Builder.of(((DeepMatterTankBlock) DEEP_MATTER_TANK.get()).getTileEntityFactory(), DEEP_MATTER_TANK.get()).build(null));

    public static final DeferredHolder<Block, Block> ABYSSAL_MATTER_TANK = BLOCKS.register("abyssal_matter_tank",
            () -> new AbyssalMatterTankBlock("abyssal_matter_tank", () -> abyssalMatterTankType.get()));
    public static final DeferredHolder<Item, Item> ABYSSAL_MATTER_TANK_ITEM = ITEMS.register("abyssal_matter_tank",
            () -> new BlockItem(ABYSSAL_MATTER_TANK.get(), new Item.Properties()));
    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssalMatterTankBlockEntity>> BLOCK_ENTITY_ABYSSAL_MATTER_TANK =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssalMatterTankBlockEntity>>) (Object) BLOCK_ENTITY_TYPES.register("abyssal_matter_tank",
                    () -> BlockEntityType.Builder.of(((AbyssalMatterTankBlock) ABYSSAL_MATTER_TANK.get()).getTileEntityFactory(), ABYSSAL_MATTER_TANK.get()).build(null));

    public static void register(IEventBus modEventBus) {
        deepMatterTankType = () -> BLOCK_ENTITY_DEEP_MATTER_TANK.get();
        abyssalMatterTankType = () -> BLOCK_ENTITY_ABYSSAL_MATTER_TANK.get();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(RepUpRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                BLOCK_ENTITY_REP_IMP.get(),
                (blockEntity, context) -> blockEntity.getEnergyStorage()
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                BLOCK_ENTITY_REP_IMP.get(),
                (blockEntity, context) -> blockEntity.getItemHandler(context)
        );
        event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
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
    }
}
