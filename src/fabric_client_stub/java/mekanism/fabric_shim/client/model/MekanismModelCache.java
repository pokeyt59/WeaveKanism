package mekanism.fabric_shim.client.model;

import net.minecraft.resources.ResourceLocation;

/**
 * Server-safe stand-in for {@code mekanism.client.model.MekanismModelCache}. Common module setup
 * registers mekasuit module model locations here; a no-op on the server. Phase 4 loads the models.
 */
public final class MekanismModelCache {

    public static final MekanismModelCache INSTANCE = new MekanismModelCache();

    private MekanismModelCache() {
    }

    public void registerMekaSuitModuleModel(ResourceLocation rl) {
    }
}
