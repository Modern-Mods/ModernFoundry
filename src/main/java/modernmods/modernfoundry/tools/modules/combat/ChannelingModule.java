package modernmods.modernfoundry.tools.modules.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import modernmods.hilt.data.loadable.primitive.BooleanLoadable;
import modernmods.hilt.data.loadable.primitive.FloatLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.LauncherHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Module causing a lighting strike at the target position */
public record ChannelingModule(float clearChance, float rainChance, float thunderChance, boolean allowMelee) implements ModifierModule, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, LauncherHitModifierHook {
  private static final List<ModuleHook<?>> MELEE_HOOKS = HookProvider.<ChannelingModule>defaultHooks(ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.LAUNCHER_HIT);
  private static final List<ModuleHook<?>> PROJECTILE_HOOKS = HookProvider.<ChannelingModule>defaultHooks(ModifierHooks.MELEE_HIT, ModifierHooks.LAUNCHER_HIT);
  public static final RecordLoadable<ChannelingModule> LOADER = RecordLoadable.create(
    FloatLoadable.PERCENT.requiredField("chance_clear", ChannelingModule::clearChance),
    FloatLoadable.PERCENT.requiredField("chance_rain", ChannelingModule::rainChance),
    FloatLoadable.PERCENT.requiredField("chance_thunder", ChannelingModule::thunderChance),
    BooleanLoadable.INSTANCE.requiredField("allow_melee", ChannelingModule::allowMelee),
    ChannelingModule::new);

  @Override
  public RecordLoadable<ChannelingModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return allowMelee ? MELEE_HOOKS : PROJECTILE_HOOKS;
  }

  /** Attempts to summon lightning at the target */
  private void tryStrike(Level level, @Nullable LivingEntity attacker, BlockPos target) {
    if (level instanceof ServerLevel && level.canSeeSky(target)) {
      // select chance based on weather
      float chance;
      if (level.isThundering()) {
        chance = thunderChance;
      } else if (level.isRaining()) {
        chance = rainChance;
      } else {
        chance = clearChance;
      }
      // if the chance passes, spawn lightning
      if (chance >= 1 || level.random.nextFloat() < chance) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
          lightning.moveTo(Vec3.atBottomCenterOf(target));
          if (attacker instanceof ServerPlayer player) {
            lightning.setCause(player);
          }
          level.addFreshEntity(lightning);
          level.playSound(null, target, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.NEUTRAL, 5, 1);
        }
      }
    }
  }

  @Override
  public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
    if (context.isFullyCharged() && (allowMelee || context.isProjectile())) {
      tryStrike(context.getLevel(), context.getPlayerAttacker(), context.getTarget().blockPosition());
    }
  }

  @Override
  public void onLauncherHitEntity(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity attacker, Entity target, @Nullable LivingEntity livingTarget, float damageDealt) {
    tryStrike(projectile.level(), attacker, target.blockPosition());
  }

  @Override
  public void onLauncherHitBlock(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity owner, BlockPos target) {
    tryStrike(projectile.level(), owner, target);
  }
}
