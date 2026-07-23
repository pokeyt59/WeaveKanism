package mekanism.fabric_shim.inject;

import net.minecraft.client.resources.model.ModelBakery;

/**
 * NeoForge's ModelManager patch surface: NeoForge retains the {@link ModelBakery} after each
 * resource reload so modders can re-bake with custom parameters. Attached to ModelManager via Loom
 * interface injection; ModelManagerMixin captures the bakery from the reload's apply step and
 * implements this.
 */
public interface MekModelManagerExt {

    //Injected-interface methods must be default (the class file is never modified to implement
    // them — an abstract one does not even resolve at compile time, verified). ModelManagerMixin's
    // class-level override always wins over this default at runtime.
    default ModelBakery getModelBakery() {
        throw new IllegalStateException("ModelManagerMixin not applied");
    }
}
