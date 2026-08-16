package modernmods.modernfoundry.tools.modifiers.upgrades.ranged;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.hook.interaction.UsingToolModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.data.ModifierIds;
import modernmods.modernfoundry.tools.modules.ZoomModule;

/** @deprecated use {@link ZoomModule} */
@Deprecated(forRemoval = true)
public class ScopeModifier extends Modifier {
  @Deprecated(forRemoval = true)
  public static final ResourceLocation SCOPE = ModifierIds.scope;

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(ZoomModule.SCOPE);
  }

  /** @deprecated no longer needed in modifiers. Call {@link UsingToolModifierHook#onUsingTick(IToolStackView, ModifierEntry, LivingEntity, int, int, ModifierEntry)} in tools. */
  @Deprecated(forRemoval = true)
  public static void scopingUsingTick(IToolStackView tool, LivingEntity entity, int chargeTime) {}

  /**
   * Cancels the scoping effect for the given entity.
   * @param entity  Entity
   * @deprecated No longer necessary to call in your modifier. For custom tools, see {@link UsingToolModifierHook#afterStopUsing(IToolStackView, LivingEntity, int)}
   */
  @Deprecated(forRemoval = true)
  public static void stopScoping(LivingEntity entity) {
    if (entity.level().isClientSide) {
      TinkerDataCapability.getCapability(entity).ifPresent(data -> data.computeIfAbsent(TinkerDataKeys.FOV_MODIFIER).remove(SCOPE));
    }
  }
}
