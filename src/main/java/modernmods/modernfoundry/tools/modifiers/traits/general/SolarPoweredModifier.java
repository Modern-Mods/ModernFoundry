package modernmods.modernfoundry.tools.modifiers.traits.general;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.behavior.ReduceToolDamageModule} with {@link modernmods.modernfoundry.library.json.variable.entity.EntityLightVariable} */
@Deprecated(forRemoval = true)
public class SolarPoweredModifier extends NoLevelsModifier implements ToolDamageModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
  }

  @Override
  public int getPriority() {
    return 185; // after tanned, before stoneshield
  }

  @Override
  public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
    if (holder != null) {
      Level world = holder.getCommandSenderWorld();
      // note this may go negative, that is not a problem
      int skylight = world.getBrightness(LightLayer.SKY, holder.blockPosition()) - world.getSkyDarken();
      if (skylight > 0) {
        float chance = skylight * 0.05f; // up to a 75% chance at max sunlight
        int maxDamage = amount;
        // for each damage we will take, if the random number is below chance, reduce
        for (int i = 0; i < maxDamage; i++) {
          if (RANDOM.nextFloat() < chance) {
            amount--;
          }
        }
      }
    }
    return amount;
  }
}
