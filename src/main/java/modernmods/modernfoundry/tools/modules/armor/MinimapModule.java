package modernmods.modernfoundry.tools.modules.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import modernmods.mantle.client.TooltipKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.mantle.util.LogicHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.KeybindInteractModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.modules.InventorySelectionModule;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing the minimap module. Should be paired with an {@link modernmods.modernfoundry.library.tools.capability.inventory.InventoryModule} */
public enum MinimapModule implements ModifierModule, EquipmentChangeModifierHook, KeybindInteractModifierHook, InventoryTickModifierHook, ModifierRemovalHook, InventorySelectionModule {
  INSTANCE;
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MinimapModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE, ModifierHooks.ARMOR_INTERACT, ModifierHooks.INVENTORY_TICK, ModifierHooks.REMOVE);
  public static final RecordLoadable<MinimapModule> LOADER = new SingletonLoader<>(INSTANCE);
  /** Data key for the active map on the player */
  public static final TinkerDataKey<ItemStack> MAP = TConstruct.createKey("current_map");
  /** Key for the currently selected map */
  private static final Identifier SELECTED_SLOT = TConstruct.getResource("minimap_selected");
  /** Message when disabling the minimap */
  private static final Component DISABLED = TConstruct.makeTranslation("modifier", "minimap.disabled");
  /** Message to display selected slot */
  private static final String SELECTED = TConstruct.makeTranslationKey("modifier", "minimap.selected");

  @Override
  public RecordLoadable<MinimapModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Integer getPriority() {
    return 30; // after slurping, before zoom
  }

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot() == EquipmentSlot.HEAD) {
      TinkerDataCapability.Holder data = LogicHelper.orElseNull(context.getTinkerData().resolve());
      if (data != null) {
        // set the map to the selected one
        ItemStack map = modifier.getHook(ToolInventoryCapability.HOOK).getStack(tool, modifier, tool.getPersistentData().getInt(SELECTED_SLOT));
        if (!map.isEmpty()) {
          data.put(MAP, map);
        } else {
          data.remove(MAP);
        }
      }
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot() == EquipmentSlot.HEAD) {
      TinkerDataCapability.Holder data = LogicHelper.orElseNull(context.getTinkerData().resolve());
      if (data != null) {
        data.remove(MAP);
      }
    }
  }

  @Override
  public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
    if (isCorrectSlot && !world.isClientSide()) {
      TinkerDataCapability.Holder data = TinkerDataCapability.getData(holder);
      if (data != null) {
        ItemStack map = data.get(MAP, ItemStack.EMPTY);
        if (!map.isEmpty()) {
          // goal: we want to tick the map so it continues to explore. We could copy code from MapItem, but this gives us compatability with custom maps
          // hack: map logic requires the map to be in the selected slot to tick properly, so put it in the invnetory temporarily
          ItemStack held = holder.getOffhandItem();
          holder.setItemInHand(InteractionHand.OFF_HAND, map);
          map.inventoryTick(world, holder, net.minecraft.world.entity.EquipmentSlot.OFFHAND);
          holder.setItemInHand(InteractionHand.OFF_HAND, held);
          if (holder instanceof ServerPlayer player) {
            MapItemSavedData mapData = MapItem.getSavedData(map, world);
            MapId id = map.get(DataComponents.MAP_ID);
            if (mapData != null && id != null) {
              Packet<?> packet = mapData.getUpdatePacket(id, player);
              if (packet != null) {
                player.connection.send(packet);
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void onDisableSelection(IToolStackView tool, ModifierEntry modifier, Player player) {
    player.sendOverlayMessage(DISABLED);
  }

  @Override
  public void onInventorySelect(IToolStackView tool, ModifierEntry modifier, Player player, int newIndex, ItemStack stack) {
    MapId id = stack.get(DataComponents.MAP_ID);
    player.sendOverlayMessage(Component.translatable(SELECTED, stack.getHoverName(), id == null ? -1 : id.id(), newIndex + 1));
  }

  @Override
  public boolean startInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot, TooltipKey keyModifier) {
    if (keyModifier == TooltipKey.NORMAL || keyModifier == TooltipKey.CONTROL) {
      return selectNext(tool, modifier, player, SELECTED_SLOT);
    }
    return false;
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    tool.getPersistentData().remove(SELECTED_SLOT);
    return null;
  }
}
