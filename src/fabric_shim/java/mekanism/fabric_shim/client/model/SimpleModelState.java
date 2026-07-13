package mekanism.fabric_shim.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;

/**
 * Same surface as net.neoforged.neoforge.client.model.SimpleModelState: a vanilla {@link ModelState}
 * backed by a fixed {@link Transformation} + uv-lock flag. Fresh implementation.
 */
public class SimpleModelState implements ModelState {

    private final Transformation rotation;
    private final boolean uvLock;

    public SimpleModelState(Transformation rotation) {
        this(rotation, false);
    }

    public SimpleModelState(Transformation rotation, boolean uvLock) {
        this.rotation = rotation;
        this.uvLock = uvLock;
    }

    @Override
    public Transformation getRotation() {
        return rotation;
    }

    @Override
    public boolean isUvLocked() {
        return uvLock;
    }
}
