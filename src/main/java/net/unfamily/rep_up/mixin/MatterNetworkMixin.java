package net.unfamily.rep_up.mixin;

import com.buuz135.replication.api.task.IReplicationTask;
import com.buuz135.replication.network.MatterNetwork;
import com.hrznstudio.titanium.block_network.element.NetworkElement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.unfamily.rep_up.block.RepImpBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Adds RepImpBlockEntity as a replicator in the matter network and handles task cancel.
 */
@Mixin(MatterNetwork.class)
public class MatterNetworkMixin {

    private static final ThreadLocal<IReplicationTask> repUp$capturedTask = new ThreadLocal<>();

    @Shadow
    private List<NetworkElement> replicators;

    @Shadow
    private List<NetworkElement> queueNetworkElements;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", ordinal = 0), require = 1)
    private void repUp$addRepImpReplicators(Level level, CallbackInfo ci) {
        for (NetworkElement element : this.queueNetworkElements) {
            if (!element.getLevel().isLoaded(element.getPos())) continue;
            var tile = element.getLevel().getBlockEntity(element.getPos());
            if (tile instanceof RepImpBlockEntity) {
                this.replicators.add(element);
            }
        }
    }

    @Inject(method = "cancelTask", at = @At("HEAD"), require = 1)
    private void repUp$captureTask(String task, Level level, CallbackInfo ci) {
        repUp$capturedTask.set(((MatterNetwork) (Object) this).getTaskManager().getPendingTasks().get(task));
    }

    @Inject(method = "cancelTask", at = @At("RETURN"), require = 1)
    private void repUp$cancelRepImpTask(String task, Level level, CallbackInfo ci) {
        var replicationTask = repUp$capturedTask.get();
        try {
            if (replicationTask != null) {
                for (Long l : replicationTask.getReplicatorsOnTask()) {
                    var pos = BlockPos.of(l);
                    if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof RepImpBlockEntity repImp) {
                        repImp.cancelTask();
                    }
                }
            }
        } finally {
            repUp$capturedTask.remove();
        }
    }
}
