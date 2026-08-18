package modernmods.modernfoundry.tools.modules.armor;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import modernmods.mantle.client.TooltipKey;
import modernmods.mantle.data.loadable.primitive.FloatLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.damage.DamageSourcePredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ProtectionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ProtectionModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModuleBuilder;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module implementing the depth protection modifier
 * @param baselineHeight  Y level of neutral behavior, buff goes negative above
 * @param neutralRange    Distance above baseline with no effect
 * @param amount          Multiplier to the protection level to grant
 */
// TODO: consider formula support in protection module
public record DepthProtectionModule(IJsonPredicate<DamageSource> source, IJsonPredicate<LivingEntity> entity, float baselineHeight, float neutralRange, LevelingValue amount, ModifierCondition<IToolStackView> condition) implements ModifierModule, ProtectionModifierHook, TooltipModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<DepthProtectionModule>defaultHooks(ModifierHooks.PROTECTION, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<DepthProtectionModule> LOADER = RecordLoadable.create(
    DamageSourcePredicate.LOADER.defaultField("damage_source", DepthProtectionModule::source),
    LivingEntityPredicate.LOADER.defaultField("wearing_entity", DepthProtectionModule::entity),
    FloatLoadable.ANY.requiredField("baseline_height", DepthProtectionModule::baselineHeight),
    FloatLoadable.FROM_ZERO.requiredField("neutral_range", DepthProtectionModule::neutralRange),
    LevelingValue.LOADABLE.directField(DepthProtectionModule::amount),
    ModifierCondition.TOOL_FIELD,
    DepthProtectionModule::new);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<DepthProtectionModule> getLoader() {
    return LOADER;
  }

  /** Gets the boost given the parameters */
  public static float getBonusMultiplier(LivingEntity entity, float baselineHeight, float neutralRange) {
    float y = (float)entity.getY();
    if (y < baselineHeight) {
      // just a linear scale of boosting from 0 to 2
      return Math.min((baselineHeight - y) / baselineHeight, 2);
    }
    // debuff above the range
    float debuffHeight = baselineHeight + neutralRange;
    if (y > debuffHeight) {
      return Math.max((debuffHeight - y) / baselineHeight, -1);
    }
    return  0;
  }

  @Override
  public float getProtectionModifier(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float modifierValue) {
    LivingEntity target = context.getEntity();
    if (this.condition.matches(tool, modifier) && this.source.matches(source) && this.entity.matches(target)) {
      modifierValue += getBonusMultiplier(context.getEntity(), baselineHeight, neutralRange) * amount.compute(modifier.getEffectiveLevel());
    }
    return modifierValue;
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (this.condition.matches(tool, modifier)) {
      float multiplier = 0;
      if (player == null || tooltipKey != TooltipKey.SHIFT) {
        multiplier = 1;
      } else if (this.entity.matches(player)) {
        multiplier = getBonusMultiplier(player, baselineHeight, neutralRange);
      }
      float amount = multiplier * this.amount.compute(modifier);
      if (Math.abs(amount) >= 0.25f) {
        ProtectionModule.addResistanceTooltip(tool, modifier.getModifier(), amount, player, tooltip);
      }
    }
  }


  /* Builder */

  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(fluent = true)
  public static class Builder extends ModuleBuilder.Stack<Builder> implements LevelingValue.Builder<DepthProtectionModule> {
    private IJsonPredicate<DamageSource> source = DamageSourcePredicate.CAN_PROTECT;
    private IJsonPredicate<LivingEntity> entity = LivingEntityPredicate.ANY;
    private float baselineHeight;
    private float neutralRange;

    @Override
    public DepthProtectionModule amount(float flat, float eachLevel) {
      return new DepthProtectionModule(source, entity, baselineHeight, neutralRange, new LevelingValue(flat, eachLevel), condition);
    }
  }
}
