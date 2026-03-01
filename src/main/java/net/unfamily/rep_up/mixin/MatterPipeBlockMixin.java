package net.unfamily.rep_up.mixin;

import com.buuz135.replication.block.MatterPipeBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows rep_up matter tanks (and other INetworkDirectionalConnection blocks) to connect
 * physically with matter pipes. Without this, only blocks in the "replication" namespace
 * are considered for pipe connection.
 */
@Mixin(MatterPipeBlock.class)
public class MatterPipeBlockMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
    private static void rep_up$addAllowedConnectionNamespace(CallbackInfo ci) {
        MatterPipeBlock.ALLOWED_CONNECTION_BLOCKS.add(
                (Block block) -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("rep_up")
        );
    }
}
