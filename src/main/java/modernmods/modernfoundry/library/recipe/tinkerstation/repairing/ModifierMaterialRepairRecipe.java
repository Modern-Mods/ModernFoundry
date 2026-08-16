package modernmods.modernfoundry.library.recipe.tinkerstation.repairing;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.materials.definition.LazyMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationContainer;
import modernmods.modernfoundry.library.tools.definition.module.material.MaterialRepairModule;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tables.recipe.TinkerStationRepairRecipe;
import modernmods.modernfoundry.tools.TinkerModifiers;

/** @deprecated use {@link modernmods.modernfoundry.library.modifiers.modules.behavior.MaterialRepairModule} */
@Deprecated(forRemoval = true)
public class ModifierMaterialRepairRecipe extends TinkerStationRepairRecipe implements IModifierMaterialRepairRecipe {
  public static final RecordLoadable<ModifierMaterialRepairRecipe> LOADER = RecordLoadable.create(ContextKey.ID.requiredField(), MODIFIER_FIELD, REPAIR_MATERIAL_FIELD, STAT_TYPE_FIELD, ModifierMaterialRepairRecipe::new);

  /** Tool that can be repaired with this recipe */
  @Getter
  private final ModifierId modifier;
  /** ID of material used in repairing */
  private final LazyMaterial repairMaterial;
  /** Stat type used for repairing, null means it will be fetched as the first available stat type */
  @Getter
  private final MaterialStatsId statType;
  public ModifierMaterialRepairRecipe(ResourceLocation id, ModifierId modifier, MaterialId repairMaterialID, MaterialStatsId statType) {
    super(id);
    this.modifier = modifier;
    this.repairMaterial = LazyMaterial.of(repairMaterialID);
    this.statType = statType;
  }

  @Override
  public MaterialId getRepairMaterial() {
    return repairMaterial.getId();
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    if (repairMaterial.isUnknown()) {
      return false;
    }
    // must have the modifier
    ItemStack tinkerable = inv.getTinkerableStack();
    if (!tinkerable.is(TinkerTags.Items.MODIFIABLE) || ModifierUtil.getModifierLevel(tinkerable, modifier) == 0) {
      return false;
    }
    return findMaterialItem(inv, repairMaterial.getId());
  }

  @Override
  protected float getRepairAmount(IToolStackView tool, MaterialId repairMaterial) {
    return MaterialRepairModule.getDurability(tool.getDefinition().getId(), repairMaterial, statType) * tool.getModifierLevel(modifier);
  }

  @SuppressWarnings("removal")
  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.modifierMaterialRepair.get();
  }


  /** Find the repair item in the inventory */
  private static boolean findMaterialItem(ITinkerStationContainer inv, MaterialId repairMaterial) {
    // validate that we have at least one material
    boolean found = false;
    for (int i = 0; i < inv.getInputCount(); i++) {
      // skip empty slots
      ItemStack stack = inv.getInput(i);
      if (stack.isEmpty()) {
        continue;
      }

      // ensure we have a material
      if (!repairMaterial.equals(TinkerStationRepairRecipe.getMaterialFrom(inv, i))) {
        return false;
      }
      found = true;
    }
    return found;
  }
}
