package mekanism.fabric_shim.fluids;

import java.util.Optional;
import java.util.function.Supplier;
import mekanism.fabric_shim.common.SoundActions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * Mekanism-owned stand-in for NeoForge's {@code BaseFlowingFluid}: a configurable {@link FlowingFluid}
 * driven entirely by a {@link Properties} holder, so a mod can register a source/flowing pair without
 * subclassing per fluid. Fresh implementation of vanilla's {@link FlowingFluid} contract (method set
 * derived from vanilla {@code WaterFluid}); the {@link Properties} public surface matches NeoForge so
 * {@code FluidDeferredRegister} stays textually intact.
 *
 * <p>Behavioral simplifications for the port (source conversion off, water-like replacement rules)
 * mirror NeoForge's defaults for Mekanism's fluids, none of which convert to source or flow like
 * anything but water.
 */
public abstract class BaseFlowingFluid extends FlowingFluid {

    private final Supplier<? extends FluidType> fluidType;
    private final Supplier<? extends Fluid> flowing;
    private final Supplier<? extends Fluid> still;
    @Nullable
    private final Supplier<? extends Item> bucket;
    @Nullable
    private final Supplier<? extends LiquidBlock> block;
    private final int slopeFindDistance;
    private final int levelDecreasePerBlock;
    private final float explosionResistance;
    private final int tickRate;

    protected BaseFlowingFluid(Properties properties) {
        this.fluidType = properties.fluidType;
        this.flowing = properties.flowing;
        this.still = properties.still;
        this.bucket = properties.bucket;
        this.block = properties.block;
        this.slopeFindDistance = properties.slopeFindDistance;
        this.levelDecreasePerBlock = properties.levelDecreasePerBlock;
        this.explosionResistance = properties.explosionResistance;
        this.tickRate = properties.tickRate;
    }

    /**
     * The fluid's {@link FluidType}. Overrides the {@code getFluidType()} default injected onto vanilla
     * {@link Fluid}, so Mekanism fluids report their configured type rather than the generic fallback.
     */
    public FluidType getFluidType() {
        return this.fluidType.get();
    }

    @Override
    public Fluid getFlowing() {
        return this.flowing.get();
    }

    @Override
    public Fluid getSource() {
        return this.still.get();
    }

    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }

    @Override
    protected int getSlopeFindDistance(LevelReader level) {
        return this.slopeFindDistance;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return this.levelDecreasePerBlock;
    }

    @Override
    public Item getBucket() {
        return this.bucket != null ? this.bucket.get() : Items.AIR;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !isSame(fluid);
    }

    @Override
    public int getTickDelay(LevelReader level) {
        return this.tickRate;
    }

    @Override
    protected float getExplosionResistance() {
        return this.explosionResistance;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        if (this.block != null) {
            return this.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == this.still.get() || fluid == this.flowing.get();
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.ofNullable(getFluidType().getSound(SoundActions.BUCKET_FILL));
    }

    public static class Flowing extends BaseFlowingFluid {

        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 7));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static class Source extends BaseFlowingFluid {

        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    /** Builder describing a source/flowing fluid pair; mirrors NeoForge's {@code BaseFlowingFluid.Properties}. */
    public static class Properties {

        private final Supplier<? extends FluidType> fluidType;
        private final Supplier<? extends Fluid> still;
        private final Supplier<? extends Fluid> flowing;
        @Nullable
        private Supplier<? extends Item> bucket;
        @Nullable
        private Supplier<? extends LiquidBlock> block;
        private int slopeFindDistance = 4;
        private int levelDecreasePerBlock = 1;
        private float explosionResistance = 1;
        private int tickRate = 5;

        public Properties(Supplier<? extends FluidType> fluidType, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
            this.fluidType = fluidType;
            this.still = still;
            this.flowing = flowing;
        }

        public Properties bucket(Supplier<? extends Item> bucket) {
            this.bucket = bucket;
            return this;
        }

        public Properties block(Supplier<? extends LiquidBlock> block) {
            this.block = block;
            return this;
        }

        public Properties slopeFindDistance(int slopeFindDistance) {
            this.slopeFindDistance = slopeFindDistance;
            return this;
        }

        public Properties levelDecreasePerBlock(int levelDecreasePerBlock) {
            this.levelDecreasePerBlock = levelDecreasePerBlock;
            return this;
        }

        public Properties explosionResistance(float explosionResistance) {
            this.explosionResistance = explosionResistance;
            return this;
        }

        public Properties tickRate(int tickRate) {
            this.tickRate = tickRate;
            return this;
        }
    }
}
