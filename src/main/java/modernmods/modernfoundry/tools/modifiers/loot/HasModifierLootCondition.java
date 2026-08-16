package modernmods.modernfoundry.tools.modifiers.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.tools.TinkerModifiers;

/** Condition to check if a held tool has the given modifier */
@RequiredArgsConstructor
public class HasModifierLootCondition implements LootItemCondition {
  public static final MapCodec<HasModifierLootCondition> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(ResourceLocation.CODEC.xmap(ModifierId::new, id -> id).fieldOf("modifier").forGetter(condition -> condition.modifier))
                        .apply(instance, HasModifierLootCondition::new)
  );
  private final ModifierId modifier;

  @Override
  public LootItemConditionType getType() {
    return TinkerModifiers.hasModifierLootCondition.get();
  }

  @Override
  public boolean test(LootContext context) {
    ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
    return tool != null && tool.is(TinkerTags.Items.MODIFIABLE) && ModifierUtil.getModifierLevel(tool, modifier) > 0;
  }

}
