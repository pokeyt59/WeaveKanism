package mekanism.fabric_shim.client.event;

import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stand-in for NeoForge's {@code ViewportEvent} (game bus): ComputeFogColor lets Mekanism tint the
 * fog for vision enhancement; RenderFog lets it push out the fog planes. Posted from fog-color and
 * fog-render mixins (step 3b) which read the (mutated) values back to apply them. Only the slice
 * Mekanism uses. Fresh implementation.
 */
public abstract class ViewportEvent extends Event {

    private final Camera camera;

    protected ViewportEvent(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return camera;
    }

    public static class ComputeFogColor extends ViewportEvent {

        private float red;
        private float green;
        private float blue;

        public ComputeFogColor(Camera camera, float red, float green, float blue) {
            super(camera);
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public float getRed() {
            return red;
        }

        public void setRed(float red) {
            this.red = red;
        }

        public float getGreen() {
            return green;
        }

        public void setGreen(float green) {
            this.green = green;
        }

        public float getBlue() {
            return blue;
        }

        public void setBlue(float blue) {
            this.blue = blue;
        }
    }

    public static class RenderFog extends ViewportEvent implements ICancellableEvent {

        private final FogType type;
        private float nearPlaneDistance;
        private float farPlaneDistance;

        public RenderFog(FogType type, Camera camera, float nearPlaneDistance, float farPlaneDistance) {
            super(camera);
            this.type = type;
            this.nearPlaneDistance = nearPlaneDistance;
            this.farPlaneDistance = farPlaneDistance;
        }

        public FogType getType() {
            return type;
        }

        public float getNearPlaneDistance() {
            return nearPlaneDistance;
        }

        public void setNearPlaneDistance(float distance) {
            this.nearPlaneDistance = distance;
        }

        public float getFarPlaneDistance() {
            return farPlaneDistance;
        }

        public void setFarPlaneDistance(float distance) {
            this.farPlaneDistance = distance;
        }

        public void scaleFarPlaneDistance(float factor) {
            this.farPlaneDistance *= factor;
        }
    }
}
