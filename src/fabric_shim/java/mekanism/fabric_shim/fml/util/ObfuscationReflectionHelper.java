package mekanism.fabric_shim.fml.util;

import java.lang.reflect.Field;

/**
 * Reflection helper (stand-in for net.neoforged.fml.util.ObfuscationReflectionHelper; the slice
 * Mekanism uses). On NeoForge this resolves SRG names in production; on Fabric the dev and
 * production names are both Mojang-mapped under Loom, so a plain declared-field lookup is the
 * whole job.
 */
public final class ObfuscationReflectionHelper {

    private ObfuscationReflectionHelper() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Field findField(final Class<? super T> clazz, final String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new UnableToFindFieldException(e);
        }
    }

    public static class UnableToFindFieldException extends RuntimeException {

        public UnableToFindFieldException(Exception e) {
            super(e);
        }
    }
}
