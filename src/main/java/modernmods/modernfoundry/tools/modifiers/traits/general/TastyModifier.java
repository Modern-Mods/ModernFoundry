package modernmods.modernfoundry.tools.modifiers.traits.general;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ProcessLootModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.behavior.EdibleModule;
import modernmods.modernfoundry.library.modifiers.modules.build.StatBoostModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerCommons;

import java.util.List;

/** @deprecated use {@link EdibleModule} and {@link modernmods.hilt.loot.AddEntryLootModifier} */
@Deprecated(forRemoval = true)
public class TastyModifier extends Modifier implements ProcessLootModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT);
    hookBuilder.addModule(new EdibleModule(TinkerCommons.bacon, LevelingInt.flat(16), LevelingInt.eachLevel(15), LevelingValue.eachLevel(0.15f)));
    hookBuilder.addModule(StatBoostModule.add(EdibleModule.HUNGER).eachLevel(1));
    hookBuilder.addModule(StatBoostModule.add(EdibleModule.SATURATION).flat(0.4f));
  }

  @Override
  public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
    // if no damage source, probably not a mob
    // otherwise blocks breaking (where THIS_ENTITY is the player) start dropping bacon
    if (!context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
      return;
    }

    // must have an entity
    Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
    if (entity != null && entity.getType().is(TinkerTags.EntityTypes.BACON_PRODUCER)) {
      // at tasty 1, 2, 3, and 4 its a 2%, 4.15%, 6.25%, 8% per level
      Integer lootingLevel = context.getParamOrNull(LootContextParams.ENCHANTMENT_LEVEL);
      int looting = lootingLevel == null ? 0 : lootingLevel;
      if (RANDOM.nextInt(48 / modifier.intEffectiveLevel()) <= looting) {
        // bacon
        generatedLoot.add(new ItemStack(TinkerCommons.bacon));
      }
    }
  }
}
