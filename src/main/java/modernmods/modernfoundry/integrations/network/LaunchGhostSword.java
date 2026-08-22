package modernmods.modernfoundry.integrations.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.hilt.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.integrations.items.TciModifiers;
import modernmods.modernfoundry.integrations.util.IfdWorkaroundHelper;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

/** Validated client request for the Ice and Fire ghost-sword hook. */
public final class LaunchGhostSword implements IThreadsafePacket {
  public LaunchGhostSword() {}
  public LaunchGhostSword(FriendlyByteBuf buffer) {}
  @Override public void encode(FriendlyByteBuf buffer) {}

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    if (!(context.player() instanceof ServerPlayer player) || !IfdWorkaroundHelper.isLoaded()) return;
    ItemStack stack = player.getMainHandItem();
    if (player.getCooldowns().isOnCooldown(stack.getItem())) return;
    for (ModifierEntry entry : ToolStack.from(stack).getModifierList()) {
      if (TciModifiers.PHANTASMAL_MODIFIER != null && entry.getId().equals(TciModifiers.PHANTASMAL_MODIFIER.getId())) {
        entry.getHook(ModifierHooks.PROJECTILE_LAUNCH).onProjectileLaunch(ToolStack.from(stack), entry, player, null, null, null, false);
      }
    }
  }
}
