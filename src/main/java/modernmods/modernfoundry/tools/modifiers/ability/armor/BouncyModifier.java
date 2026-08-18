package modernmods.modernfoundry.tools.modifiers.ability.armor;

import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule.TooltipStyle;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.shared.TinkerAttributes;

/** @deprecated use {@link AttributeModule} with {@link TinkerAttributes#BOUNCY} */
@Deprecated(forRemoval = true)
public class BouncyModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addModule(AttributeModule.builder(TinkerAttributes.BOUNCY, Operation.ADD_VALUE).uniqueFrom(getId().getIdentifier()).tooltipStyle(TooltipStyle.NONE).flat(1));
  }
}
