package mekanism.fabric_shim.fml;

/**
 * Stand-in for org.apache.maven.artifact.versioning.ArtifactVersion (maven-artifact is part of the
 * FML runtime on NeoForge and not present on Fabric). Provides the numeric accessors Mekanism's
 * Version record consumes; {@link #toString()} preserves the raw version string.
 */
public final class ArtifactVersion {

    private final String raw;
    private final int major;
    private final int minor;
    private final int incremental;

    public ArtifactVersion(String raw) {
        this.raw = raw;
        //Trim build metadata / prerelease qualifiers (10.7.19+fabric, 10.7.19-SNAPSHOT)
        String base = raw;
        for (char separator : new char[]{'+', '-'}) {
            int idx = base.indexOf(separator);
            if (idx != -1) {
                base = base.substring(0, idx);
            }
        }
        String[] parts = base.split("\\.");
        this.major = parseOrZero(parts, 0);
        this.minor = parseOrZero(parts, 1);
        this.incremental = parseOrZero(parts, 2);
    }

    private static int parseOrZero(String[] parts, int index) {
        if (index < parts.length) {
            try {
                return Integer.parseInt(parts[index]);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    public int getMajorVersion() {
        return major;
    }

    public int getMinorVersion() {
        return minor;
    }

    public int getIncrementalVersion() {
        return incremental;
    }

    @Override
    public String toString() {
        return raw;
    }
}
