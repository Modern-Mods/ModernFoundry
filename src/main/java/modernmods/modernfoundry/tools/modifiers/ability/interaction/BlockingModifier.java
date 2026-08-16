package modernmods.modernfoundry.tools.modifiers.ability.interaction;

import net.minecraft.world.item.UseAnim;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.combat.BlockingModule;

/** @deprecated use {@link modernmods.modernfoundry.tools.modules.combat.BlockingModule} */
@Deprecated(forRemoval = true)
public class BlockingModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(BlockingModule.INSTANCE);
  }

  @Override
  public int getPriority() {
    return 50; // late as many modifiers have special blocking interactions
  }

  /** @deprecated use {@link ModifierUtil#blockWhileCharging(IToolStackView, UseAnim)} */
  @Deprecated(forRemoval = true)
  public static UseAnim blockWhileCharging(IToolStackView tool, UseAnim fallback) {
    return ModifierUtil.blockWhileCharging(tool, fallback);
  }
}
