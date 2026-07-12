package mekanism.fabric_shim.client.extensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/**
 * Same surface as net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions (the
 * particle-override slice Mekanism uses). Semantics mirror NeoForge: returning {@code true} from a
 * hook means "handled — suppress vanilla particles". The ParticleEngine call sites that consult
 * these on Fabric are wired with the client mixin step (client-compile-plan.md).
 */
public interface IClientBlockExtensions {

    IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {
    };

    static IClientBlockExtensions of(BlockState state) {
        return of(state.getBlock());
    }

    static IClientBlockExtensions of(Block block) {
        return ClientExtensionsHooks.BLOCK_EXTENSIONS.getOrDefault(block, DEFAULT);
    }

    default boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        return false;
    }

    default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        return false;
    }
}
