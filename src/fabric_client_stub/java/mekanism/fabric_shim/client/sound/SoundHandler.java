package mekanism.fabric_shim.client.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Server-safe stand-in for {@code mekanism.client.sound.SoundHandler}. Common (tile/player) code
 * starts and stops sounds through these statics; on a server they are no-ops. Real sound playback is
 * Phase 4. Lives in the portMain source set (references src/main {@code RadiationScale}).
 */
public final class SoundHandler {

    public static final Map<RadiationScale, Object> radiationSoundMap = new HashMap<>();

    private SoundHandler() {
    }

    public static void clearPlayerSounds() {
    }

    public static void clearPlayerSounds(UUID uuid) {
    }

    public static void startSound(LevelAccessor world, UUID uuid, PlayerSound.SoundType soundType) {
    }

    public static void startFlamethrowerSound(Player player) {
    }

    @Nullable
    public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos) {
        return null;
    }

    @Nullable
    public static SoundInstance startTileSound(SoundEvent soundEvent, SoundSource category, float volume, RandomSource random, BlockPos pos, boolean looping) {
        return null;
    }

    public static void stopTileSound(BlockPos pos) {
    }

    //UI feedback sounds (keybind mode switches): implemented for real — the client classes are
    // on the compile classpath, and these only ever run on the physical client
    public static void playSound(SoundEventRegistryObject<?> soundEventRO) {
        playSound(soundEventRO.get());
    }

    public static void playSound(SoundEvent sound) {
        playSound(SimpleSoundInstance.forUI(sound, 1, MekanismConfig.client.baseSoundVolume.get()));
    }

    public static void playSound(SoundInstance sound) {
        Minecraft.getInstance().getSoundManager().play(sound);
    }
}
