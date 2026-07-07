package mekanism.fabric_shim.fml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import mekanism.fabric_shim.distmarker.Dist;

/**
 * Stand-in for net.neoforged.fml.common.Mod. Inert on Fabric — entry points are declared in
 * fabric.mod.json and the annotated class is constructed explicitly by the Fabric bootstrap — but
 * keeping the annotation lets upstream sources stay textually unchanged.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {

    String value();

    Dist[] dist() default {Dist.CLIENT, Dist.DEDICATED_SERVER};
}
