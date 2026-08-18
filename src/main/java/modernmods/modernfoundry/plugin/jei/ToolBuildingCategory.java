package modernmods.modernfoundry.plugin.jei;

import org.joml.Matrix3x2fStack;
import lombok.Getter;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.client.GuiUtil;
import modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolBuildingRecipe;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.layout.LayoutSlot;
import modernmods.modernfoundry.tools.TinkerTools;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolBuildingRecipe.SLOT_SIZE;
import static modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolBuildingRecipe.X_OFFSET;
import static modernmods.modernfoundry.library.recipe.tinkerstation.building.ToolBuildingRecipe.Y_OFFSET;

public class ToolBuildingCategory implements IRecipeCategory<ToolBuildingRecipe> {
  private static final Identifier BACKGROUND_LOC = TConstruct.getResource("textures/gui/jei/tinker_station.png");
  private static final Component TITLE = TConstruct.makeTranslation("jei", "tinkering.tool_building");
  @Getter
  private final IDrawable icon;
  @Getter
  private final IDrawable background;
  private final IDrawable anvil, slotBg, slotBorder;
  private final IDrawable itemCover;
  private static final int WIDTH = 134;
  private static final int HEIGHT = 66;
  private static final int ITEM_SIZE = 16;

  public ToolBuildingCategory(IGuiHelper guiHelper) {
    this.icon = guiHelper.createDrawableItemStack(TinkerTools.pickaxe.get().getRenderTool());
    this.background = guiHelper.createDrawable(BACKGROUND_LOC, 122, 77, WIDTH, HEIGHT);
    this.slotBg = guiHelper.createDrawable(BACKGROUND_LOC, 144, 59, SLOT_SIZE, SLOT_SIZE);
    this.slotBorder = guiHelper.createDrawable(BACKGROUND_LOC, 162, 59, SLOT_SIZE, SLOT_SIZE);
    this.anvil = guiHelper.createDrawable(BACKGROUND_LOC, 128, 61, ITEM_SIZE, ITEM_SIZE);
    this.itemCover = guiHelper.createDrawable(BACKGROUND_LOC, 122, 77, 70, 60);
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ToolBuildingRecipe recipe, IFocusGroup focuses) {
    List<List<ItemStack>> partsAndExtras = Stream.concat(recipe.getAllToolParts().stream(),
      recipe.getExtraRequirements().stream().map(ingredient -> Arrays.asList(ingredient.items().map(h -> new net.minecraft.world.item.ItemStack(h)).toArray(net.minecraft.world.item.ItemStack[]::new)))).toList();
    List<LayoutSlot> layoutSlots = recipe.getLayoutSlots();

    int missingSlots = partsAndExtras.size() - layoutSlots.size();

    if (missingSlots < 0) {
      partsAndExtras = new ArrayList<>(partsAndExtras);
      for (int additionalItem = 0; additionalItem > missingSlots; additionalItem--){
        // just add nothing to fill the empty slots
        partsAndExtras.add(List.of(ItemStack.EMPTY));
      }
    }

    IRecipeSlotBuilder firstSlot = null;
    for (int i = 0; i < layoutSlots.size(); i++) {
      IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, layoutSlots.get(i).getX() + X_OFFSET, layoutSlots.get(i).getY() + Y_OFFSET)
             .addItemStacks(partsAndExtras.get(i));
      if (i == 0) {
        firstSlot = slot;
      }
    }

    // create a focus link between result and first slot if same size
    List<ItemStack> result = recipe.getDisplayOutput();
    IRecipeSlotBuilder resultSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 26, 23).addItemStacks(result);
    if (result.size() > 1 && partsAndExtras.get(0).size() == result.size()) {
      builder.createFocusLink(resultSlot, firstSlot);
    }
  }

  @Override
  public int getWidth() {
    return WIDTH;
  }

  @Override
  public int getHeight() {
    return HEIGHT;
  }

  @Override
  public void draw(ToolBuildingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
    // getBackground() was removed in JEI 27.x; draw our background ourselves
    background.draw(graphics, 0, 0);
    // first, draw the item background
    ItemStack outputStack = recipe.getOutput() instanceof IModifiableDisplay modifiable ? modifiable.getRenderTool() : recipe.getOutput().asItem().getDefaultInstance();
    Matrix3x2fStack renderPose = graphics.pose();
    renderPose.pushMatrix();
    renderPose.translate(5f, 6.5f);
    renderPose.scale(3.7f, 3.7f);
    graphics.item(outputStack, 0, 0);
    renderPose.popMatrix();

    // next, overlay the item with transparent grey, makes it appear transparent
    itemCover.draw(graphics, 5, 6);

    // next, draw slot backgrounds very transparent
    for (LayoutSlot layoutSlot : recipe.getLayoutSlots()) {
      // need to offset by 1 because the inventory slot icons are 18x18
      this.slotBg.draw(graphics, layoutSlot.getX() + X_OFFSET - 1, layoutSlot.getY() + Y_OFFSET - 1);
    }
    // finally, draw slot borders opaque
    for (LayoutSlot layoutSlot : recipe.getLayoutSlots()) {
      // need to offset by 1 because the inventory slot icons are 18x18
      this.slotBorder.draw(graphics, layoutSlot.getX() + X_OFFSET - 1, layoutSlot.getY() + Y_OFFSET - 1);
    }

    // draw anvil icon if anvil is required
    if (recipe.requiresAnvil()) {
      this.anvil.draw(graphics, 76, 44);
    }
  }

  @Override
  public void getTooltip(ITooltipBuilder tooltip, ToolBuildingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
    if (recipe.requiresAnvil() && GuiUtil.isHovered((int) mouseX, (int) mouseY, 76, 44, ITEM_SIZE, ITEM_SIZE)) {
      tooltip.add(TConstruct.makeTranslation("jei", "tinkering.tool_building.anvil"));
    }
  }

  @Nonnull
  @Override
  public Component getTitle() {
    return TITLE;
  }

  @Nonnull
  @Override
  public RecipeType<ToolBuildingRecipe> getRecipeType() {
    return TConstructJEIConstants.TOOL_BUILDING;
  }

  @Override
  public Identifier getRegistryName(ToolBuildingRecipe recipe) {
    return recipe.getId();
  }
}
