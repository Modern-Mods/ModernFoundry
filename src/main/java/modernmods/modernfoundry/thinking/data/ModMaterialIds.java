package modernmods.modernfoundry.thinking.data;

import modernmods.modernfoundry.TConstruct;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import modernmods.modernfoundry.library.materials.definition.MaterialId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ModMaterialIds {
    public static final MaterialId electrical_steel = id("electrical_steel");
    private static MaterialId id(String name) {
        return new MaterialId(TConstruct.MOD_ID, name);
    }
}
