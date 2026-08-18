package modernmods.modernfoundry.compat.neoforged.neoforge.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Compatibility facade for old Forge client hook names. */
public final class ForgeHooksClient {
  private ForgeHooksClient() {}

  // 26.1: handleCameraTransforms + BakedModel removed by the ItemStackRenderState render rewrite; the old
  // camera-transform hook has no equivalent and had no callers, so it is dropped.

  // 26.1: ClientHooks.getArmorModel removed by the armor/equipment render rewrite; had no callers, dropped.

  public static String getArmorTexture(Entity entity, ItemStack stack, String defaultTexture, EquipmentSlot slot, String type) {
    return defaultTexture;
  }
}
