package modernmods.modernfoundry.tools.modifiers.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;

/** Condition to check if a held tool has the given modifier */
@RequiredArgsConstructor
public class HasModifierLootCondition implements LootItemCondition {
  public static final MapCodec<HasModifierLootCondition> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(Identifier.CODEC.xmap(ModifierId::new, ModifierId::getIdentifier).fieldOf("modifier").forGetter(condition -> condition.modifier))
                        .apply(instance, HasModifierLootCondition::new)
  );
  private final ModifierId modifier;

  @Override
  public MapCodec<? extends LootItemCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(LootContext context) {
    ItemStack tool = context.getOptionalParameter(LootContextParams.TOOL) instanceof ItemStack stack ? stack : null;
    return tool != null && tool.is(TinkerTags.Items.MODIFIABLE) && ModifierUtil.getModifierLevel(tool, modifier) > 0;
  }

}
