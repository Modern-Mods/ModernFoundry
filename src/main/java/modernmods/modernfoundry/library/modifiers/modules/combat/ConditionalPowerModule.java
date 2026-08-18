package modernmods.modernfoundry.library.modifiers.modules.combat;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.IntRange;
import modernmods.modernfoundry.library.json.math.ModifierFormula;
import modernmods.modernfoundry.library.json.predicate.TinkerPredicate;
import modernmods.modernfoundry.library.json.predicate.tool.ToolStackPredicate;
import modernmods.modernfoundry.library.json.variable.VariableFormula;
import modernmods.modernfoundry.library.json.variable.power.PowerFormula;
import modernmods.modernfoundry.library.json.variable.power.PowerVariable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ProjectileWithPower;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingForceModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ConditionalStatTooltip;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.tools.stat.INumericToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Implementation of projectile power conditioned on the attacker or target's properties.
 * It's better to use {@link modernmods.modernfoundry.library.modifiers.modules.behavior.ConditionalStatModule} unless you need the target condition.
 * @param target        Target condition
 * @param holder        Condition on entity using bow
 * @param formula       Power formula
 * @param modifierLevel Modifier level condition
 */
public record ConditionalPowerModule(IJsonPredicate<LivingEntity> target, IJsonPredicate<LivingEntity> holder, PowerFormula formula, IntRange modifierLevel) implements ModifierModule, ProjectileLaunchModifierHook.NoShooter, ProjectileHitModifierHook, SlingForceModifierHook, ConditionalStatTooltip {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ConditionalPowerModule>defaultHooks(ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_HIT, ModifierHooks.SLING_FORCE, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<ConditionalPowerModule> LOADER = RecordLoadable.create(
    LivingEntityPredicate.LOADER.defaultField("target", ConditionalPowerModule::target),
    LivingEntityPredicate.LOADER.defaultField("holder", ConditionalPowerModule::holder),
    PowerFormula.LOADER.directField(ConditionalPowerModule::formula),
    ModifierEntry.VALID_LEVEL.defaultField("modifier_level", ConditionalPowerModule::modifierLevel),
    ConditionalPowerModule::new);
  /** Projectile persistent data key for the stat multiplier */
  private static final Identifier AMMO_MULTIPLIER = ToolStats.PROJECTILE_DAMAGE.getName().withSuffix("_ammo_multiplier");
  /** Projectile persistent data key for the stat multiplier */
  private static final Identifier BOW_MULTIPLIER = ToolStats.PROJECTILE_DAMAGE.getName().withSuffix("_bow_multiplier");

  /** @apiNote Internal constructor, use {@link #builder()} */
  @Internal
  public ConditionalPowerModule {}

  @Override
  public boolean percent() {
    return formula.percent();
  }

  @Nullable
  @Override
  public Integer getPriority() {
    // run multipliers a bit later
    return percent() ? 75 : null;
  }

  @Override
  public RecordLoadable<ConditionalPowerModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    // copy projectile multiplier into the arrow damage so we can use it later
    // we use a separate key for ammo vs bow as those may mismatch. But for the same weapon its fine if multiple modifiers reuse it
    persistentData.putFloat(tool.hasTag(TinkerTags.Items.AMMO) ? AMMO_MULTIPLIER : BOW_MULTIPLIER, tool.getMultiplier(ToolStats.PROJECTILE_DAMAGE));
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    Identifier key = modifier.getId().getIdentifier();
    // if we already boosted power from an entity, don't boost again
    // minimizes issues with projectile bounces and piercing
    if (modifierLevel.test(modifier.getLevel()) && !persistentData.getBoolean(key)) {
      // as soon as we attempt to boost, mark this modifier as having run
      // means the second entity will not get to apply its boost if the first did not apply it
      persistentData.putBoolean(key, true);
      if (TinkerPredicate.matches(this.target, target) && TinkerPredicate.matches(this.holder, attacker)) {
        float multiplier = 1;
        if (persistentData.contains(AMMO_MULTIPLIER)) {
          multiplier *= persistentData.getFloat(AMMO_MULTIPLIER);
        }
        if (persistentData.contains(BOW_MULTIPLIER)) {
          multiplier *= persistentData.getFloat(BOW_MULTIPLIER);
        }
        if (projectile instanceof AbstractArrow arrow) {
          arrow.setBaseDamage(formula.apply(modifiers, persistentData, modifier, projectile, hit, attacker, target, arrow.baseDamage, multiplier));
        } else if (projectile instanceof ProjectileWithPower withPower) {
          withPower.setPower(formula.apply(modifiers, persistentData, modifier, projectile, hit, attacker, target, withPower.getPower(), multiplier));
        }
      }
    }
    return false;
  }

  @Override
  public float modifySlingForce(IToolStackView tool, ModifierEntry modifier, LivingEntity holder, LivingEntity target, ModifierEntry slingSource, float force, float multiplier) {
    if (modifierLevel.test(modifier.getLevel()) && ToolStats.PROJECTILE_DAMAGE.supports(tool.getItem()) && this.holder.matches(holder) && this.target.matches(target)) {
      return formula.apply(tool.getModifiers(), tool.getPersistentData(), modifier, null, null, holder, target, force, multiplier * tool.getMultiplier(ToolStats.PROJECTILE_DAMAGE));
    }
    return force;
  }

  @Override
  public INumericToolStat<?> stat() {
    return ToolStats.PROJECTILE_DAMAGE;
  }

  @Override
  public ModifierCondition<IToolStackView> condition() {
    return new ModifierCondition<>(ToolStackPredicate.ANY, modifierLevel);
  }

  @Override
  public boolean matchesTool(IToolStackView tool, ModifierEntry entry) {
    return modifierLevel.test(entry.getLevel());
  }

  @Override
  public float computeTooltipValue(IToolStackView tool, ModifierEntry entry, @Nullable Player player) {
    return formula.apply(tool.getModifiers(), tool.getPersistentData(), entry, null, null, player, null, 1, tool.getMultiplier(ToolStats.PROJECTILE_DAMAGE));
  }


  /* Builder */

  /** Creates a builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder class */
  @Setter
  @Accessors(fluent = true)
  public static class Builder extends VariableFormula.Builder<ConditionalPowerModule.Builder,ConditionalPowerModule, PowerVariable> {
    private IJsonPredicate<LivingEntity> target = LivingEntityPredicate.ANY;
    private IJsonPredicate<LivingEntity> holder = LivingEntityPredicate.ANY;

    private Builder() {
      super(PowerFormula.VARIABLES);
    }

    @Override
    protected ConditionalPowerModule build(ModifierFormula formula) {
      return new ConditionalPowerModule(target, holder, new PowerFormula(formula, variables, percent), condition.modifierLevel());
    }
  }
}
