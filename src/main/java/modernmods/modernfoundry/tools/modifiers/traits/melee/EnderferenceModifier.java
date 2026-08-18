package modernmods.modernfoundry.tools.modifiers.traits.melee;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.json.RandomLevelingValue;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.combat.MobEffectModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.shared.TinkerEffects;

import javax.annotation.Nullable;

/** @deprecated use {@link MobEffectModule} and {@link TinkerEffects#ENDERFERENCE_KEY} */
@Deprecated(forRemoval = true)
public class EnderferenceModifier extends Modifier implements ProjectileLaunchModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addHook(this, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_THROWN);
    hookBuilder.addModule(MobEffectModule.builder(TinkerEffects.enderference).applyBeforeMelee(true).time(RandomLevelingValue.flat(100)).buildWeapon());
    hookBuilder.addModule(MobEffectModule.builder(TinkerEffects.enderference).time(RandomLevelingValue.flat(100)).toolTag(TinkerTags.Items.ARMOR).chance(LevelingValue.eachLevel(0.25f)).buildCounter());
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    persistentData.putBoolean(TinkerEffects.ENDERFERENCE_KEY, true);
  }
}
