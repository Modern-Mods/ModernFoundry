package modernmods.modernfoundry.common.data.tags;

import net.minecraft.tags.TagEntry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.alchemy.Potion;
import modernmods.mantle.data.BuiltinRegistryTagProvider;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;

import java.util.concurrent.CompletableFuture;

public class PotionTagProvider extends BuiltinRegistryTagProvider<Potion> {
  @SuppressWarnings("deprecation")
  public PotionTagProvider(PackOutput packOutput, CompletableFuture<Provider> lookupProvider) {
    super(packOutput, BuiltInRegistries.POTION, lookupProvider, TConstruct.MOD_ID);
  }

  @Override
  protected void addTags(Provider provider) {
    tag(TinkerTags.Potions.HIDDEN_FLUID).add(TagEntry.optionalTag(TinkerTags.HIDDEN_FROM_RECIPE_VIEWERS));
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Potion Tags";
  }
}
