package modernmods.modernfoundry.tools.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.definition.module.aoe.AreaOfEffectIterator.AOEMatchType;
import modernmods.modernfoundry.library.tools.definition.module.mining.IsEffectiveToolHook;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Client hooks for rendering the area-of-effect preview (outline on the extra blocks a tool would break, plus the block
 * breaking overlay on those extra blocks).
 * <p>
 * DEFERRED RENDER: the pre-26.1 world-render hooks this used were removed in the 26.1 render rewrite and have no drop-in
 * replacement:
 * <ul>
 *   <li>{@code RenderHighlightEvent} (the block-outline hook) was removed entirely; re-adding the AOE outline needs a
 *       {@code LevelRenderer} mixin.</li>
 *   <li>{@code BlockRenderDispatcher}/{@code Minecraft#getBlockRenderer()}/{@code renderBreakingTexture} and the crumbling
 *       buffer access were removed/restructured; the extra-block breaking overlay needs to be rebuilt against the new
 *       block-render pipeline.</li>
 * </ul>
 * The reusable AOE block-selection logic is kept here in {@link #getAoeBlocks(ItemStack, AOEMatchType)} for that future
 * mixin-based renderer; the actual drawing is validated in-game once re-hooked.
 */
public class ToolRenderEvents {
  private ToolRenderEvents() {}

  /** Maximum number of blocks from the iterator to render */
  public static final int MAX_BLOCKS = 60;

  /**
   * Resolves the extra blocks a held tool would affect at the currently targeted block, matching the pre-26.1 selection
   * used for the AOE outline and breaking overlay. Returns null when there is nothing to preview.
   * @param stack      Held tool stack
   * @param matchType  AOE match type (breaking or display)
   * @return  Iterator over the extra block positions, or null if none apply
   */
  @Nullable
  public static Iterator<BlockPos> getAoeBlocks(ItemStack stack, AOEMatchType matchType) {
    Level world = Minecraft.getInstance().level;
    Player player = Minecraft.getInstance().player;
    if (world == null || player == null) {
      return null;
    }
    if (stack.isEmpty() || !stack.is(TinkerTags.Items.MODIFIABLE)) {
      return null;
    }
    // must be targeting a block
    HitResult result = Minecraft.getInstance().hitResult;
    if (result == null || result.getType() != Type.BLOCK) {
      return null;
    }
    // must not be broken
    ToolStack tool = ToolStack.from(stack);
    if (tool.isBroken()) {
      return null;
    }
    BlockHitResult blockTrace = (BlockHitResult) result;
    BlockPos origin = blockTrace.getBlockPos();
    BlockState state = world.getBlockState(origin);
    if (matchType == AOEMatchType.BREAKING && !IsEffectiveToolHook.isEffective(tool, state)) {
      return null;
    }
    UseOnContext context = new UseOnContext(world, player, InteractionHand.MAIN_HAND, stack, blockTrace);
    Iterator<BlockPos> extraBlocks = tool.getHook(ToolHooks.AOE_ITERATOR).getBlocks(tool, context, state, matchType).iterator();
    return extraBlocks.hasNext() ? extraBlocks : null;
  }
}
