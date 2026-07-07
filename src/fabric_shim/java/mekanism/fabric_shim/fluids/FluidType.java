package mekanism.fabric_shim.fluids;

import java.util.HashMap;
import java.util.Map;
import mekanism.fabric_shim.common.SoundAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * Mekanism-owned stand-in for NeoForge's {@code net.neoforged.neoforge.fluids.FluidType} — a data
 * holder describing a fluid's arbitrary attributes (density, temperature, sounds, ...). Fresh
 * implementation carrying only the surface the ported {@code src/main} touches; the full NeoForge
 * accessor set (entity-movement, pathfinding, dripstone, client extensions) is intentionally
 * omitted until the phases that need it.
 *
 * <p>Fluid &rarr; FluidType resolution lives in {@link FluidTypes} (vanilla water/lava/empty +
 * a generic default); Mekanism's own fluids carry their type through {@link BaseFlowingFluid}.
 */
public class FluidType {

    /** The number of fluid units that a bucket represents. */
    public static final int BUCKET_VOLUME = 1000;

    @Nullable
    private String descriptionId;
    private final int lightLevel;
    private final int density;
    private final int temperature;
    private final int viscosity;
    private final Rarity rarity;
    private final boolean air;
    private final Map<SoundAction, SoundEvent> sounds;

    public FluidType(Properties properties) {
        this(properties, false);
    }

    FluidType(Properties properties, boolean air) {
        this.descriptionId = properties.descriptionId;
        this.lightLevel = properties.lightLevel;
        this.density = properties.density;
        this.temperature = properties.temperature;
        this.viscosity = properties.viscosity;
        this.rarity = properties.rarity;
        this.sounds = Map.copyOf(properties.sounds);
        this.air = air;
    }

    /* Default accessors */

    public Component getDescription() {
        return Component.translatable(getDescriptionId());
    }

    public String getDescriptionId() {
        return this.descriptionId != null ? this.descriptionId : "fluid_type";
    }

    public int getLightLevel() {
        return this.lightLevel;
    }

    public int getDensity() {
        return this.density;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public int getViscosity() {
        return this.viscosity;
    }

    public Rarity getRarity() {
        return this.rarity;
    }

    @Nullable
    public SoundEvent getSound(SoundAction action) {
        return this.sounds.get(action);
    }

    @Nullable
    public SoundEvent getSound(@Nullable Player player, BlockGetter getter, BlockPos pos, SoundAction action) {
        return getSound(action);
    }

    /* Stack-based accessors */

    public Component getDescription(FluidStack stack) {
        return Component.translatable(getDescriptionId(stack));
    }

    public String getDescriptionId(FluidStack stack) {
        return getDescriptionId();
    }

    public int getLightLevel(FluidStack stack) {
        return getLightLevel();
    }

    public int getDensity(FluidStack stack) {
        return getDensity();
    }

    public int getTemperature(FluidStack stack) {
        return getTemperature();
    }

    public Rarity getRarity(FluidStack stack) {
        return getRarity();
    }

    public ItemStack getBucket(FluidStack stack) {
        return new ItemStack(stack.getFluid().getBucket());
    }

    /* Helper methods */

    public final boolean isAir() {
        return this.air;
    }

    public final boolean isLighterThanAir() {
        return this.density <= 0;
    }

    public BlockState getBlockForFluidState(BlockAndTintGetter getter, BlockPos pos, FluidState state) {
        return state.createLegacyBlock();
    }

    public FluidState getStateForPlacement(BlockAndTintGetter getter, BlockPos pos, FluidStack stack) {
        return stack.getFluid().defaultFluidState();
    }

    public boolean canBePlacedInLevel(BlockAndTintGetter getter, BlockPos pos, FluidState state) {
        return !getBlockForFluidState(getter, pos, state).isAir();
    }

    public boolean canBePlacedInLevel(BlockAndTintGetter getter, BlockPos pos, FluidStack stack) {
        return canBePlacedInLevel(getter, pos, getStateForPlacement(getter, pos, stack));
    }

    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        if (level.dimensionType().ultraWarm()) {
            return getStateForPlacement(level, pos, stack).is(FluidTags.WATER);
        }
        return false;
    }

    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
        SoundEvent sound = getSound(player, level, pos, mekanism.fabric_shim.common.SoundActions.FLUID_VAPORIZE);
        level.playSound(player, pos, sound != null ? sound : SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
              2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        for (int i = 0; i < 8; ++i) {
            level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(),
                  pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public String toString() {
        return "FluidType[" + getDescriptionId() + "]";
    }

    /**
     * The simple, builder-style properties of a fluid type. Mirrors the subset of NeoForge's
     * {@code FluidType.Properties} that Mekanism configures.
     */
    public static final class Properties {

        @Nullable
        private String descriptionId;
        private int lightLevel = 0;
        private int density = 1000;
        private int temperature = 300;
        private int viscosity = 1000;
        private Rarity rarity = Rarity.COMMON;
        private final Map<SoundAction, SoundEvent> sounds = new HashMap<>();

        private Properties() {
        }

        public static Properties create() {
            return new Properties();
        }

        public Properties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        public Properties sound(SoundAction action, SoundEvent sound) {
            this.sounds.put(action, sound);
            return this;
        }

        public Properties lightLevel(int lightLevel) {
            if (lightLevel < 0 || lightLevel > 15) {
                throw new IllegalArgumentException("The light level should be between [0,15].");
            }
            this.lightLevel = lightLevel;
            return this;
        }

        public Properties density(int density) {
            this.density = density;
            return this;
        }

        public Properties temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public Properties viscosity(int viscosity) {
            if (viscosity < 0) {
                throw new IllegalArgumentException("The viscosity should never be negative.");
            }
            this.viscosity = viscosity;
            return this;
        }

        public Properties rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }
    }
}
