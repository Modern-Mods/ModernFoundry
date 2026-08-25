package modernmods.modernfoundry.smeltery.block.entity.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmelteryFluidPortModeTest {
  @Test
  void cyclesBetweenBidirectionalInputAndOutput() {
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.BOTH.next())
      .isEqualTo(SmelteryInputOutputBlockEntity.FluidPortMode.INPUT);
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.INPUT.next())
      .isEqualTo(SmelteryInputOutputBlockEntity.FluidPortMode.OUTPUT);
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.OUTPUT.next())
      .isEqualTo(SmelteryInputOutputBlockEntity.FluidPortMode.BOTH);
  }

  @Test
  void modeControlsFluidDirection() {
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.BOTH.allowsFill()).isTrue();
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.BOTH.allowsDrain()).isTrue();
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.INPUT.allowsFill()).isTrue();
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.INPUT.allowsDrain()).isFalse();
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.OUTPUT.allowsFill()).isFalse();
    assertThat(SmelteryInputOutputBlockEntity.FluidPortMode.OUTPUT.allowsDrain()).isTrue();
  }
}
