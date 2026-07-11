package mekanism.fabric_shim.client;

/**
 * Server-safe stand-in for {@code mekanism.client.SpecialColors} (the colormap-driven GUI palette).
 * Only the slice reached from common code is present: the creative tab label color. The real
 * atlas-parsing implementation (and the rest of the palette) is Phase 4 client work.
 */
public final class SpecialColors {

    private SpecialColors() {
    }

    public static final ColorRegistryObject TEXT_TITLE = new ColorRegistryObject(0xFF404040);

    /**
     * Mirrors the surface of {@code mekanism.client.render.lib.ColorAtlas.ColorRegistryObject}: a
     * registered palette slot exposing its ARGB value. The stub pins the upstream default color
     * instead of parsing the colormap texture.
     */
    public static final class ColorRegistryObject {

        private final int argb;

        ColorRegistryObject(int argb) {
            this.argb = argb;
        }

        public int argb() {
            return argb;
        }
    }
}
