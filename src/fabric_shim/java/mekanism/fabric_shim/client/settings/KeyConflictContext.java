package mekanism.fabric_shim.client.settings;

import net.minecraft.client.Minecraft;

/**
 * Same surface as net.neoforged.neoforge.client.settings.KeyConflictContext (fresh
 * implementation): the three built-in contexts.
 */
public enum KeyConflictContext implements IKeyConflictContext {
    /** Active everywhere; conflicts with everything. */
    UNIVERSAL {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return true;
        }
    },
    /** Active while a screen is open. */
    GUI {
        @Override
        public boolean isActive() {
            return Minecraft.getInstance().screen != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    },
    /** Active while no screen is open. */
    IN_GAME {
        @Override
        public boolean isActive() {
            return !GUI.isActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return this == other;
        }
    }
}
