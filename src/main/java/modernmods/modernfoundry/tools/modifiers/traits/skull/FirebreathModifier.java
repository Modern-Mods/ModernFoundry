package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.world.item.crafting.Ingredient;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.hook.interaction.KeybindInteractModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.data.ModifierIds;
import modernmods.modernfoundry.tools.modules.interaction.FireballModule;

/** @deprecated use {@link FireballModule} */
@Deprecated
public class FirebreathModifier extends NoLevelsModifier implements KeybindInteractModifierHook {
  @Override
  public int getPriority() {
    return 40;
  }

  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(FireballModule.builder().damageMultiplier(2.5f).fireball(Ingredient.of(TinkerTags.Items.FIREBALLS)).end().modifier(ModifierIds.fiery).build());
  }
}
