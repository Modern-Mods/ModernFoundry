package modernmods.modernfoundry.tools.modifiers.upgrades.melee;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

/** @deprecated use {@link modernmods.modernfoundry.tools.modules.combat.SweepingEdgeModule} */
@Deprecated(forRemoval = true)
public class SweepingEdgeModifier extends Modifier implements TooltipModifierHook {
  private static final Component SWEEPING_BONUS = TConstruct.makeTranslation("modifier", "sweeping_edge.attack_damage");

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.TOOLTIP);
  }

  /** @deprecated use {@link modernmods.modernfoundry.library.tools.definition.module.weapon.SweepWeaponAttack#getSweepingDamage(IToolStackView, float)} */
  @Deprecated(forRemoval = true)
  public float getSweepingDamage(IToolStackView toolStack, float baseDamage) {
    float level = toolStack.getModifier(this).getEffectiveLevel();
    float sweepingDamage = 1;
    if (level > 4) {
      sweepingDamage = baseDamage;
    } else if (level > 0) {
      // gives 25% per level, cap at base damage
      sweepingDamage = Math.min(baseDamage, level * 0.25f * baseDamage + 1);
    }
    return sweepingDamage;
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    float amount = modifier.getEffectiveLevel() * 0.25f;
    tooltip.add(applyStyle(Component.literal(Util.PERCENT_FORMAT.format(amount)).append(" ").append(SWEEPING_BONUS)));
  }
}
