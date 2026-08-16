package modernmods.modernfoundry.fixture;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ItemAbilities;
import modernmods.hilt.data.predicate.block.BlockPredicate;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.ToolDefinitionDataBuilder;
import modernmods.modernfoundry.library.tools.definition.module.build.ToolActionsModule;
import modernmods.modernfoundry.library.tools.definition.module.material.PartStatsModule;
import modernmods.modernfoundry.library.tools.definition.module.mining.IsEffectiveModule;

public final class ToolDefinitionFixture {
  private static final ResourceLocation DEFINITION_ID = new ResourceLocation("test", "test_tool");

  /** Standard tool definition for testing */
  private static final ToolDefinition STANDARD_TOOL_DEFINITION = new ToolDefinition(DEFINITION_ID);
  static {
    STANDARD_TOOL_DEFINITION.setData(
      ToolDefinitionDataBuilder.builder()
                               .module(PartStatsModule.parts()
                                                      .part(MaterialItemFixture.MATERIAL_ITEM_HEAD)
                                                      .part(MaterialItemFixture.MATERIAL_ITEM_HANDLE)
                                                      .part(MaterialItemFixture.MATERIAL_ITEM_EXTRA).build())
                               .module(ToolActionsModule.of(ItemAbilities.PICKAXE_DIG))
                               .module(new IsEffectiveModule(BlockPredicate.set(Blocks.STONE), true))
                               .smallToolStartingSlots()
                               .build());
  }
  public static ToolDefinition getStandardToolDefinition() {
    return STANDARD_TOOL_DEFINITION;
  }

  private ToolDefinitionFixture() {}
}
