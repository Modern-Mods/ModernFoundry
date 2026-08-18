package modernmods.modernfoundry.tools.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.entity.ReusableProjectile;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ScheduledProjectileTaskModifierHook;
import modernmods.modernfoundry.library.tools.IndestructibleItemEntity;
import modernmods.modernfoundry.library.tools.capability.EntityModifierCapability;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.library.utils.Schedule;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.tools.TinkerTools;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/** Arrow with material variants */
public class ModifiableArrow extends AbstractArrow implements ToolProjectile, ReusableProjectile {
  /** Key to sync the stack to the client */
  protected static final EntityDataAccessor<ItemStack> STACK = SynchedEntityData.defineId(ModifiableArrow.class, EntityDataSerializers.ITEM_STACK);
  /** Movement speed in water */
  protected static final EntityDataAccessor<Float> WATER_INERTIA = SynchedEntityData.defineId(ModifiableArrow.class, EntityDataSerializers.FLOAT);

  private ItemStack stack = ItemStack.EMPTY;
  private IToolStackView tool = null;
  private int knockback = 0;
  private boolean reclaim = false;
  private boolean dealtDamage = false;
  /** Tasks queued by modifiers */
  private Schedule tasks = Schedule.EMPTY;

  public ModifiableArrow(EntityType<? extends AbstractArrow> type, Level level) {
    super(type, level);
  }

  public ModifiableArrow(Level level, double pX, double pY, double pZ) {
    super(TinkerTools.materialArrow.get(), pX, pY, pZ, level, ItemStack.EMPTY, null);
  }

  public ModifiableArrow(Level level, LivingEntity shooter) {
    super(TinkerTools.materialArrow.get(), shooter, level, ItemStack.EMPTY, null);
  }


  /* Stack */

  @Override
  public ItemStack getPickupItem() {
    return stack.copy();
  }

  @Override
  protected ItemStack getDefaultPickupItem() {
    return ItemStack.EMPTY;
  }

  /** Updates the stack on the arrow */
  private void setStack(ItemStack stack) {
    this.stack = stack;
    this.entityData.set(STACK, stack);
    this.reclaim = ModifierUtil.checkVolatileFlag(stack, IndestructibleItemEntity.INDESTRUCTIBLE_ENTITY);
  }

  /** Gets the tool instance, ensuring its created */
  private IToolStackView getTool() {
    if (tool == null) {
      tool = ToolStack.from(stack);
    }
    return tool;
  }

  /**
   * Called when the arrow is created to set initial properties.
   * @see ThrownShuriken#onCreate(ItemStack, LivingEntity)
   */
  public IToolStackView onCreate(ItemStack stack, @Nullable LivingEntity shooter) {
    stack = stack.copyWithCount(1);
    setStack(stack);
    // initialize arrow stats
    IToolStackView tool = getTool();
    EntityModifierCapability.getCapability(this).addModifiers(tool.getModifiers());
    setBaseDamage(ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.PROJECTILE_DAMAGE));
    this.entityData.set(WATER_INERTIA, ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.WATER_INERTIA));
    return tool;
  }

  /** @see ThrownShuriken#shoot(double, double, double, float, float)  */
  @Override
  public void shoot(double pX, double pY, double pZ, float velocity, float inaccuracy) {
    if (!stack.isEmpty()) {
      IToolStackView tool = getTool();
      // apply accuracy, no need to compute this earlier nor store it
      LivingEntity shooter = ModifierUtil.asLiving(getOwner());
      velocity *= ConditionalStatModifierHook.getModifiedStat(tool, shooter, ToolStats.VELOCITY);
      inaccuracy *= ModifierUtil.getInaccuracy(tool, shooter);

      // shoot with new information
      super.shoot(pX, pY, pZ, velocity, inaccuracy);

      // run modifier hooks from the arrow's perspective
      ModDataNBT arrowData = PersistentDataCapability.getOrWarn(this);
      for (ModifierEntry entry : tool.getModifiers()) {
        entry.getHook(ModifierHooks.PROJECTILE_SHOT).onProjectileShoot(tool, entry, shooter, stack, this, this, arrowData, true);
      }

      // schedule tasks
      this.tasks = ScheduledProjectileTaskModifierHook.createSchedule(tool, stack, this, this, arrowData);
    } else {
      super.shoot(pX, pY, pZ, velocity, inaccuracy);
    }
  }

  @Override
  public void tick() {
    super.tick();
    // check if any tasks are ready
    if (!tasks.isEmpty() && !stack.isEmpty()) {
      ScheduledProjectileTaskModifierHook.checkSchedule(getTool(), stack, this, this, tasks);
    }
  }

  /* Stats */

  @Override
  protected float getWaterInertia() {
    return entityData.get(WATER_INERTIA);
  }

  // need to replace some setters with adders so vanilla bows work with our logic

  public int getKnockback() {
    return knockback;
  }

  public void setKnockback(int knockback) {
    this.knockback += knockback;
  }

  public void setPierceLevel(byte pierceLevel) {
    AbstractArrowAccess.setPierceLevel(this, (byte)(getPierceLevel() + pierceLevel));
  }


  /* Despawn */

  @Override
  public boolean isReusable() {
    return reclaim;
  }

  @Override
  public void tickDespawn() {
    // if we can pick up the arrows, don't despawn with worldbound
    if (pickup != Pickup.ALLOWED || !reclaim) {
      super.tickDespawn();
    }
  }

  private enum CaptureDiscard { NOT_CAPTURING,  CAPTURING,  DISCARDED }
  private CaptureDiscard captureDiscard = CaptureDiscard.NOT_CAPTURING;

  @Override
  protected void onHitEntity(EntityHitResult result) {
    if (reclaim) {
      // prevent the entity from being discarded for a bit
      captureDiscard = CaptureDiscard.CAPTURING;
    }

    super.onHitEntity(result);

    // if we tried to discard it, back off the movement and mark it to prevent further damage
    if (captureDiscard == CaptureDiscard.DISCARDED) {
      dealtDamage = true;
      setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));
    }
    captureDiscard = CaptureDiscard.NOT_CAPTURING;
  }

  @Override
  public void remove(RemovalReason reason) {
    // capturing is used for worldbound to keep the ammo around after hit
    // however, there is a single case where we don't want to stick around, and that is when we failed to hit a target and the movement is now too small
    if (reason == RemovalReason.DISCARDED && captureDiscard != CaptureDiscard.NOT_CAPTURING && getDeltaMovement().lengthSqr() >= 1.0E-7D) {
      captureDiscard = CaptureDiscard.DISCARDED;
    } else {
      super.remove(reason);
    }
  }

  @Override
  @Nullable
  protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
    return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
  }


  /* Client */

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(STACK, ItemStack.EMPTY);
    builder.define(WATER_INERTIA, 0.6f);
  }

  @Override
  public ItemStack getDisplayTool() {
    return this.entityData.get(STACK);
  }

  @Override
  public Component getDisplayName() {
    return getDisplayTool().getDisplayName();
  }


  /* NBT */
  private static final String KEY_STACK = "stack";
  private static final String KEY_WATER_INERTIA = "water_inertia";
  private static final String KEY_DEALT_DAMAGE = "dealt_damage";
  private static final String KEY_TASKS = "tasks";

  @Override
  public void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.store(KEY_STACK, CompoundTag.CODEC, TagUtil.saveItem(this.stack, new CompoundTag()));
    output.putFloat(KEY_WATER_INERTIA, this.entityData.get(WATER_INERTIA));
    output.putBoolean(KEY_DEALT_DAMAGE, dealtDamage);
    if (!this.tasks.isEmpty()) {
      // ValueOutput has no raw-tag put, so wrap the task list in a compound stored via its codec
      CompoundTag wrapper = new CompoundTag();
      wrapper.put(KEY_TASKS, this.tasks.serialize());
      output.store(KEY_TASKS, CompoundTag.CODEC, wrapper);
    }
  }

  @Override
  public void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    input.read(KEY_STACK, CompoundTag.CODEC).ifPresent(t -> setStack(TagUtil.readItem(t)));
    this.entityData.set(WATER_INERTIA, input.getFloatOr(KEY_WATER_INERTIA, 0f));
    this.dealtDamage = input.getBooleanOr(KEY_DEALT_DAMAGE, false);
    input.read(KEY_TASKS, CompoundTag.CODEC).ifPresent(wrapper -> {
      ListTag list = wrapper.getListOrEmpty(KEY_TASKS);
      if (!list.isEmpty()) {
        this.tasks = Schedule.deserialize(list);
      }
    });
  }

  private static class AbstractArrowAccess {
    private static final Method SET_PIERCE_LEVEL = findSetPierceLevel();

    private static Method findSetPierceLevel() {
      try {
        Method method = AbstractArrow.class.getDeclaredMethod("setPierceLevel", byte.class);
        method.setAccessible(true);
        return method;
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to access AbstractArrow#setPierceLevel", e);
      }
    }

    private static void setPierceLevel(AbstractArrow arrow, byte level) {
      try {
        SET_PIERCE_LEVEL.invoke(arrow, level);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to set arrow pierce level", e);
      }
    }
  }
}
