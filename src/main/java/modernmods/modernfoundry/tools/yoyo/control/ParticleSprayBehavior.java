package modernmods.modernfoundry.tools.yoyo.control;

import modernmods.modernfoundry.tools.yoyo.YoyoBehavior;
import modernmods.modernfoundry.tools.yoyo.YoyoContext;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3dc;

public final class ParticleSprayBehavior implements YoyoBehavior {
  @Override
  public void onTouchBlock(YoyoEntity yoyo, YoyoContext context, BlockPos touchedPos, Vector3dc touchedExact) {
    if (yoyo.level() instanceof ServerLevel serverLevel) {
      serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, serverLevel.getBlockState(touchedPos)), touchedExact.x(), touchedExact.y(), touchedExact.z(), 1, 0.0D, 0.0D, 0.0D, 0.15F);
    }
  }
}
