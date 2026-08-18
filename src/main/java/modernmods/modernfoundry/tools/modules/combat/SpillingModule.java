package modernmods.modernfoundry.tools.modules.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectContext;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectManager;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffects;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.LauncherHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

import static modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper.TANK_HELPER;
import static modernmods.modernfoundry.tools.modifiers.ability.fluid.UseFluidOnHitModifier.spawnParticles;

/** Module to apply spilling effects post melee or projectile hit */
public record SpillingModule(LevelingValue level, ModifierCondition<IToolStackView> condition) implements ModifierModule, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, LauncherHitModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SpillingModule>defaultHooks(ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.LAUNCHER_HIT);
  public static final RecordLoadable<SpillingModule> LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(SpillingModule::level), ModifierCondition.TOOL_FIELD, SpillingModule::new);

  @Override
  public RecordLoadable<SpillingModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  /** Applies the fluid to the target */
  private void applyEffect(IToolStackView tool, ModifierEntry modifier, LivingEntity attacker, @Nullable Player playerAttacker, Entity target, @Nullable LivingEntity livingTarget, @Nullable Projectile projectile) {
    if (condition.matches(tool, modifier)) {
      FluidStack fluid = TANK_HELPER.getFluid(tool);
      if (!fluid.isEmpty()) {
        FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
        if (recipe.hasEntityEffects()) {
          int consumed = recipe.applyToEntity(fluid, this.level.compute(modifier.getEffectiveLevel()), FluidEffectContext.builder(attacker.level()).user(attacker, playerAttacker).projectile(projectile).target(target, livingTarget), FluidAction.EXECUTE);
          if (consumed > 0 && (playerAttacker == null || !playerAttacker.isCreative())) {
            spawnParticles(target, fluid);
            fluid.shrink(consumed);
            TANK_HELPER.setFluid(tool, fluid);
          }
        }
      }
    }
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    if (damageDealt > 0 && context.isFullyCharged()) {
      applyEffect(tool, modifier, context.getAttacker(), context.getPlayerAttacker(), context.getTarget(), context.getLivingTarget(), context.getProjectile());
    }
  }

  @Override
  public void onLauncherHitEntity(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity attacker, Entity target, @Nullable LivingEntity livingTarget, float damageDealt) {
    if (damageDealt > 0) {
      applyEffect(tool, modifier, attacker, ModifierUtil.asPlayer(attacker), target, livingTarget, projectile);
    }
  }
}
