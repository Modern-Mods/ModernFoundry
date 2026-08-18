package modernmods.modernfoundry.library.json.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.TConstruct;

/** @deprecated use {@link modernmods.mantle.recipe.condition.TagFilledCondition} */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class TagNotEmptyCondition<T> implements LootItemCondition, ICondition {
  private static final Identifier NAME = TConstruct.getResource("tag_not_empty");
  public static final MapCodec<TagNotEmptyCondition<?>> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(
      Identifier.CODEC.fieldOf("registry").forGetter(condition -> condition.tag.registry().identifier()),
      Identifier.CODEC.fieldOf("tag").forGetter(condition -> condition.tag.location())
    ).apply(instance, TagNotEmptyCondition::create)
  );
  private final TagKey<T> tag;

  public Identifier getID() {
    return NAME;
  }

  @Override
  public MapCodec<TagNotEmptyCondition<?>> codec() {
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

  private static TagNotEmptyCondition<?> create(Identifier registryName, Identifier tagName) {
    ResourceKey<? extends Registry<Object>> registry = ResourceKey.createRegistryKey(registryName);
    return new TagNotEmptyCondition<>(TagKey.create(registry, tagName));
  }
}
