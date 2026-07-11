package mekanism.fabric_shim.spi;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.Type;

/**
 * Stand-in for net.neoforged.neoforgespi.language.ModFileScanData. Fabric has no FML annotation
 * scan; the shim reports no data, so MekAnnotationScanner-driven features (computer method
 * binding) are inert until a Fabric-side scanner lands (deferred with the computer integrations —
 * see PORTING.md).
 */
public class ModFileScanData {

    public Set<AnnotationData> getAnnotations() {
        return Set.of();
    }

    public List<IModFileInfo> getIModInfoData() {
        return List.of();
    }

    public record AnnotationData(Type annotationType, ElementType targetType, Type clazz, String memberName, Map<String, Object> annotationData) {
    }
}
