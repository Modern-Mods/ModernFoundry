package modernmods.modernfoundry.common.json;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class BlockOrEntityCondition implements LootItemCondition {
  public static final BlockOrEntityCondition INSTANCE = new BlockOrEntityCondition();
  public static final MapCodec<BlockOrEntityCondition> CODEC = MapCodec.unit(INSTANCE);

  private BlockOrEntityCondition() {}

  @Override
  public MapCodec<? extends LootItemCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(LootContext lootContext) {
    return lootContext.hasParameter(LootContextParams.THIS_ENTITY) || lootContext.hasParameter(LootContextParams.BLOCK_STATE);
  }

}
