package mekanism.fabric_shim.fml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Stand-in for net.neoforged.fml.loading.FMLPaths, backed by Fabric Loader's game/config dirs.
 */
public enum FMLPaths {
    GAMEDIR(FabricLoader.getInstance().getGameDir()),
    CONFIGDIR(FabricLoader.getInstance().getConfigDir());

    private final Path path;

    FMLPaths(Path path) {
        this.path = path;
    }

    public Path get() {
        return path;
    }

    public static Path getOrCreateGameRelativePath(Path path) {
        Path resolved = path.isAbsolute() ? path : GAMEDIR.get().resolve(path);
        try {
            Files.createDirectories(resolved);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create directory " + resolved, e);
        }
        return resolved;
    }
}
