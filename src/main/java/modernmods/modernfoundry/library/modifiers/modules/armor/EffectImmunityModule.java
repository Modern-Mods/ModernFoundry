package modernmods.modernfoundry.library.modifiers.modules.armor;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.world.effect.MobEffect;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.technical.ArmorLevelModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.function.Supplier;

/**
 * Module for armor modifiers that makes the wearer immune to a mob effect
 */
public record EffectImmunityModule(MobEffect effect, LevelingInt maxLevel, ModifierCondition<IToolStackView> condition) implements ModifierModule, EquipmentChangeModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MobDisguiseModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final ComputableDataKey<Multiset<MobEffect>> EFFECT_IMMUNITY = TConstruct.createKey("effect_immunity", HashMultiset::create);
  private static final LevelingInt ANY_LEVEL = LevelingInt.flat(255);
  public static final RecordLoadable<EffectImmunityModule> LOADER = RecordLoadable.create(
    Loadables.MOB_EFFECT.requiredField("effect", EffectImmunityModule::effect),
    LevelingInt.LOADABLE.defaultField("max_level", ANY_LEVEL, false, EffectImmunityModule::maxLevel),
    ModifierCondition.TOOL_FIELD,
    EffectImmunityModule::new);

  /** @deprecated use {@link #EffectImmunityModule(MobEffect, LevelingInt, ModifierCondition)} */
  @Deprecated(forRemoval = true)
  public EffectImmunityModule(MobEffect effect, ModifierCondition<IToolStackView> condition) {
    this(effect, ANY_LEVEL, condition);
  }

  public EffectImmunityModule(MobEffect effect, LevelingInt maxLevel) {
    this(effect, maxLevel, ModifierCondition.ANY_TOOL);
  }

  public EffectImmunityModule(MobEffect effect) {
    this(effect, ANY_LEVEL);
  }

  public EffectImmunityModule(Supplier<? extends MobEffect> effect, LevelingInt maxLevel) {
    this(effect.get(), maxLevel);
  }

  public EffectImmunityModule(Supplier<? extends MobEffect> effect) {
    this(effect.get());
  }

  @Override
  public RecordLoadable<EffectImmunityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!tool.isBroken() && ArmorLevelModule.validSlot(tool, context.getChangedSlot(), TinkerTags.Items.HELD_ARMOR) && condition.matches(tool, modifier)) {
      TinkerDataCapability.Holder data = context.getDataHolder();
      if (data != null) {
        data.computeIfAbsent(EFFECT_IMMUNITY).add(effect, maxLevel.compute(modifier));
      }
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (!tool.isBroken() && ArmorLevelModule.validSlot(tool, context.getChangedSlot(), TinkerTags.Items.HELD_ARMOR) && condition.matches(tool, modifier)) {
      TinkerDataCapability.Holder data = context.getDataHolder();
      if (data != null) {
        Multiset<MobEffect> effects = data.get(EFFECT_IMMUNITY);
        if (effects != null) {
          effects.remove(effect, maxLevel.compute(modifier));
        }
      }
    }
  }
}
