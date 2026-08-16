package modernmods.modernfoundry.library.json.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.hilt.util.RegistryHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.shared.TinkerCommons;

/** @deprecated use {@link modernmods.hilt.recipe.condition.TagFilledCondition} */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class TagNotEmptyCondition<T> implements LootItemCondition, ICondition {
  private static final ResourceLocation NAME = TConstruct.getResource("tag_not_empty");
  public static final MapCodec<TagNotEmptyCondition<?>> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(
      ResourceLocation.CODEC.fieldOf("registry").forGetter(condition -> condition.tag.registry().location()),
      ResourceLocation.CODEC.fieldOf("tag").forGetter(condition -> condition.tag.location())
    ).apply(instance, TagNotEmptyCondition::create)
  );
  private final TagKey<T> tag;

  @SuppressWarnings("removal")
  @Override
  public LootItemConditionType getType() {
    return TinkerCommons.lootTagNotEmptyCondition.get();
  }

  public ResourceLocation getID() {
    return NAME;
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return !context.getTag(tag).isEmpty();
  }

  @Override
  public boolean test(LootContext context) {
    Registry<T> registry = RegistryHelper.getRegistry(tag.registry());
    return registry != null && registry.getTagOrEmpty(tag).iterator().hasNext();
  }

  private static TagNotEmptyCondition<?> create(ResourceLocation registryName, ResourceLocation tagName) {
    ResourceKey<? extends Registry<Object>> registry = ResourceKey.createRegistryKey(registryName);
    return new TagNotEmptyCondition<>(TagKey.create(registry, tagName));
  }
}
