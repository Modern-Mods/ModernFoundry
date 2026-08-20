package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.network.TinkerNetwork;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

/** Runtime hand-to-entity state for both server gameplay and client rendering. */
@EventBusSubscriber(modid = TConstruct.MOD_ID)
public final class YoyoTracker {
  private static final Map<LivingEntity, YoyoTracker> TRACKERS = new WeakHashMap<>();

  private final LivingEntity parent;
  @Nullable private YoyoEntity mainHand;
  @Nullable private YoyoEntity offHand;

  private YoyoTracker(LivingEntity parent) {
    this.parent = parent;
  }

  public static YoyoTracker on(LivingEntity living) {
    return TRACKERS.computeIfAbsent(living, YoyoTracker::new);
  }

  public boolean hasYoyo(InteractionHand hand) {
    return getYoyoInHand(hand) != null;
  }

  @Nullable
  public YoyoEntity getYoyoInHand(InteractionHand hand) {
    YoyoEntity yoyo = hand == InteractionHand.MAIN_HAND ? mainHand : offHand;
    if (yoyo != null && (yoyo.isRemoved() || !ItemStack.isSameItemSameComponents(yoyo.getYoyoStack(), parent.getItemInHand(hand)))) {
      setYoyoInHand(hand, null);
      return null;
    }
    return yoyo;
  }

  public void setYoyoInHand(InteractionHand hand, @Nullable YoyoEntity yoyo) {
    if (hand == InteractionHand.MAIN_HAND) mainHand = yoyo;
    else offHand = yoyo;
    if (!parent.level().isClientSide) sync();
  }

  public void clearIf(InteractionHand hand, YoyoEntity yoyo) {
    if (getYoyoInHand(hand) == yoyo) setYoyoInHand(hand, null);
  }

  public void clear(YoyoEntity yoyo) {
    if (mainHand == yoyo) {
      setYoyoInHand(InteractionHand.MAIN_HAND, null);
    } else if (offHand == yoyo) {
      setYoyoInHand(InteractionHand.OFF_HAND, null);
    }
  }

  void apply(LevelEntityLookup lookup, int mainId, int offId) {
    if (mainId == 0) mainHand = null;
    else if (lookup.yoyo(mainId) instanceof YoyoEntity yoyo) mainHand = yoyo;
    if (offId == 0) offHand = null;
    else if (lookup.yoyo(offId) instanceof YoyoEntity yoyo) offHand = yoyo;
  }

  void syncTo(net.minecraft.server.level.ServerPlayer player) {
    TinkerNetwork.getInstance().sendTo(new YoyoTrackerPacket(this), player);
  }

  int parentId() {
    return parent.getId();
  }

  int yoyoId(InteractionHand hand) {
    YoyoEntity yoyo = getYoyoInHand(hand);
    return yoyo == null ? 0 : yoyo.getId();
  }

  private void sync() {
    TinkerNetwork.getInstance().sendToTrackingAndSelf(new YoyoTrackerPacket(this), parent);
  }

  @SubscribeEvent
  static void startTracking(PlayerEvent.StartTracking event) {
    if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;
    if (event.getTarget() instanceof LivingEntity living) on(living).syncTo(player);
    if (event.getTarget() instanceof YoyoEntity yoyo && yoyo.getOwner() instanceof LivingEntity living) on(living).syncTo(player);
  }

  interface LevelEntityLookup {
    @Nullable YoyoEntity yoyo(int id);
  }
}
