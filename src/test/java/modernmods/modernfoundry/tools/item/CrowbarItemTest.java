package modernmods.modernfoundry.tools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import modernmods.modernfoundry.fixture.ToolDefinitionFixture;
import modernmods.modernfoundry.smeltery.block.entity.component.SmelteryInputOutputBlockEntity.SmelteryFluidIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrowbarItemTest {
  @Test
  void cyclesFluidPortWhenUsedOnSmelteryFluidIo() {
    Level level = mock(Level.class);
    BlockPos pos = BlockPos.ZERO;
    SmelteryFluidIO port = mock(SmelteryFluidIO.class);
    when(level.getBlockEntity(pos)).thenReturn(port);
    when(port.cycleFluidPortMode()).thenReturn(true);

    CrowbarItem crowbar = new CrowbarItem(new Item.Properties().stacksTo(1), ToolDefinitionFixture.getStandardToolDefinition());
    UseOnContext context = new UseOnContext(level, null, InteractionHand.MAIN_HAND, ItemStack.EMPTY,
      new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false));

    assertThat(crowbar.useOn(context)).isEqualTo(InteractionResult.SUCCESS);
    verify(port).cycleFluidPortMode();
  }
}
