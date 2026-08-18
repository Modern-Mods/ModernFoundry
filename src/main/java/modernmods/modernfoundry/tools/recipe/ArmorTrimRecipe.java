package modernmods.modernfoundry.tools.recipe;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import modernmods.mantle.recipe.IMultiRecipe;
import modernmods.mantle.util.RegistryHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.IntRange;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.RecipeResult;
import modernmods.modernfoundry.library.recipe.modifiers.ModifierRecipeLookup;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modules.cosmetic.TrimModule;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ArmorTrimRecipe implements ITinkerStationRecipe, IMultiRecipe<IDisplayModifierRecipe> {
  protected static final String KEY_INVALID_MATERIAL = TConstruct.makeTranslationKey("recipe", "modifier.armor_trim.invalid_material");
  protected static final String KEY_INVALID_PATTERN = TConstruct.makeTranslationKey("recipe", "modifier.armor_trim.invalid_pattern");


  @Getter
  private final Identifier id;

  public ArmorTrimRecipe(Identifier id) {
    this.id = id;
    ModifierRecipeLookup.addRecipeModifier(null, TinkerModifiers.trim);
  }

  /** Match for the trim item finding method */
  private record TrimItems(ItemStack template, ItemStack material) {}

  /** Finds the trim template and material */
  @Nullable
  private static TrimItems findInputs(ITinkerStationContainer inv) {
    ItemStack template = ItemStack.EMPTY;
    ItemStack material = ItemStack.EMPTY;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (!stack.isEmpty()) {
        // find the two inputs, but ensure no duplicates
        // 26.1 removed ItemTags.TRIM_TEMPLATES; templates are SmithingTemplateItem instances
        if (stack.getItem() instanceof SmithingTemplateItem) {
          if (!template.isEmpty()) {
            return null;
          }
          template = stack;
        }
        // 26.1 identifies trim materials by the PROVIDES_TRIM_MATERIAL data component
        if (stack.has(DataComponents.PROVIDES_TRIM_MATERIAL)) {
          if (!material.isEmpty()) {
            return null;
          }
          material = stack;
        }
      }
    }
    // if we found both, we match
    if (!material.isEmpty() && !template.isEmpty()) {
      return new TrimItems(template, material);
    }
    return null;
  }

  /**
   * Finds the trim pattern for the given template item.
   * 26.1 removed the runtime template->pattern lookup (TrimPattern no longer stores its template item, and the
   * mapping now lives in smithing trim recipe data). This reconstructs it from the trim pattern registry using the
   * vanilla "&lt;pattern&gt;_armor_trim_smithing_template" item naming convention.
   */
  @Nullable
  private static Holder<TrimPattern> findPattern(RegistryAccess access, ItemStack template) {
    Identifier templateId = BuiltInRegistries.ITEM.getKey(template.getItem());
    String templatePath = templateId.getPath();
    return access.lookupOrThrow(Registries.TRIM_PATTERN).listElements()
                 .filter(ref -> templatePath.equals(ref.key().identifier().getPath() + "_armor_trim_smithing_template")
                             || templatePath.equals(ref.key().identifier().getPath()))
                 .map(ref -> (Holder<TrimPattern>) ref)
                 .findFirst().orElse(null);
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    // ensure this modifier can be applied
    if (!inv.getTinkerableStack().is(TinkerTags.Items.TRIM)) {
      return false;
    }
    // need to locate two things: the trim material, and the trim template
    return findInputs(inv) != null;
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
    // first need to find our trim and material instances
    TrimItems trimItems = findInputs(inv);
    // should never happen
    if (trimItems == null) {
      return RecipeResult.pass();
    }
    // validate the material and pattern items
    Holder<TrimMaterial> material = trimItems.material.get(DataComponents.PROVIDES_TRIM_MATERIAL);
    if (material == null || material.unwrapKey().isEmpty()) {
      return RecipeResult.failure(KEY_INVALID_MATERIAL, trimItems.material.getDisplayName());
    }
    ToolStack original = inv.getTinkerable();
    Holder<TrimPattern> pattern = null;
    if (!original.hasTag(TinkerTags.Items.TRIM_NO_PATTERN)) {
      pattern = findPattern(access, trimItems.template);
      if (pattern == null) {
        return RecipeResult.failure(KEY_INVALID_PATTERN, trimItems.template.getDisplayName());
      }
    }

    // store into tool NBT
    ToolStack tool = inv.getTinkerable().copy();
    ModDataNBT persistentData = tool.getPersistentData();
    ModifierId modifier = TinkerModifiers.trim.getId();
    persistentData.putString(TrimModule.materialKey(modifier), material.unwrapKey().orElseThrow().identifier().toString());
    if (pattern != null) {
      persistentData.putString(TrimModule.patternKey(modifier), pattern.unwrapKey().orElseThrow().identifier().toString());
    }

    // add the modifier if missing
    if (tool.getModifierLevel(modifier) == 0) {
      tool.addModifier(modifier, 1);
    }
    return ITinkerStationRecipe.success(tool, inv);
  }

  @Override
  public RecipeSerializer<? extends ArmorTrimRecipe> getSerializer() {
    return TinkerModifiers.armorTrimSerializer.get();
  }


  /* JEI */

  private List<IDisplayModifierRecipe> displayRecipes = null;

  @SuppressWarnings("deprecation")
  @Override
  public List<IDisplayModifierRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      // 26.1 removed ItemTags.TRIM_TEMPLATES; trim templates are armor-trim SmithingTemplateItem instances
      List<ItemStack> trims = BuiltInRegistries.ITEM.entrySet().stream()
                                            .filter(e -> e.getValue() instanceof SmithingTemplateItem && e.getKey().identifier().getPath().endsWith("_armor_trim_smithing_template"))
                                            .map(e -> new ItemStack(e.getValue())).toList();
      List<ItemStack> toolInputs = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.TRIM)
                                                 .map(IModifiableDisplay::getDisplayStack).toList();
      if (!trims.isEmpty() && !toolInputs.isEmpty()) {
        Identifier id = getId();
        displayRecipes = access.lookupOrThrow(Registries.TRIM_MATERIAL).listElements()
          .map(material -> new DisplayRecipe(id, toolInputs, trims, material))
          .collect(Collectors.toList());
      } else {
        displayRecipes = List.of();
      }
    }
    return displayRecipes;
  }

  private static class DisplayRecipe implements IDisplayModifierRecipe {
    private static final IntRange LEVELS = new IntRange(1, 1);
    private final ModifierEntry RESULT = new ModifierEntry(TinkerModifiers.trim, 1);

    @Getter
    private final Identifier recipeId;
    @Getter
    private final List<ItemStack> toolWithoutModifier;
    @Getter
    private final List<ItemStack> toolWithModifier;
    private final List<ItemStack> trim;
    private final List<ItemStack> material;
    @Getter
    private final Component variant;

    public DisplayRecipe(Identifier id, List<ItemStack> tools, List<ItemStack> trim, Reference<TrimMaterial> holder) {
      this.recipeId = id;
      TrimMaterial material = holder.value();
      toolWithoutModifier = tools;
      this.trim = trim;
      // 26.1 TrimMaterial no longer carries an ingredient item; gather example items from the trim material tag that provide this material
      ResourceKey<TrimMaterial> materialKey = holder.key();
      this.material = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, ItemTags.TRIM_MATERIALS)
                                    .map(ItemStack::new)
                                    .filter(stack -> {
                                      Holder<TrimMaterial> provided = stack.get(DataComponents.PROVIDES_TRIM_MATERIAL);
                                      return provided != null && provided.is(materialKey);
                                    })
                                    .toList();
      this.variant = material.description().plainCopy();

      String materialName = holder.key().identifier().toString();
      List<ModifierEntry> results = List.of(RESULT);
      Identifier key = TrimModule.materialKey(TinkerModifiers.trim.getId());
      toolWithModifier = tools.stream().map(stack -> IDisplayModifierRecipe.withModifiers(stack, results, data -> data.putString(key, materialName))).toList();

    }

    @Override
    public int getInputCount() {
      return 2;
    }

    @Override
    public List<ItemStack> getDisplayItems(int slot) {
      return switch (slot) {
        case 0 -> trim;
        case 1 -> material;
        default -> List.of();
      };
    }

    @Override
    public ModifierEntry getDisplayResult() {
      return RESULT;
    }

    @Override
    public IntRange getLevel() {
      return LEVELS;
    }
  }
}
