package modernmods.modernfoundry.compat.neoforged.neoforge.common.loot;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.loot.CanItemPerformAbility;

/** Datagen compatibility shim for the removed Forge loot condition builder. */
public final class CanToolPerformAction {
  private CanToolPerformAction() {}

  public static LootItemCondition.Builder canToolPerformAction(ItemAbility action) {
    // 26.1 replaces Forge's CanToolPerformAction with the neoforge:can_item_perform_ability loot condition. The prior
    // stub returned randomChance(1) (ALWAYS true), which would make leaves always drop themselves instead of saplings if
    // the loot tables were regenerated. Emit the real condition instead.
    return () -> new CanItemPerformAbility(action);
  }
}
