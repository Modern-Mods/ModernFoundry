package modernmods.modernfoundry.library.modifiers.modules.build;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.VolatileDataModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolDataNBT;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that just sets a float on a tool to the given value
 * @see VolatileFlagModule
 * @see VolatileIntModule
 */
public record VolatileFloatModule(ResourceLocation flag, LevelingValue value, ModifierCondition<IToolContext> condition) implements VolatileDataModifierHook, ProjectileLaunchModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileFloatModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileFloatModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileFloatModule::flag),
    LevelingValue.LOADABLE.directField(VolatileFloatModule::value),
    ModifierCondition.CONTEXT_FIELD, VolatileFloatModule::new);

  public VolatileFloatModule(ResourceLocation flag, LevelingValue value) {
    this(flag, value, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.putFloat(flag, volatileData.getFloat(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      persistentData.putFloat(flag, persistentData.getFloat(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public RecordLoadable<VolatileFloatModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
