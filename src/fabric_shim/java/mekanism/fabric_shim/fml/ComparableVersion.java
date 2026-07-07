package mekanism.fabric_shim.fml;

/**
 * Stand-in for org.apache.maven.artifact.versioning.ComparableVersion, used by Mekanism's
 * mod_version_loaded recipe condition. Compares dotted numeric versions segment by segment;
 * non-numeric segments fall back to string comparison, and a purely numeric version outranks one
 * with a qualifier at the same base (10.7.19 > 10.7.19-beta), which covers the practical cases.
 */
public final class ComparableVersion implements Comparable<ComparableVersion> {

    private final String raw;
    private final String[] tokens;

    public ComparableVersion(String version) {
        this.raw = version;
        this.tokens = version.split("[.\\-+_]");
    }

    @Override
    public int compareTo(ComparableVersion other) {
        int length = Math.max(tokens.length, other.tokens.length);
        for (int i = 0; i < length; i++) {
            String left = i < tokens.length ? tokens[i] : "";
            String right = i < other.tokens.length ? other.tokens[i] : "";
            Integer leftNum = tryParse(left);
            Integer rightNum = tryParse(right);
            int result;
            if (leftNum != null && rightNum != null) {
                result = Integer.compare(leftNum, rightNum);
            } else if (leftNum != null) {
                //numeric (or absent-treated-as-zero) outranks qualifier: 10.7.19 > 10.7.19-beta
                result = 1;
            } else if (rightNum != null) {
                result = -1;
            } else {
                result = left.compareToIgnoreCase(right);
            }
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static Integer tryParse(String token) {
        if (token.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ComparableVersion other && compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    @Override
    public String toString() {
        return raw;
    }
}
