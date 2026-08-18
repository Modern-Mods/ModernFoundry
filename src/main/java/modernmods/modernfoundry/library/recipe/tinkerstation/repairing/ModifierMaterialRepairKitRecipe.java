package modernmods.modernfoundry.library.recipe.tinkerstation.repairing;

import lombok.Getter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.definition.module.material.MaterialRepairModule;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.tables.recipe.CraftingTableRepairKitRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.behavior.MaterialRepairModule} */
@Deprecated(forRemoval = true)
public class ModifierMaterialRepairKitRecipe extends CraftingTableRepairKitRecipe implements IModifierMaterialRepairRecipe {
  public static final RecordLoadable<ModifierMaterialRepairKitRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), MODIFIER_FIELD, REPAIR_MATERIAL_FIELD, STAT_TYPE_FIELD, ModifierMaterialRepairKitRecipe::new);

  /** Tool that can be repaired with this recipe */
  @Getter
  private final ModifierId modifier;
  /** ID of material used in repairing */
  @Getter
  private final MaterialId repairMaterial;
  /** Stat type used for repairing, null means it will be fetched as the first available stat type */
  @Getter
  private final MaterialStatsId statType;
  public ModifierMaterialRepairKitRecipe(Identifier id, ModifierId modifier, MaterialId repairMaterial, MaterialStatsId statType) {
    super(id);
    this.modifier = modifier;
    this.repairMaterial = repairMaterial;
    this.statType = statType;
  }

  @Override
  protected boolean toolMatches(ItemStack stack) {
    return stack.is(TinkerTags.Items.DURABILITY) && ModifierUtil.getModifierLevel(stack, modifier) > 0;
  }

  @Override
  public boolean matches(CraftingInput inv, Level worldIn) {
    ToolRepair inputs = getRelevantInputs(inv);
    if (inputs == null || !repairMaterial.equals(IMaterialItem.getMaterialFromStack(inputs.repairKit()).getId())) {
      return false;
    }
    // tool must be damaged
    IToolStackView tool = ToolStack.from(inputs.tool());
    return tool.isBroken() || tool.getDamage() != 0;
  }

  @Override
  protected float getRepairAmount(IToolStackView tool, ItemStack repairStack) {
    return MaterialRepairModule.getDurability(tool.getDefinition().getId(), repairMaterial.getId(), statType) * tool.getModifierLevel(modifier);
  }

  @SuppressWarnings("removal")
  @Override
  public RecipeSerializer<? extends ModifierMaterialRepairKitRecipe> getSerializer() {
    return TinkerModifiers.craftingModifierMaterialRepair.get();
  }
}
