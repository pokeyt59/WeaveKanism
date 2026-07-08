package mekanism.fabric_shim.event.level;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Stand-in for NeoForge's {@code BlockDropsEvent}. Compile-only; firing is Phase 3.
 */
public class BlockDropsEvent extends BlockEvent implements ICancellableEvent {

    private final ServerLevel level;
    @Nullable
    private final BlockEntity blockEntity;
    private final List<ItemEntity> drops;
    @Nullable
    private final Entity breaker;
    private final ItemStack tool;
    private int droppedExperience;

    public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, List<ItemEntity> drops,
          @Nullable Entity breaker, ItemStack tool) {
        super(level, pos, state);
        this.level = level;
        this.blockEntity = blockEntity;
        this.drops = drops;
        this.breaker = breaker;
        this.tool = tool;
    }

    public List<ItemEntity> getDrops() {
        return this.drops;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Nullable
    public Entity getBreaker() {
        return this.breaker;
    }

    public ItemStack getTool() {
        return this.tool;
    }

    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    public int getDroppedExperience() {
        return this.droppedExperience;
    }

    public void setDroppedExperience(int experience) {
        this.droppedExperience = experience;
    }
}
