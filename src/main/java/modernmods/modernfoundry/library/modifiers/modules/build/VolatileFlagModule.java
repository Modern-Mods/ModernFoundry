package modernmods.modernfoundry.library.modifiers.modules.build;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
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
 * Module that just sets a boolean flag to true on a tool.
 * @see modernmods.modernfoundry.library.tools.definition.module.build.VolatileFlagModule
 * @see VolatileIntModule
 * @see VolatileFloatModule
 */
public record VolatileFlagModule(Identifier flag, ModifierCondition<IToolContext> condition) implements VolatileDataModifierHook, ProjectileLaunchModifierHook, ModifierModule, ConditionalModule<IToolContext> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<VolatileFlagModule>defaultHooks(ModifierHooks.VOLATILE_DATA);
  public static final RecordLoadable<VolatileFlagModule> LOADER = RecordLoadable.create(Loadables.RESOURCE_LOCATION.requiredField("flag", VolatileFlagModule::flag), ModifierCondition.CONTEXT_FIELD, VolatileFlagModule::new);

  public VolatileFlagModule(Identifier flag) {
    this(flag, ModifierCondition.ANY_CONTEXT);
  }

  @Override
  public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
    if (condition.matches(context, modifier)) {
      volatileData.putBoolean(flag, true);
    }
  }

  @Override
  public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      persistentData.putBoolean(flag, true);
    }
  }

  @Override
  public RecordLoadable<VolatileFlagModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
