package modernmods.modernfoundry.library.json.predicate.material;

import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;

/** Predicate matching materials that can use the given stat type */
public record MaterialHasPartPredicate(IMaterialItem part) implements MaterialPredicate {
  public static final RecordLoadable<MaterialHasPartPredicate> LOADER = RecordLoadable.create(TinkerLoadables.MATERIAL_ITEM.requiredField("part", MaterialHasPartPredicate::part), MaterialHasPartPredicate::new);

  @Override
  public boolean matches(MaterialVariantId variant) {
    return part.canUseMaterial(variant.getId());
  }

  @Override
  public RecordLoadable<? extends MaterialPredicate> getLoader() {
    return LOADER;
  }
}
