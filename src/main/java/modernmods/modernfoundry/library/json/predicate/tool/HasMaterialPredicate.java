package modernmods.modernfoundry.library.json.predicate.tool;

import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicate;
import modernmods.modernfoundry.library.json.predicate.material.MaterialPredicateField;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;

/**
 * Tool predicate checking for the given material on the tool
 * @param material   Material variant to locate.
 * @param index      Index to check for the material. If -1, will check all materials on the tool.
 */
public record HasMaterialPredicate(IJsonPredicate<MaterialVariantId> material, int index) implements ToolContextPredicate {
  public static final RecordLoadable<HasMaterialPredicate> LOADER = RecordLoadable.create(
    new MaterialPredicateField<>("material", HasMaterialPredicate::material),
    IntLoadable.FROM_MINUS_ONE.defaultField("index", -1, HasMaterialPredicate::index),
    HasMaterialPredicate::new);

  public HasMaterialPredicate(MaterialVariantId material, int index) {
    this(MaterialPredicate.variant(material), index);
  }

  public HasMaterialPredicate(MaterialVariantId material) {
    this(material, -1);
  }

  @Override
  public boolean matches(IToolContext input) {
    // if given an index, use exact location match
    if (index >= 0) {
      return material.matches(input.getMaterial(index).getVariant());
    }
    // otherwise, search each material
    for (MaterialVariant variant : input.getMaterials().getList()) {
      if (material.matches(variant.getVariant())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public RecordLoadable<HasMaterialPredicate> getLoader() {
    return LOADER;
  }
}
