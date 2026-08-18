package modernmods.modernfoundry.shared.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerEffect;
import modernmods.modernfoundry.library.events.teleport.ReturningTeleportEvent;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.utils.TeleportHelper;

public class ReturningEffect extends TinkerEffect {
  private static final Identifier KEY = TConstruct.getResource("returning");
  public ReturningEffect() {
    super(MobEffectCategory.NEUTRAL, 0xa92dff, true);
    NeoForge.EVENT_BUS.addListener((MobEffectEvent.Added event) -> this.onEffectAdded(event));
  }

  /** Called to set the return position when the effect is added */
  private void onEffectAdded(MobEffectEvent.Added event) {
    // store entity's current position when the effect is added
    LivingEntity entity = event.getEntity();
    if (!entity.level().isClientSide() && event.getOldEffectInstance() == null && event.getEffectInstance().getEffect().value() == this) {
      ModDataNBT data = PersistentDataCapability.getOrWarn(entity);
      CompoundTag tag = new CompoundTag();
      tag.put("pos", modernmods.modernfoundry.library.utils.TagUtil.writeBlockPos(entity.blockPosition()));
      tag.putString("dimension", entity.level().dimension().identifier().toString());
      data.put(KEY, tag);
    }
  }

  @Override
  public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
    return duration == 1;
  }

  @Override
  public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity living, int amplifier) {
    ModDataNBT data = PersistentDataCapability.getOrWarn(living);
    if (data.contains(KEY)) {
      CompoundTag tag = data.getCompound(KEY);
      Identifier dimension = Identifier.tryParse(tag.getStringOr("dimension", ""));
      // no teleporting if you switched dimensions
      // TODO: look into cross dimensional teleport, its doable with entity#teleportTo
      if (dimension != null && dimension.equals(living.level().dimension().identifier())) {
        modernmods.modernfoundry.library.utils.TagUtil.readBlockPos(tag, "pos").ifPresent(pos -> TeleportHelper.tryTeleport(new ReturningTeleportEvent(living, pos.getX(), pos.getY(), pos.getZ())));
      }
    }
    return true;
  }
}
