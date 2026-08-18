package modernmods.modernfoundry.library.modifiers.modules.build;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingInt;
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
 * Module that just sets a integer on a tool to the given value
 * @see modernmods.modernfoundry.library.tools.definition.module.build.VolatileIntModule
 * @see VolatileFloatModule
 */
public record VolatileIntModule(Identifier flag, LevelingInt value, ModifierCondition<IToolContext> condition) implements VolatileDataModifierHook, ProjectileLaunchModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileIntModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileIntModule> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileIntModule::flag),
    LevelingInt.LOADABLE.directField(VolatileIntModule::value),
    ModifierCondition.CONTEXT_FIELD, VolatileIntModule::new);

  public VolatileIntModule(Identifier flag, LevelingInt value) {
    this(flag, value, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.putInt(flag, volatileData.getInt(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      persistentData.putInt(flag, persistentData.getInt(flag) + value.compute(modifier.getEffectiveLevel()));
    }
  }

  @Override
  public RecordLoadable<VolatileIntModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
