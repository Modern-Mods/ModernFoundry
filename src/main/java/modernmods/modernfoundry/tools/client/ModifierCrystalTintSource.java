package modernmods.modernfoundry.tools.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.client.ResourceColorManager;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.utils.Util;
import modernmods.modernfoundry.tools.item.ModifierCrystalItem;

import javax.annotation.Nullable;

/**
 * Item tint source that colors a modifier crystal by its stored modifier's color.
 * <p>
 * In 26.1 the runtime {@code ItemColors}/{@code RegisterColorHandlersEvent.Item} system was removed; dynamic per-stack
 * item tints are now data-driven {@link ItemTintSource}s registered on {@code RegisterColorHandlersEvent.ItemTintSources}
 * and referenced from the item model's {@code tints} array. Without this, every modifier crystal rendered the same
 * (untinted) color.
 */
public record ModifierCrystalTintSource() implements ItemTintSource {
  public static final ModifierCrystalTintSource INSTANCE = new ModifierCrystalTintSource();
  public static final MapCodec<ModifierCrystalTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);
  /** Registered id, referenced as {@code "type": "modernfoundry:modifier_crystal"} in the item model tints */
  public static final Identifier ID = TConstruct.getResource("modifier_crystal");

  @Override
  public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
    ModifierId modifier = ModifierCrystalItem.getModifier(stack);
    if (modifier != null) {
      return ARGB.opaque(ResourceColorManager.getColor(Util.makeTranslationKey("modifier", modifier.getIdentifier())));
    }
    return -1;
  }

  @Override
  public MapCodec<? extends ItemTintSource> type() {
    return MAP_CODEC;
  }
}
