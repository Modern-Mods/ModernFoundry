package modernmods.modernfoundry.tools.modifiers.traits;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.AttributesModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BreakSpeedModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Use {@link modernmods.modernfoundry.library.modifiers.modules.mining.ConditionalMiningSpeedModule} and {@link modernmods.modernfoundry.library.modifiers.modules.combat.ConditionalMeleeDamageModule}
 * with {@link modernmods.modernfoundry.library.json.variable.tool.ToolVariable#CURRENT_DAMAGE} and {@link modernmods.modernfoundry.library.json.variable.tool.StatMultiplierVariable}
 */
@Deprecated(forRemoval = true)
public class DamageSpeedTradeModifier extends Modifier implements AttributesModifierHook, TooltipModifierHook, BreakSpeedModifierHook {
  private static final Component MINING_SPEED = TConstruct.makeTranslation("armor_stat", "mining_speed");
  private final float multiplier;
  private final Lazy<ResourceLocation> attributeId = Lazy.of(() -> getId().withSuffix("/attack_damage"));

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.TOOLTIP, ModifierHooks.ATTRIBUTES, ModifierHooks.BREAK_SPEED);
  }

  /**
   * Creates a new instance of
   * @param multiplier  Multiplier. Positive boosts damage, negative boosts mining speed
   */
  public DamageSpeedTradeModifier(float multiplier) {
    this.multiplier = multiplier;
  }

  /** Gets the multiplier for this modifier at the current durability and level */
  private double getMultiplier(IToolStackView tool, int level) {
    return Math.sqrt(tool.getDamage() * level / tool.getMultiplier(ToolStats.DURABILITY)) * multiplier;
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    double boost = getMultiplier(tool, modifier.getLevel());
    if (boost != 0 && tool.hasTag(TinkerTags.Items.HARVEST)) {
      tooltip.add(applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format(-boost)).append(" ").append(MINING_SPEED)));
    }
  }

  @Override
  public void addAttributes(IToolStackView tool, ModifierEntry modifier, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer) {
    if (slot == EquipmentSlot.MAINHAND) {
      double boost = getMultiplier(tool, modifier.getLevel());
      if (boost != 0) {
        // half boost for attack speed, its
        consumer.accept(Attributes.ATTACK_DAMAGE.value(), new AttributeModifier(attributeId.get(), boost / 2, Operation.ADD_MULTIPLIED_TOTAL));
      }
    }
  }

  @Override
  public void onBreakSpeed(IToolStackView tool, ModifierEntry modifier, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    if (isEffective) {
      event.setNewSpeed((float)(event.getNewSpeed() * (1 - getMultiplier(tool, modifier.getLevel()))));
    }
  }
}
