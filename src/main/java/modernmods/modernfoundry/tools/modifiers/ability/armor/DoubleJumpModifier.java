package modernmods.modernfoundry.tools.modifiers.ability.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule.TooltipStyle;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.shared.TinkerAttributes;
import modernmods.modernfoundry.tools.logic.DoubleJumpHandler;

import static modernmods.modernfoundry.library.tools.definition.ModifiableArmorMaterial.ARMOR_SLOTS;

/** @deprecated use {@link TinkerAttributes#JUMP_COUNT} */
@Deprecated(forRemoval = true)
public class DoubleJumpModifier extends Modifier {
  private Component levelOneName = null;
  private Component levelTwoName = null;

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(AttributeModule.builder(TinkerAttributes.JUMP_COUNT.get(), Operation.ADD_VALUE).slots(ARMOR_SLOTS).tooltipStyle(TooltipStyle.NONE).flat(1));
  }

  @Override
  public Component getDisplayName(int level) {
    if (level == 1) {
      if (levelOneName == null) {
        levelOneName = applyStyle(Component.translatable(getTranslationKey() + ".1"));
      }
      return levelOneName;
    }
    if (level == 2) {
      if (levelTwoName == null) {
        levelTwoName = applyStyle(Component.translatable(getTranslationKey() + ".2"));
      }
      return levelTwoName;
    }
    return super.getDisplayName(level);
  }

  /** @deprecated use {@link modernmods.modernfoundry.tools.logic.DoubleJumpHandler#extraJump(Player)} */
  @Deprecated(forRemoval = true)
  public static boolean extraJump(Player entity) {
    return DoubleJumpHandler.extraJump(entity);
  }
}
