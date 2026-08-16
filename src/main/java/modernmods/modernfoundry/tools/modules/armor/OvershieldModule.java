package modernmods.modernfoundry.tools.modules.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import modernmods.hilt.client.TooltipKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.damage.DamageSourcePredicate;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ProtectionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ProtectionModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Module converting overslime into bonus protection. */
public record OvershieldModule(LevelingValue protection, LevelingInt consumed) implements ModifierModule, ProtectionModifierHook, TooltipModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FlameBarrierModule>defaultHooks(ModifierHooks.PROTECTION, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<OvershieldModule> LOADER = RecordLoadable.create(
    LevelingValue.LOADABLE.requiredField("protection", OvershieldModule::protection),
    LevelingInt.LOADABLE.requiredField("consumed", OvershieldModule::consumed),
    OvershieldModule::new);

  @Override
  public RecordLoadable<OvershieldModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float getProtectionModifier(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float modifierValue) {
    if (DamageSourcePredicate.CAN_PROTECT.matches(source)) {
      int overslime = OverslimeModule.INSTANCE.getAmount(tool);
      if (overslime > 0) {
        float level = modifier.getEffectiveLevel();
        int target = this.consumed.compute(level);
        int consumed = Math.min(overslime, target);
        // scale the modifier value based on the consumed overslime
        modifierValue += protection.compute(level) * consumed / target;
        // remove overslime from the tool
        OverslimeModule.INSTANCE.removeAmount(tool, consumed);
      }
    }
    return modifierValue;
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    float level = modifier.getEffectiveLevel();
    float protection = this.protection.compute(level);
    if (tooltipKey == TooltipKey.SHIFT) {
      int overslime = OverslimeModule.INSTANCE.getAmount(tool);
      if (overslime == 0) {
        return;
      }
      int target = this.consumed.compute(level);
      protection *= Math.min(overslime, target) / (float) target;
    }
    ProtectionModule.addResistanceTooltip(tool, modifier.getModifier(), protection, player, tooltip);
  }
}
