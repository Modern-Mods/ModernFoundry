package modernmods.modernfoundry.tools.modifiers.traits.general;

import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

/** @deprecated use {@link modernmods.modernfoundry.tools.modules.durability.ToolDamageRangeModule} */
@Deprecated(forRemoval = true)
public class TannedModifier extends NoLevelsModifier implements ToolDamageModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
  }

  @Override
  public int getPriority() {
    // higher than stoneshield, overslime, and reinforced
    return 200;
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
    return amount >= 1 ? 1 : 0;
  }
}
