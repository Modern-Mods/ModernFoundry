package modernmods.modernfoundry.tools.modifiers.traits.general;

import net.minecraft.world.item.crafting.Ingredient;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.capacity.CapacityBarModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.DurabilityShieldModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.LootToCapacityModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

/** @deprecated use {@link CapacityBarModule}, {@link DurabilityShieldModule}, and {@link LootToCapacityModule} */
@Deprecated(forRemoval = true)
public class StoneshieldModifier extends Modifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(new CapacityBarModule(LevelingInt.eachLevel(100), ToolStats.DURABILITY));
    hookBuilder.addModule(new DurabilityShieldModule(0x7F7F7F));
    hookBuilder.addModule(LootToCapacityModule.consume(Ingredient.of(TinkerTags.Items.STONESHIELDS)).amount(3).eachLevel(0.2f));
  }

  @Override
  public int getPriority() {
    // higher than overslime, to ensure this is removed first
    return 175;
  }
}
