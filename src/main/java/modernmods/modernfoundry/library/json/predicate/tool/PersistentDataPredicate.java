package modernmods.modernfoundry.library.json.predicate.tool;

import net.minecraft.resources.Identifier;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.json.variable.tool.ModDataSource;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;

/** Predicate that checks if a key is present in persistent data */
public record PersistentDataPredicate(Identifier key) implements ToolContextPredicate {
  public static final RecordLoadable<PersistentDataPredicate> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("key", PersistentDataPredicate::key), PersistentDataPredicate::new);

  @Override
  public boolean matches(IToolContext tool) {
    return tool.getPersistentData().contains(key);
  }

  @Override
  public RecordLoadable<PersistentDataPredicate> getLoader() {
    return LOADER;
  }
}
