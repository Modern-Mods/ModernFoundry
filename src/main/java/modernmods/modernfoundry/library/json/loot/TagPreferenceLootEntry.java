package modernmods.modernfoundry.library.json.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import modernmods.mantle.recipe.helper.TagPreference;
import modernmods.modernfoundry.TConstruct;

import java.util.List;
import java.util.function.Consumer;

/** @deprecated use {@link modernmods.mantle.loot.entry.TagPreferenceLootEntry} */
@Deprecated(forRemoval = true)
public class TagPreferenceLootEntry extends LootPoolSingletonContainer {
  public static final MapCodec<TagPreferenceLootEntry> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(entry -> entry.tag))
                        .and(singletonFields(instance))
                        .apply(instance, TagPreferenceLootEntry::newDeprecated)
  );
  private final TagKey<Item> tag;
  protected TagPreferenceLootEntry(TagKey<Item> tag, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
    super(weight, quality, conditions, functions);
    this.tag = tag;
  }

  @Override
  public MapCodec<? extends LootPoolSingletonContainer> codec() {
    return CODEC;
  }

  @Override
  protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
    TagPreference.getPreference(tag).ifPresent(item -> consumer.accept(new ItemStack(item)));
  }

  /** @deprecated use {@link modernmods.mantle.loot.entry.TagPreferenceLootEntry#tagPreference(TagKey)} */
  @Deprecated(forRemoval = true)
  public static LootPoolSingletonContainer.Builder<?> tagPreference(TagKey<Item> tag) {
    return modernmods.mantle.loot.entry.TagPreferenceLootEntry.tagPreference(tag);
  }

  private static TagPreferenceLootEntry newDeprecated(TagKey<Item> tag, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
    TConstruct.LOG.warn("Using deprecated tag preference loot entry 'modernfoundry:tag_preference', use 'mantle:tag_preference' instead");
    return new TagPreferenceLootEntry(tag, weight, quality, conditions, functions);
  }
}
