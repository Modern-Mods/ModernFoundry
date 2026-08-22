package modernmods.modernfoundry.thinking.data;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> last_effort = create("last_effort");
    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TConstruct.getResource(name));
    }
}
