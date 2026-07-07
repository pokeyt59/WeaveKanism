package mekanism.fabric_shim.spi;

/**
 * Stand-in for net.neoforged.fml.loading.modscan.ModAnnotation — only the EnumHolder shape that
 * annotation-scan consumers pattern-match on.
 */
public class ModAnnotation {

    public record EnumHolder(String desc, String value) {
    }
}
