package mekanism.fabric_shim.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.fabric_shim.distmarker.Dist;

/**
 * Stand-in for net.neoforged.fml.common.EventBusSubscriber. On NeoForge, FML's annotation scan
 * auto-registers annotated classes to the chosen bus; Fabric has no scan data, so the Fabric
 * bootstrap registers these classes explicitly (see the residual checklist in PORTING.md — new
 * upstream {@code @EventBusSubscriber} classes must be added to the bootstrap's registration
 * list). The annotation is kept so upstream sources stay textually unchanged.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {

    String modid() default "";

    Bus bus() default Bus.GAME;

    Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};

    enum Bus {
        GAME,
        MOD
    }
}
