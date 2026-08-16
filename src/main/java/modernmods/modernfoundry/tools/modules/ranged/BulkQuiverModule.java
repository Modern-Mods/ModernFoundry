package modernmods.modernfoundry.tools.modules.ranged;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.primitive.BooleanLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.BowAmmoModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability.StackMatch;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.function.Predicate;

/**
 * Module implementing bulk quiver, which pulls arrows from the inventory to fire.
 * TODO 1.21: move to {@link modernmods.modernfoundry.tools.modules.ranged.bow}
 */
public record BulkQuiverModule(boolean checkStandardArrows) implements ModifierModule, BowAmmoModifierHook {
  public static final RecordLoadable<BulkQuiverModule> LOADER = RecordLoadable.create(
    BooleanLoadable.INSTANCE.defaultField("check_standard_arrows", true, BulkQuiverModule::checkStandardArrows),
    BulkQuiverModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<BulkQuiverModule>defaultHooks(ModifierHooks.BOW_AMMO);
  private static final ResourceLocation LAST_SLOT = TConstruct.getResource("quiver_last_selected");
  /** @deprecated use {@link #BulkQuiverModule(boolean)} */
  @Deprecated(forRemoval = true)
  public static final BulkQuiverModule INSTANCE = new BulkQuiverModule(true);

  @Override
  public RecordLoadable<BulkQuiverModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public ItemStack findAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack standardAmmo, Predicate<ItemStack> ammoPredicate) {
    // skip if we have standard ammo, this quiver holds backup arrows
    if (checkStandardArrows && !standardAmmo.isEmpty()) {
      return ItemStack.EMPTY;
    }
    StackMatch match = modifier.getHook(ToolInventoryCapability.HOOK).findStack(tool, modifier, ammoPredicate);
    if (!match.isEmpty()) {
      tool.getPersistentData().putInt(LAST_SLOT, match.slot());
      return match.stack();
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void shrinkAmmo(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, ItemStack ammo, int needed) {
    // we assume no one else touched the quiver inventory, this is a good assumption, do not make it a bad assumption by modifying the quiver in other modifiers
    ammo.shrink(needed);
    modifier.getHook(ToolInventoryCapability.HOOK).setStack(tool, modifier, tool.getPersistentData().getInt(LAST_SLOT), ammo);
  }
}
