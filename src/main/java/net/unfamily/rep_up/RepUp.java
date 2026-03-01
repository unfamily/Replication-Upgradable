package net.unfamily.rep_up;

import com.buuz135.replication.ReplicationRegistry;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.unfamily.rep_up.block.RepImpBlock;
import net.unfamily.rep_up.block.RepImpBlockEntity;
import net.unfamily.rep_up.data.EnergyMatRecipeLoader;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(RepUp.MODID)
public class RepUp {

    public static final String MODID = "rep_up";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RepUp(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, Config.SPEC);
        RepUpRegistry.register(modEventBus);
        NBTManager.getInstance().scanTileClassForAnnotations(RepImpBlockEntity.class);
        NBTManager.getInstance().scanTileClassForAnnotations(net.unfamily.rep_up.block.tile.DeepMatterTankBlockEntity.class);
        NBTManager.getInstance().scanTileClassForAnnotations(net.unfamily.rep_up.block.tile.AbyssalMatterTankBlockEntity.class);
        NBTManager.getInstance().scanTileClassForAnnotations(net.unfamily.rep_up.block.tile.EnergyMaterializerBlockEntity.class);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onBuildCreativeTab);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(RepUp::onRightClickBlock);
    }

    /** Allow applying Replication's enclosure and motor items to rep_imp. */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        var hit = event.getHitVec();
        var pos = hit.getBlockPos();
        var state = event.getLevel().getBlockState(pos);
        if (!(state.getBlock() instanceof RepImpBlock)) return;
        var stack = event.getEntity().getItemInHand(event.getHand());
        if (stack.isEmpty()) return;
        var item = stack.getItem();
        if (item == ReplicationRegistry.Items.REPLICATOR_ENCLOSURE.get() && !state.getValue(RepImpBlock.HAS_ENCLOSURE)) {
            event.getLevel().setBlockAndUpdate(pos, state.setValue(RepImpBlock.HAS_ENCLOSURE, true));
            stack.shrink(1);
            event.getEntity().swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        } else if (item == ReplicationRegistry.Items.REPLICATOR_MOTOR.get() && !state.getValue(RepImpBlock.HAS_MOTOR)) {
            event.getLevel().setBlockAndUpdate(pos, state.setValue(RepImpBlock.HAS_MOTOR, true));
            stack.shrink(1);
            event.getEntity().swing(event.getHand());
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.debug("RepUp common setup");
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("replication", "main"))) {
            event.accept(new ItemStack(RepUpRegistry.REP_IMP.get()));
            event.accept(new ItemStack(RepUpRegistry.DEEP_MATTER_TANK.get()));
            event.accept(new ItemStack(RepUpRegistry.ABYSSAL_MATTER_TANK.get()));
            event.accept(new ItemStack(RepUpRegistry.ENERGY_MAT_ITEM.get()));
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.debug("RepUp server starting");
        EnergyMatRecipeLoader.load();
    }
}
