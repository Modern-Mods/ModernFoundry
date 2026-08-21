package modernmods.modernfoundry.tools.yoyo;

import com.mojang.datafixers.util.Pair;
import modernmods.modernfoundry.tools.yoyo.api.IYoyo;
import modernmods.modernfoundry.tools.network.YoyoCollectedDropsPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.tools.TinkerTools;

public class YoyoEntity extends Entity {
   public static final EntityDataAccessor<ItemStack> YOYO_STACK = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.ITEM_STACK);
   public static final EntityDataAccessor<Byte> HAND = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.BYTE);
   public static final EntityDataAccessor<Boolean> RETRACTING = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.BOOLEAN);
   public static final EntityDataAccessor<Integer> MAX_TIME = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> REMAINING_TIME = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Float> WEIGHT = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Float> CURRENT_LENGTH = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Float> MAX_LENGTH = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.FLOAT);
   public static final EntityDataAccessor<Integer> THROWER_ID = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.INT);
   public static final EntityDataAccessor<Integer> MAX_COLLECTED_DROPS = SynchedEntityData.defineId(YoyoEntity.class, EntityDataSerializers.INT);
   public static final float MAX_RETRACT_TIME = 40.0F;
   private static final Logger log = LoggerFactory.getLogger(YoyoEntity.class);
   public static Map<UUID, YoyoEntity> CASTERS = new HashMap<>();
   protected List<ItemStack> collectedDrops = new ArrayList<>();
   protected int numCollectedDrops = 0;
   protected boolean needCollectedSync = false;
   private Player thrower;
   private boolean isThrowerInitialized = false;
   protected ItemStack yoyoStackLastTick = ItemStack.EMPTY;
   private IYoyo yoyo;
   private boolean isYoyoInitialized = false;
   protected int attackCool = 0;
   protected int attackInterval = 0;
   protected boolean shouldResetCool = false;
   protected boolean canCancelRetract = true;
   protected int retractionTimeout = 0;
   protected int lastSlot = -1;
   protected boolean shouldGetStats = true;
   protected boolean doesBlockInteraction = true;

   public YoyoEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noCulling = true;
      this.setNoGravity(true);
   }

   public YoyoEntity(Level level) {
      this((EntityType<?>)TinkerTools.yoyoEntity.get(), level);
   }

   public YoyoEntity(Level level, Player player, InteractionHand hand) {
      this((EntityType<?>)TinkerTools.yoyoEntity.get(), level, player, hand);
   }

   public YoyoEntity(EntityType<?> type, Level level, Player player, InteractionHand hand) {
      this(type, level);
      this.setThrower(player);
      this.setHand(hand);
      CASTERS.put(player.getUUID(), this);
      Vec3 handPos = this.getPlayerHandPos(1.0F);
      this.setPos(handPos);
      if (!level.noCollision(this)) {
         this.setPos(player.getX(), player.getY() + this.getThrowerEyeHeight(), player.getZ());
      }
   }

   protected void defineSynchedData(Builder builder) {
      builder.define(YOYO_STACK, ItemStack.EMPTY);
      builder.define(HAND, (byte)InteractionHand.MAIN_HAND.ordinal());
      builder.define(RETRACTING, false);
      builder.define(MAX_TIME, -1);
      builder.define(REMAINING_TIME, -1);
      builder.define(WEIGHT, 1.0F);
      builder.define(CURRENT_LENGTH, 1.0F);
      builder.define(MAX_LENGTH, 1.0F);
      builder.define(MAX_COLLECTED_DROPS, 0);
      builder.define(THROWER_ID, 0);
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      this.collectedDrops.clear();
      ListTag list = tag.getList("collectedDrops", 10);

      for (int i = 0; i < list.size(); i++) {
         CompoundTag nbt = list.getCompound(i);
         nbt.putByte("Count", (byte)1);
         ItemStack stack = ItemStack.parseOptional(this.level().registryAccess(), nbt);
         stack.setCount(nbt.getInt("count"));
         this.collectedDrops.add(stack);
      }
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      ListTag list = new ListTag();
      this.collectedDrops
         .forEach(
            it -> {
               CompoundTag stackTag = new CompoundTag();
               ResourceLocation id = BuiltInRegistries.ITEM.getKey(it.getItem());
               stackTag.putString("id", id.toString());
               stackTag.putInt("count", it.getCount());
               stackTag.put(
                  "components",
                  (Tag)DataComponentPatch.CODEC
                     .encode(it.getComponentsPatch(), this.level().registryAccess().createSerializationContext(NbtOps.INSTANCE), new CompoundTag())
                     .getOrThrow()
               );
               list.add(stackTag);
            }
         );
      tag.put("collectedDrops", list);
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity p_352110_) {
      return new ClientboundAddEntityPacket(this, p_352110_);
   }

   public ItemStack collectDrop(ItemStack stack) {
      if (!this.isCollecting()) {
         return stack;
      }

      int maxTake = this.getMaxCollectedDrops() - this.numCollectedDrops;
      ItemStack take = stack.split(maxTake);
      this.collectedDrops.add(take);
      this.needCollectedSync = true;
      this.numCollectedDrops = this.numCollectedDrops + take.getCount();
      return stack;
   }

   public void collectDrop(ItemEntity drop) {
      if (drop != null) {
         ItemStack stack = drop.getItem();
         int countBefore = stack.getCount();
         this.collectDrop(stack);
         if (countBefore != stack.getCount()) {
            drop.setItem(stack);
            if (stack.isEmpty()) {
               drop.setNeverPickUp();
               drop.remove(RemovalReason.KILLED);
            }

            this.level()
               .playSound(
                  null,
                  drop.getX(),
                  drop.getY(),
                  drop.getZ(),
                  SoundEvents.ITEM_PICKUP,
                  SoundSource.NEUTRAL,
                  0.2F,
                  ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F
               );
         }
      }
   }

   public Vec3 getPlayerHandPos(Player thrower, Float partialTicks) {
      float yaw = thrower.getYRot();
      float pitch = thrower.getXRot();
      double posX = thrower.getX();
      double posY = thrower.getY();
      double posZ = thrower.getZ();
      if (partialTicks != 1.0F) {
         yaw = (float)this.interpolateValue(thrower.yRotO, yaw, partialTicks.floatValue());
         pitch = (float)this.interpolateValue(thrower.xRotO, pitch, partialTicks.floatValue());
         posX = this.interpolateValue(thrower.xo, posX, partialTicks.floatValue());
         posY = this.interpolateValue(thrower.yo, posY, partialTicks.floatValue());
         posZ = this.interpolateValue(thrower.zo, posZ, partialTicks.floatValue());
      }

      double throwerLookOffsetX = Math.cos(yaw * (float) (Math.PI / 180.0));
      double throwerLookOffsetZ = Math.sin(yaw * (float) (Math.PI / 180.0));
      double throwerLookOffsetY = Math.sin(pitch * (float) (Math.PI / 180.0));
      double throwerLookWidth = Math.cos(pitch * (float) (Math.PI / 180.0));
      float side = thrower.getMainArm() == HumanoidArm.RIGHT == (this.getHand() == InteractionHand.MAIN_HAND) ? 1.0F : -1.0F;
      return new Vec3(
         posX - throwerLookOffsetX * side * 0.4 - throwerLookOffsetZ * 0.5 * throwerLookWidth,
         posY + this.getThrowerEyeHeight() - throwerLookOffsetY * 0.5 - 0.25,
         posZ - throwerLookOffsetZ * side * 0.4 + throwerLookOffsetX * 0.5 * throwerLookWidth
      );
   }

   public Vec3 getPlayerHandPos(Float partialTicks) {
      if (!this.hasThrower()) {
         return new Vec3(this.getX(), this.getY(), this.getZ());
      }

      float yaw = this.thrower.getYRot();
      float pitch = this.thrower.getXRot();
      double posX = this.thrower.getX();
      double posY = this.thrower.getY();
      double posZ = this.thrower.getZ();
      if (partialTicks != 1.0F) {
         yaw = (float)this.interpolateValue(this.thrower.yRotO, yaw, partialTicks.floatValue());
         pitch = (float)this.interpolateValue(this.thrower.xRotO, pitch, partialTicks.floatValue());
         posX = this.interpolateValue(this.thrower.xo, posX, partialTicks.floatValue());
         posY = this.interpolateValue(this.thrower.yo, posY, partialTicks.floatValue());
         posZ = this.interpolateValue(this.thrower.zo, posZ, partialTicks.floatValue());
      }

      double throwerLookOffsetX = Math.cos(yaw * (float) (Math.PI / 180.0));
      double throwerLookOffsetZ = Math.sin(yaw * (float) (Math.PI / 180.0));
      double throwerLookOffsetY = Math.sin(pitch * (float) (Math.PI / 180.0));
      double throwerLookWidth = Math.cos(pitch * (float) (Math.PI / 180.0));
      float side = this.thrower.getMainArm() == HumanoidArm.RIGHT == (this.getHand() == InteractionHand.MAIN_HAND) ? 1.0F : -1.0F;
      return new Vec3(
         posX - throwerLookOffsetX * side * 0.4 - throwerLookOffsetZ * 0.5 * throwerLookWidth,
         posY + this.getThrowerEyeHeight() - throwerLookOffsetY * 0.5 - 0.25,
         posZ - throwerLookOffsetZ * side * 0.4 + throwerLookOffsetX * 0.5 * throwerLookWidth
      );
   }

   public double interpolateValue(double start, double end, double progress) {
      return start + (end - start) * progress;
   }

   public void forceRetract() {
      this.setRetracting(true);
      this.canCancelRetract = false;
   }

   public void resetOrIncrementAttackCooldown() {
      if (this.shouldResetCool) {
         this.attackCool = 0;
         this.shouldResetCool = false;
      } else {
         this.attackCool++;
      }
   }

   public int decrementRemainingTime() {
      return this.decrementRemainingTime(1);
   }

   public int decrementRemainingTime(int amount) {
      int out = this.getRemainingTime() - amount;
      this.setRemainingTime(out);
      return out;
   }

   public boolean canAttack() {
      return this.attackCool >= this.attackInterval;
   }

   public void resetAttackCooldown() {
      this.shouldResetCool = true;
   }

   public void tick() {
      super.tick();
      if (!this.hasThrower() && this.level().isClientSide) {
         this.resolveThrower();
      }
      this.xOld = this.getX();
      this.yOld = this.getY();
      this.zOld = this.getZ();
      if (this.hasThrower()) {
         if (this.checkAndGetYoyo() == null) {
            return;
         }

         this.setYoyo(this.checkAndGetYoyo());
         if (this.getMaxTime() >= 0 && this.decrementRemainingTime() < 0) {
            this.forceRetract();
         }

         this.updateMotion();
         this.moveAndCollide();
         this.yoyo.onUpdate(this.getYoyoStack(), this);
         if (!this.level().isClientSide && this.doesBlockInteraction()) {
            this.worldInteraction();
         }

         if (this.isCollecting()) {
            this.updateCapturedDrops();
         }

         this.resetOrIncrementAttackCooldown();
      } else if (!this.level().isClientSide) {
         this.remove(RemovalReason.UNLOADED_WITH_PLAYER);
      }
   }

   public float getRotation(int age, float partialTicks) {
      int maxTime = this.getMaxTime();
      float ageInTicks;
      if (maxTime < 0) {
         ageInTicks = age + partialTicks;
      } else {
         ageInTicks = maxTime - this.getRemainingTime() + partialTicks;
      }

      float multiplier = 35.0F;
      if (maxTime >= 0) {
         multiplier *= ageInTicks / maxTime;
      }

      return ageInTicks * multiplier;
   }

   protected void updateCapturedDrops() {
      if (!this.level().isClientSide && !this.collectedDrops.isEmpty() && this.needCollectedSync) {
         Iterator<ItemStack> iterator = this.collectedDrops.iterator();
         Map<Item, ItemStack> existing = new HashMap<>();

         while (iterator.hasNext()) {
            ItemStack collectedDrop = iterator.next();
            if (!collectedDrop.isEmpty()) {
               Item item = collectedDrop.getItem();
               ItemStack master = existing.get(item);
               if (collectedDrop.equals(master)) {
                  master.setCount(master.getCount() + collectedDrop.getCount());
                  iterator.remove();
               } else {
                  existing.put(item, collectedDrop);
               }
            }
         }

         if (!this.level().isClientSide) {
            TinkerNetwork.getInstance().sendToTrackingAndSelf(new YoyoCollectedDropsPacket(this), this);
         }

         this.needCollectedSync = false;
      }
   }

   public void createItemDropOrCollect(ItemStack stack) {
      ItemStack remaining = stack;
      if (this.isCollecting()) {
         remaining = this.collectDrop(stack);
         if (remaining.isEmpty()) {
            return;
         }
      }

      ItemEntity item = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), remaining);
      item.setDefaultPickUpDelay();
      this.level().addFreshEntity(item);
   }

   protected void worldInteraction() {
      BlockPos pos = this.blockPosition();
      AABB entityBox = this.getBoundingBox().inflate(0.1);
      BlockPos.betweenClosedStream(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
         .map(p -> new Pair(p.immutable(), this.level().getBlockState(p)))
         .filter(p -> !((BlockState)p.getSecond()).isAir())
         .filter(
            p -> ((BlockState)p.getSecond())
               .getShape(this.level(), (BlockPos)p.getFirst())
               .toAabbs()
               .stream()
               .anyMatch(bb -> bb.move((BlockPos)p.getFirst()).intersects(entityBox))
         )
         .forEach(
            p -> this.yoyo
               .blockInteraction(
                  this.getYoyoStack(),
                  this.thrower,
                  this.level(),
                  (BlockPos)p.getFirst(),
                  (BlockState)p.getSecond(),
                  ((BlockState)p.getSecond()).getBlock(),
                  this
               )
         );
   }

   protected void moveAndCollide() {
      AABB yoyoBox = this.getBoundingBox();
      AABB targetBox = yoyoBox.move(this.getDeltaMovement());
      if (this.noPhysics) {
         Vec3 pos = targetBox.getCenter();
         this.setPos(pos);
      } else {
         AABB union = yoyoBox.minmax(targetBox);
         List<AABB> collisions = new ArrayList<>();

         for (VoxelShape voxelShape : this.level().getCollisions(null, union)) {
            collisions.addAll(voxelShape.toAabbs());
         }

         List<Entity> entities = this.level().getEntities(this, union);
         int steps = 50;

         for (int step = 1; step < steps; step++) {
            Vec3 motion = this.getDeltaMovement();
            double dx = motion.x / step;
            double dy = motion.y / step;
            double dz = motion.z / step;

            for (AABB collision : collisions) {
               dx = calculateOffset(collision, yoyoBox, dx, 'x');
               dy = calculateOffset(collision, yoyoBox, dy, 'y');
               dz = calculateOffset(collision, yoyoBox, dz, 'z');
            }

            yoyoBox = yoyoBox.move(dx, dy, dz);

            for (AABB collision : collisions) {
               if (collision.intersects(yoyoBox)) {
                  dx = calculateOffset(collision, yoyoBox, dx, 'x');
                  dy = calculateOffset(collision, yoyoBox, dy, 'y');
                  dz = calculateOffset(collision, yoyoBox, dz, 'z');
                  yoyoBox = yoyoBox.move(-dx, -dy, -dz);
               }
            }

            if (!this.level().isClientSide) {
               Iterator<Entity> iterator = entities.iterator();

               while (iterator.hasNext()) {
                  Entity entity = iterator.next();
                  if (entity == this.thrower) {
                     iterator.remove();
                  } else if (entity.getBoundingBox().intersects(yoyoBox)) {
                     this.interactWithEntity(entity);
                     iterator.remove();
                  }
               }
            }
         }
      }
   }

   private void interactWithEntity(Entity entity) {
      this.yoyo.entityInteraction(this.getYoyoStack(), this.thrower, this.getHand(), this, entity);
   }

   protected void updateMotion() {
      Vec3 motion = this.getTarget()
         .subtract(this.getX(), this.getY() + this.getDimensions(this.getPose()).height() / 2.0F, this.getZ())
         .scale(0.15 + 0.85 * Math.pow(1.1, -((10.0F - this.getWeight()) * (10.0F - this.getWeight()))));
      if (this.isInWater()) {
         motion = motion.scale(this.yoyo.getWaterMovementModifier(this.getYoyoStack()));
      }

      this.setDeltaMovement(motion);
      this.move(MoverType.SELF, this.getDeltaMovement());
   }

   protected Vec3 getTarget() {
      if (!this.isRetracting()) {
         Vec3 eyePos = new Vec3(this.getThrower().getX(), this.getThrower().getY() + this.getThrowerEyeHeight(), this.getThrower().getZ());
         Vec3 look = this.getThrower().getViewVector(1.0F);
         double cordLength = this.getCurrentLength();
         Vec3 target = new Vec3(eyePos.x + look.x * cordLength, eyePos.y + look.y * cordLength, eyePos.z + look.z * cordLength);
         this.retractionTimeout = 0;
         HitResult result = this.getRaycast(eyePos, target);
         if (result != null) {
            target = result.getLocation();
         }

         return target;
      } else {
         Vec3 handPos = this.getPlayerHandPos(1.0F);
         double dX = this.getX() - handPos.x;
         double dY = this.getY() - handPos.y;
         double dZ = this.getZ() - handPos.z;
         if (dX * dX + dY * dY + dZ * dZ < 0.8 || this.retractionTimeout++ >= 40.0F) {
            this.remove(RemovalReason.KILLED);
         }

         return handPos;
      }
   }

   private HitResult getRaycast(Vec3 from, Vec3 to) {
      double distance = from.distanceTo(to);
      HitResult result = this.level().clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, this.getThrower()));
      boolean flag = false;
      double d1 = distance;
      if (distance > 3.0) {
         flag = true;
      }

      if (result != null) {
         d1 = result.getLocation().distanceTo(from);
      }

      Vec3 vec3d1 = this.thrower.getViewVector(1.0F);
      Entity pointedEntity = null;
      Vec3 vec3d3 = null;
      AABB expanded = this.thrower.getBoundingBox().expandTowards(vec3d1.x * distance, vec3d1.y * distance, vec3d1.z * distance);
      List<Entity> listEntity = this.level().getEntities((Entity)null, expanded, e -> !(e instanceof Player) || !e.isSpectator() && e.canBeCollidedWith());
      double d2 = d1;

      for (Entity entity : listEntity) {
         if (entity != this && entity != this.thrower) {
            AABB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vec3> rayResult = box.clip(from, to);
            if (box.contains(from)) {
               if (d2 >= 0.0) {
                  pointedEntity = entity;
                  vec3d3 = rayResult.orElse(from);
                  d2 = 0.0;
               }
            } else if (rayResult.isPresent()) {
               double d3 = from.distanceTo(rayResult.get());
               if (d3 < d2 || d2 == 0.0) {
                  if (entity.getRootVehicle() != this.thrower.getRootVehicle() || this.thrower.canRiderInteract()) {
                     pointedEntity = entity;
                     vec3d3 = rayResult.get();
                     d2 = d3;
                  } else if (d2 == 0.0) {
                     pointedEntity = entity;
                     vec3d3 = rayResult.get();
                  }
               }
            }
         }
      }

      if (vec3d3 != null) {
         if (pointedEntity != null && flag) {
            pointedEntity = null;
            result = BlockHitResult.miss(vec3d3, Direction.UP, BlockPos.containing(vec3d3));
         }

         if (pointedEntity != null && result == null) {
            result = new EntityHitResult(pointedEntity, vec3d3);
         }
      }

      return result;
   }

   public static double calculateOffset(AABB one, AABB other, double offset, char axis) {
      return switch (axis) {
         case 'x' -> {
            if (other.maxY > one.minY && other.minY < one.maxY && other.maxZ > one.minZ && other.minZ < one.maxZ) {
               if (offset > 0.0 && other.maxX <= one.minX) {
                  double d1 = one.minX - other.maxX;
                  if (d1 < offset) {
                     yield d1;
                  }
               } else if (offset < 0.0 && other.minX >= one.maxX) {
                  double d0 = one.maxX - other.minX;
                  if (d0 > offset) {
                     yield d0;
                  }
               }
            }
            yield 0.0;
         }
         case 'y' -> {
            if (other.maxX > one.minX && other.minX < one.maxX && other.maxZ > one.minZ && other.minZ < one.maxZ) {
               if (offset > 0.0 && other.maxY <= one.minY) {
                  double d1 = one.minY - other.maxY;
                  if (d1 < offset) {
                     yield d1;
                  }
               } else if (offset < 0.0 && other.minY >= one.maxY) {
                  double d0 = one.maxY - other.minY;
                  if (d0 > offset) {
                     yield d0;
                  }
               }
            }
            yield 0.0;
         }
         case 'z' -> {
            if (other.maxX > one.minX && other.minX < one.maxX && other.maxY > one.minY && other.minY < one.maxY) {
               if (offset > 0.0 && other.maxZ <= one.minZ) {
                  double d1 = one.minZ - other.maxZ;
                  if (d1 < offset) {
                     yield d1;
                  }
               } else if (offset < 0.0 && other.minZ >= one.maxZ) {
                  double d0 = one.maxZ - other.minZ;
                  if (d0 > offset) {
                     yield d0;
                  }
               }
            }
            yield 0.0;
         }
         default -> throw new IllegalArgumentException("Invalid axis: " + axis);
      };
   }

   protected IYoyo checkAndGetYoyo() {
      InteractionHand hand = this.getHand();
      ItemStack stack = this.thrower.getItemInHand(hand);
      this.setYoyoStack(stack);
      int currentSlot = hand == InteractionHand.MAIN_HAND ? this.getThrower().getInventory().selected : -2;
      ItemStack otherHand = this.getThrower().getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
      if (CASTERS.containsKey(this.getThrower().getUUID())
         && stack.getItem() instanceof IYoyo
         && (this.tickCount <= 1 || (this.lastSlot == -1 || this.lastSlot == currentSlot) && otherHand != this.yoyoStackLastTick)) {
         this.yoyoStackLastTick = stack;
         if (stack.getMaxDamage() < stack.getDamageValue()) {
            this.remove(RemovalReason.KILLED);
            return null;
         }

         if (!this.level().isClientSide && CASTERS.get(this.getThrower().getUUID()) != this) {
            CASTERS.put(this.getThrower().getUUID(), this);
         }

         IYoyo yoyo = (IYoyo)stack.getItem();
         if (!this.level().isClientSide && this.shouldGetStats) {
            this.setMaxCollectedDrops(yoyo.getMaxCollectedDrops(stack, this.level().registryAccess()));
            this.attackInterval = yoyo.getAttackInterval(stack);
            int duration = yoyo.getDuration(stack);
            this.setMaxTime(duration);
            this.setRemainingTime(duration);
            float maxLength = (float)yoyo.getLength(stack);
            this.setCurrentLength(maxLength);
            this.setMaxLength(maxLength);
            this.setWeight((float)yoyo.getWeight(stack));
            this.setInteractsWithBlocks(yoyo.interactsWithBlocks(stack));
            this.shouldGetStats = false;
         }

         this.lastSlot = currentSlot;
         return yoyo;
      } else {
         this.remove(RemovalReason.KILLED);
         return null;
      }
   }

   public void remove(RemovalReason p_146834_) {
      super.remove(p_146834_);
      boolean hasThrower = this.hasThrower();
      if (hasThrower) {
         CASTERS.remove(this.getThrower().getUUID());
      }

      if (!this.collectedDrops.isEmpty()) {
         if (!this.level().isClientSide) {
            if (hasThrower) {
               Inventory inv = this.getThrower().getInventory();
               this.collectedDrops.stream().filter(it -> !it.isEmpty()).forEach(inv::placeItemBackInInventory);
            } else {
               this.collectedDrops
                  .forEach(
                     it -> {
                        if (it != null && !it.isEmpty()) {
                           while (it.getCount() > 0) {
                              ItemStack stack = it.split(it.getMaxStackSize());
                              ItemEntity item = new ItemEntity(
                                 this.level(), this.getX(), this.getY() + this.getDimensions(Pose.STANDING).height(), this.getZ(), stack
                              );
                              item.setDefaultPickUpDelay();
                              item.setDeltaMovement(Vec3.ZERO);
                              this.level().addFreshEntity(item);
                           }
                        }
                     }
                  );
            }
         }

         this.collectedDrops.clear();
      }
   }

   public PlayerTeam getTeam() {
      return this.hasThrower() ? this.getThrower().getTeam() : super.getTeam();
   }

   public List<ItemStack> getCollectedDrops() {
      return this.collectedDrops;
   }

   public void setCollectedDrops(List<ItemStack> drops) {
      this.collectedDrops = new ArrayList<>(drops);
      this.numCollectedDrops = this.collectedDrops.stream().mapToInt(ItemStack::getCount).sum();
   }

   public static boolean isCasting(Player player, InteractionHand hand) {
      YoyoEntity yoyo = CASTERS.get(player.getUUID());
      return yoyo != null && !yoyo.isRemoved() && yoyo.getHand() == hand;
   }

   public boolean doesBlockInteraction() {
      return this.doesBlockInteraction;
   }

   public void setInteractsWithBlocks(boolean blockInteraction) {
      this.doesBlockInteraction = blockInteraction;
   }

   public boolean hasThrower() {
      return this.isThrowerInitialized;
   }

   protected void setThrower(Player thrower) {
      this.thrower = thrower;
      this.isThrowerInitialized = true;
      this.entityData.set(THROWER_ID, thrower.getId());
      CASTERS.put(thrower.getUUID(), this);
   }

   private void resolveThrower() {
      if (!this.isThrowerInitialized && this.entityData.get(THROWER_ID) != 0
         && this.level().getEntity(this.entityData.get(THROWER_ID)) instanceof Player player) {
         this.setThrower(player);
      }
   }

   public Player getThrower() {
      if (!this.isThrowerInitialized) {
         throw new IllegalStateException("Thrower is not initialized");
      } else {
         return this.thrower;
      }
   }

   public boolean hasYoyo() {
      return this.isYoyoInitialized;
   }

   protected void setYoyo(IYoyo yoyo) {
      this.yoyo = yoyo;
      this.isYoyoInitialized = true;
   }

   public IYoyo getYoyo() {
      if (!this.isYoyoInitialized) {
         throw new IllegalStateException("Yoyo is not initialized");
      } else {
         return this.yoyo;
      }
   }

   public ItemStack getYoyoStack() {
      return (ItemStack)this.entityData.get(YOYO_STACK);
   }

   public void setYoyoStack(ItemStack stack) {
      this.entityData.set(YOYO_STACK, stack);
   }

   public InteractionHand getHand() {
      return InteractionHand.values()[this.entityData.get(HAND)];
   }

   public void setHand(InteractionHand hand) {
      this.entityData.set(HAND, (byte)hand.ordinal());
   }

   public boolean isRetracting() {
      return (Boolean)this.entityData.get(RETRACTING);
   }

   public void setRetracting(boolean retracting) {
      if (this.canCancelRetract || !this.isRetracting()) {
         this.entityData.set(RETRACTING, retracting);
      }
   }

   public int getMaxTime() {
      return (Integer)this.entityData.get(MAX_TIME);
   }

   public void setMaxTime(int duration) {
      this.entityData.set(MAX_TIME, duration);
   }

   public int getRemainingTime() {
      return (Integer)this.entityData.get(REMAINING_TIME);
   }

   public void setRemainingTime(int duration) {
      this.entityData.set(REMAINING_TIME, duration);
   }

   public float getWeight() {
      return (Float)this.entityData.get(WEIGHT);
   }

   public void setWeight(float weight) {
      this.entityData.set(WEIGHT, weight);
   }

   public float getCurrentLength() {
      return (Float)this.entityData.get(CURRENT_LENGTH);
   }

   public void setCurrentLength(float length) {
      this.entityData.set(CURRENT_LENGTH, length);
   }

   public float getMaxLength() {
      return (Float)this.entityData.get(MAX_LENGTH);
   }

   public void setMaxLength(float length) {
      this.entityData.set(MAX_LENGTH, length);
   }

   public int getMaxCollectedDrops() {
      return (Integer)this.entityData.get(MAX_COLLECTED_DROPS);
   }

   public void setMaxCollectedDrops(int drops) {
      this.entityData.set(MAX_COLLECTED_DROPS, drops);
   }

   public boolean isCollecting() {
      return YoyoItem.isEnchantmentEnable(this.getYoyoStack(), YoyoEnchantments.COLLECTING, this.registryAccess()) && this.getMaxCollectedDrops() > 0;
   }

   public float getThrowerEyeHeight() {
      return this.thrower.getEyeHeight(this.thrower.getPose());
   }
}
