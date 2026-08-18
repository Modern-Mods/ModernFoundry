package modernmods.modernfoundry.tools.modifiers.ability.interaction;

import net.minecraft.world.item.ItemUseAnimation;
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

  /** @deprecated use {@link ModifierUtil#blockWhileCharging(IToolStackView, ItemUseAnimation)} */
  @Deprecated(forRemoval = true)
  public static ItemUseAnimation blockWhileCharging(IToolStackView tool, ItemUseAnimation fallback) {
    return ModifierUtil.blockWhileCharging(tool, fallback);
  }
}
