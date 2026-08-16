package modernmods.modernfoundry.library.modifiers.modules.armor;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.world.entity.EntityType;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/**
 * Module for armor modifiers that makes this entity appear to be another entity from afar
 */
public record MobDisguiseModule(EntityType<?> entity) implements EquipmentChangeModifierHook, ModifierModule {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MobDisguiseModule>defaultHooks(ModifierHooks.EQUIPMENT_CHANGE);
  public static final RecordLoadable<MobDisguiseModule> LOADER = RecordLoadable.create(Loadables.ENTITY_TYPE.requiredField("entity", MobDisguiseModule::entity), MobDisguiseModule::new);

  /**
   * Data key for all disguises on an entity
   */
  public static final ComputableDataKey<Multiset<EntityType<?>>> DISGUISES = TConstruct.createKey("mob_disguise", HashMultiset::create);

  @Override
  public void onEquip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot().isArmor()) {
      context.getTinkerData().ifPresent(data -> data.computeIfAbsent(DISGUISES).add(entity, modifier.getLevel()));
    }
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    if (context.getChangedSlot().isArmor()) {
      context.getTinkerData().ifPresent(data -> {
        Multiset<EntityType<?>> disguises = data.get(DISGUISES);
        if (disguises != null) {
          disguises.remove(entity, modifier.getLevel());
        }
      });
    }
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<MobDisguiseModule> getLoader() {
    return LOADER;
  }
}
