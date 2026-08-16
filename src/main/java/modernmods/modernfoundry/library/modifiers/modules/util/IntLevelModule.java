package modernmods.modernfoundry.library.modifiers.modules.util;

import net.minecraft.util.Mth;
import modernmods.hilt.data.loadable.field.LoadableField;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;

/** @deprecated use {@link LevelingIntModule} */
@Deprecated(forRemoval = true)
public interface IntLevelModule {
  LoadableField<Integer,IntLevelModule> FIELD = IntLoadable.ANY_SHORT.defaultField("level", 1, true, IntLevelModule::level);

  /** Level of the leveling thing */
  int level();

  /** Gets the level to use for the module */
  default int getLevel(ModifierEntry modifier) {
    return Mth.floor(modifier.getEffectiveLevel() * level());
  }
}
