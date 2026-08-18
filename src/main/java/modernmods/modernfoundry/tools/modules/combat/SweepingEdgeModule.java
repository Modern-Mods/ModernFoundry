package modernmods.modernfoundry.tools.modules.combat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import modernmods.mantle.client.TooltipKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.VolatileDataModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.definition.module.weapon.SweepWeaponAttack;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing {@link modernmods.modernfoundry.tools.data.ModifierIds#sweeping} */
public record SweepingEdgeModule(LevelingValue value) implements ModifierModule, VolatileDataModifierHook, TooltipModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SweepingEdgeModule>defaultHooks(ModifierHooks.VOLATILE_DATA, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<SweepingEdgeModule> LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(SweepingEdgeModule::value), SweepingEdgeModule::new);

  @Override
  public RecordLoadable<SweepingEdgeModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    volatileData.putFloat(SweepWeaponAttack.SWEEP_PERCENT, volatileData.getFloat(SweepWeaponAttack.SWEEP_PERCENT) + value.compute(modifier.getEffectiveLevel()));
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry entry, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    Modifier modifier = entry.getModifier();
    tooltip.add(modifier.applyStyle(
      Component.literal(Util.PERCENT_FORMAT.format(value.compute(entry.getEffectiveLevel())))
        .append(" ").append(Component.translatable(modifier.getTranslationKey() + ".attack_damage"))));
  }
}
