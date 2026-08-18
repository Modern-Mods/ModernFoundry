package modernmods.modernfoundry.tools.modules.ranged.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.tools.entity.ModifiableArrow;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing the arrow pierce modifier */
public record ArrowPierceModule(LevelingInt amount, ModifierCondition<IToolStackView> condition) implements ModifierModule, ProjectileLaunchModifierHook.NoShooter, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ArrowPierceModule>defaultHooks(ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT);
  public static final RecordLoadable<ArrowPierceModule> LOADER = RecordLoadable.create(LevelingInt.LOADABLE.directField(ArrowPierceModule::amount), ModifierCondition.TOOL_FIELD, ArrowPierceModule::new);

  @Override
  public RecordLoadable<ArrowPierceModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier) && arrow instanceof ModifiableArrow modifiableArrow) {
      int amount = this.amount.compute(modifier.getEffectiveLevel());
      if (amount > 0) {
        modifiableArrow.setPierceLevel((byte) amount);
      }
    }
  }
}
