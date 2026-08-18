package modernmods.modernfoundry.tools.modifiers.traits;

import net.minecraft.tags.DamageTypeTags;
import modernmods.mantle.data.predicate.damage.DamageSourcePredicate;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.capacity.CapacityBarModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.DamageToCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.DurabilityShieldModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

/** @deprecated use {@link CapacityBarModule}, {@link DurabilityShieldModule}, and {@link DamageToCapacityModule} */
@Deprecated(forRemoval = true)
public class FrostshieldModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(new CapacityBarModule(LevelingInt.eachLevel(100), ToolStats.DURABILITY));
    hookBuilder.addModule(new DurabilityShieldModule(0xAAFFFF));
    hookBuilder.addModule(DamageToCapacityModule.source(DamageSourcePredicate.tag(DamageTypeTags.IS_FREEZING)).reduceDamage().flat(1));
  }


  /* Shield */

  @Override
  public int getPriority() {
    // higher than overslime, to ensure this is removed first
    return 175;
  }
}
