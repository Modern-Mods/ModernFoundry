package modernmods.modernfoundry.library.modifiers.modules.armor;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.client.TooltipKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.damage.DamageSourcePredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.predicate.TinkerPredicate;
import modernmods.modernfoundry.library.json.variable.VariableFormula;
import modernmods.modernfoundry.library.json.variable.protection.ProtectionFormula;
import modernmods.modernfoundry.library.json.variable.protection.ProtectionVariable;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ProtectionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.tools.data.ModifierIds;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module to increase protection against the given source
 * @param source    Source to protect against
 * @param entity    Conditions on the entity wearing the armor
 * @param formula   Protection formula
 * @param condition Modifier module conditions
 */
public record ProtectionModule(IJsonPredicate<DamageSource> source, IJsonPredicate<LivingEntity> entity, IJsonPredicate<LivingEntity> attacker, ProtectionFormula formula, ModifierCondition<IToolStackView> condition) implements ProtectionModifierHook, TooltipModifierHook, ModifierModule, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ProtectionModule>defaultHooks(ModifierHooks.PROTECTION, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<ProtectionModule> LOADER = RecordLoadable.create(
    DamageSourcePredicate.LOADER.defaultField("damage_source", ProtectionModule::source),
    LivingEntityPredicate.LOADER.defaultField("wearing_entity", ProtectionModule::entity),
    LivingEntityPredicate.LOADER.defaultField("attacker", ProtectionModule::attacker), // TODO 1.21: remove as this is redundant to damage source predicate
    ProtectionFormula.LOADER.directField(ProtectionModule::formula),
    ModifierCondition.TOOL_FIELD,
    ProtectionModule::new);

  /** @apiNote Internal constructor, use {@link #builder()} */
  @Internal
  public ProtectionModule {}

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float getProtectionModifier(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float modifierValue) {
    // apply the main protection bonus
    if (condition.matches(tool, modifier) && this.source.matches(source) && this.entity.matches(context.getEntity()) && TinkerPredicate.matches(attacker, source.getEntity())) {
      modifierValue = formula.apply(tool, modifier, context, context.getEntity(), slotType, source, modifierValue);
    }
    return modifierValue;
  }

  /** Adds the tooltip for the module */
  public static void addResistanceTooltip(IToolStackView tool, Modifier modifier, float amount, @Nullable Player player, List<Component> tooltip) {
    double cap;
    if (player != null) {
      cap = ProtectionModifierHook.getProtectionCap(player);
    } else {
      cap = Math.min(20f + tool.getModifierLevel(ModifierIds.boundless) * 2.5f, 20 * 0.95f);
    }
    tooltip.add(modifier.applyStyle(
      Component.literal(Util.PERCENT_BOOST_FORMAT.format(Math.min(amount, cap) / 25f))
        .append(" ").append(Component.translatable(modifier.getTranslationKey() + ".resistance"))));
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (condition.matches(tool, modifier) && TinkerPredicate.matchesInTooltip(this.entity, player, tooltipKey)) {
      float value = formula.apply(tool, modifier, null, tooltipKey == TooltipKey.SHIFT ? player : null, null, null, 0);
      if (Math.abs(value) > 0.25f) {
        addResistanceTooltip(tool, modifier.getModifier(), value, player, tooltip);
      }
    }
  }

  @Override
  public RecordLoadable<ProtectionModule> getLoader() {
    return LOADER;
  }


  /* Builder */

  /* Creates a new builder instance */
  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(fluent = true)
  public static class Builder extends VariableFormula.Builder<Builder,ProtectionModule,ProtectionVariable> {
    private IJsonPredicate<DamageSource> source = DamageSourcePredicate.CAN_PROTECT;
    private IJsonPredicate<LivingEntity> entity = LivingEntityPredicate.ANY;
    private IJsonPredicate<LivingEntity> attacker = LivingEntityPredicate.ANY;

    private Builder() {
      super(ProtectionFormula.VARIABLES);
    }

    /** Sets the source to the given sources anded together */
    @SafeVarargs
    public final Builder sources(IJsonPredicate<DamageSource>... sources) {
      return source(DamageSourcePredicate.and(sources));
    }

    /** @deprecated Serves no function for protection modules, they are always percentage */
    @Deprecated(forRemoval = true)
    @Override
    public Builder percent() {
      return this;
    }

    @Override
    protected ProtectionModule build(ModifierFormula formula) {
      return new ProtectionModule(source, entity, attacker, new ProtectionFormula(formula, variables), condition);
    }
  }
}
