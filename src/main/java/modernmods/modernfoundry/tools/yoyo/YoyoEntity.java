package modernmods.modernfoundry.tools.yoyo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.tools.TinkerTools;

public final class YoyoEntity extends Entity implements TraceableEntity {
  private static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.ITEM_STACK);
  private static final EntityDataAccessor<Byte> HAND = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.BYTE);
  private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.INT);

  @Nullable
  private LivingEntity owner;
  private InteractionHand hand = InteractionHand.MAIN_HAND;
  private final YoyoController controller = new YoyoController();

  public YoyoEntity(EntityType<? extends YoyoEntity> type, Level level) {
    super(type, level);
    setNoGravity(true);
  }

  public YoyoEntity(Level level) {
    this(TinkerTools.yoyoEntity.get(), level);
  }

  public void sendRetract() {
    if (level().isClientSide) return;
    controller.signalRetract();
    TinkerNetwork.getInstance().sendToTrackingAndSelf(new YoyoRetractPacket(this), this);
  }

  @Override
  public void tick() {
    if (!level().isClientSide && shouldDiscard()) {
      discard();
      return;
    }
    super.tick();
    if (level().isClientSide) {
      resolveClientOwner();
      if (owner != null) {
        if (isStillHeld()) YoyoTracker.on(owner).setYoyoInHand(getHand(), this);
        else YoyoTracker.on(owner).clear(this);
      }
    }
    controller.tick(this);
  }

  @Override
  public void remove(RemovalReason reason) {
    super.remove(reason);
    LivingEntity trackerOwner = owner;
    if (trackerOwner == null && entityData.get(OWNER_ID) != 0 && level().getEntity(entityData.get(OWNER_ID)) instanceof LivingEntity living) {
      trackerOwner = living;
    }
    if (trackerOwner != null) YoyoTracker.on(trackerOwner).clear(this);
  }

  public Vector3d getCenterPos(Vector3d dest) {
    return YoyoUtil.storeEntityCenter(dest, this);
  }

  public void setCenterPos(Vector3d centerPos) {
    setPos(centerPos.x, centerPos.y - getBbHeight() / 2.0, centerPos.z);
  }

  public YoyoController getController() {
    return controller;
  }

  private boolean shouldDiscard() {
    return getYoyoStack().isEmpty() || owner == null || !isStillHeld();
  }

  private boolean isStillHeld() {
    return owner != null && ItemStack.isSameItemSameComponents(getYoyoStack(), owner.getItemInHand(getHand()));
  }

  public void setYoyoStack(ItemStack stack) {
    entityData.set(STACK, stack.copy());
  }

  public ItemStack getYoyoStack() {
    return entityData.get(STACK);
  }

  public void onThrow(LivingEntity owner, InteractionHand hand) {
    setOwner(owner);
    this.hand = hand;
    entityData.set(HAND, (byte) hand.ordinal());
    setYoyoStack(owner.getItemInHand(hand));
    controller.onThrow(this, owner);
  }

  public void setOwner(@Nullable LivingEntity owner) {
    if (owner != null) {
      this.owner = owner;
      entityData.set(OWNER_ID, owner.getId());
    }
  }

  @Nullable
  @Override
  public LivingEntity getOwner() {
    return owner;
  }

  public InteractionHand getHand() {
    return entityData.get(HAND) == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    builder.define(STACK, ItemStack.EMPTY);
    builder.define(HAND, (byte) 0);
    builder.define(OWNER_ID, 0);
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag tag) {}

  @Override
  protected void readAdditionalSaveData(CompoundTag tag) {}

  @Override
  public Packet<ClientGamePacketListener> getAddEntityPacket(net.minecraft.server.level.ServerEntity entity) {
    return new ClientboundAddEntityPacket(this, owner == null ? 0 : owner.getId(), blockPosition());
  }

  @Override
  public void recreateFromPacket(ClientboundAddEntityPacket packet) {
    super.recreateFromPacket(packet);
    if (level().getEntity(packet.getData()) instanceof LivingEntity living) {
      setOwner(living);
    } else {
      discard();
    }
  }

  private void resolveClientOwner() {
    if (owner == null && entityData.get(OWNER_ID) != 0 && level().getEntity(entityData.get(OWNER_ID)) instanceof LivingEntity living) {
      owner = living;
    }
  }

  @Override
  public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
    return true;
  }

  @Nullable
  @Override
  public PlayerTeam getTeam() {
    return owner != null ? owner.getTeam() : super.getTeam();
  }

  @Override
  public boolean isControlledByLocalInstance() {
    return owner instanceof Player player && player.isLocalPlayer();
  }

  @Override
  public boolean shouldBeSaved() {
    return false;
  }

  public Predicate<Entity> getCollisionPredicate() {
    return EntitySelector.NO_SPECTATORS.and(entity -> !(entity instanceof YoyoEntity));
  }

  public void getOwnerEyePos(Vector3d dest) {
    if (owner != null) dest.set(owner.getX(), owner.getY() + owner.getEyeHeight(), owner.getZ());
  }

  public boolean isEntityOwnerOrOwnersMount(Entity other) {
    return owner != null && (other == owner || other.isPassengerOfSameVehicle(owner));
  }
}
