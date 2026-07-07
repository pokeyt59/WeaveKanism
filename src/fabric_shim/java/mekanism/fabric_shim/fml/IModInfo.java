package mekanism.fabric_shim.fml;

/**
 * Stand-in for net.neoforged.neoforgespi.language.IModInfo, backed by Fabric mod metadata.
 */
public interface IModInfo {

    String getModId();

    String getDisplayName();

    ArtifactVersion getVersion();
}
