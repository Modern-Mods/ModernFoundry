package modernmods.modernfoundry.library.json.predicate.tool;

import net.minecraft.resources.ResourceLocation;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/** Predicate that checks if a key is present in volatile data */
public record VolatileDataPredicate(ResourceLocation key) implements ToolStackPredicate {
  public static final RecordLoadable<VolatileDataPredicate> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("key", VolatileDataPredicate::key), VolatileDataPredicate::new);

  @Override
  public boolean matches(IToolStackView tool) {
    return tool.getVolatileData().contains(key);
  }

  @Override
  public RecordLoadable<VolatileDataPredicate> getLoader() {
    return LOADER;
  }
}
