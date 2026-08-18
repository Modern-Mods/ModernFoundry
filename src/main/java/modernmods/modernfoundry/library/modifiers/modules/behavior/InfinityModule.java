package modernmods.modernfoundry.library.modifiers.modules.behavior;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.loadable.common.ItemStackLoadable;
import modernmods.mantle.data.loadable.primitive.BooleanLoadable;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.primitive.StringLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.BowAmmoModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.utils.TagUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Module making the bow fire infinite ammo of the given type.
 * @param ammo                Item stack setting the ammo.
 * @param variantTag           If not empty, copies the modifier variant into this tag on the arrow item.
 * @param durabilityUsage      Amount of extra durability consumed when using this module.
 * @param checkStandardArrows  If true, won't fire infinite arrows if there are standard arrows.
 */
public record InfinityModule(ItemStack ammo, String variantTag, int durabilityUsage, boolean checkStandardArrows) implements ModifierModule, BowAmmoModifierHook, ModifierRemovalHook, ProjectileLaunchModifierHook.NoShooter {
  /** NBT marking the stack as infinity to set arrow pickup */
  private static final String INFINITY = "tic_infinity";
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<InfinityModule>defaultHooks(ModifierHooks.BOW_AMMO, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.REMOVE);
  public static final RecordLoadable<InfinityModule> LOADER = RecordLoadable.create(
    ItemStackLoadable.REQUIRED_ITEM_NBT.requiredField("ammo", InfinityModule::ammo),
    StringLoadable.DEFAULT.defaultField("variant_tag", "", InfinityModule::variantTag),
    IntLoadable.FROM_ZERO.requiredField("durability_usage", InfinityModule::durabilityUsage),
    BooleanLoadable.INSTANCE.defaultField("check_standard_arrows", true, InfinityModule::checkStandardArrows),
    InfinityModule::new
  );

  @Override
  public RecordLoadable<InfinityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public ItemStack findAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack standardAmmo, Predicate<ItemStack> ammoPredicate) {
    if (checkStandardArrows && !standardAmmo.isEmpty()) {
      return ItemStack.EMPTY;
    }
    // our available count is based on how many arrows we can create from the remaining durability, though round up to be nice
    int count = durabilityUsage <= 0 ? 64 : Math.min(64, (tool.getCurrentDurability() + durabilityUsage - 1) / durabilityUsage);
    ItemStack ammo = this.ammo.copyWithCount(count);
    CompoundTag tag = TagUtil.getOrCreateTag(ammo);
    // mark the arrow as infinity for the projectile launch hook
    tag.putBoolean(INFINITY, true);
    // if a variant is requested, set that on the stack
    if (!variantTag.isEmpty()) {
      String variant = tool.getPersistentData().getString(modifier.getId().getIdentifier());
      if (!variant.isEmpty()) {
        tag.putString(variantTag, variant);
      }
    }
    TagUtil.setTag(ammo, tag);
    return ammo;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    // for arrows fired by this module, set them to creative only pickup
    // not an issue if you have multiple types of infinity, they all agree on the goal here
    if (arrow != null && arrow.pickup != Pickup.CREATIVE_ONLY) {
      CompoundTag tag = TagUtil.getTag(ammo);
      if (tag != null && tag.getBooleanOr(INFINITY, false)) {
        arrow.pickup = Pickup.CREATIVE_ONLY;
      }
    }
  }

  @Override
  public void shrinkAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack ammo, int needed) {
    if (durabilityUsage > 0) {
      ToolDamageUtil.damageAnimated(tool, durabilityUsage * needed, shooter, shooter.getUsedItemHand(), modifier.getId());
    }
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    if (!variantTag.isEmpty()) {
      tool.getPersistentData().remove(modifier.getId().getIdentifier());
    }
    return null;
  }
}
