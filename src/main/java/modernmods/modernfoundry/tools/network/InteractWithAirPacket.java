package modernmods.modernfoundry.tools.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.tools.logic.InteractionHandler;

/** Packet sent client to server when an empty hand interaction */
public enum InteractWithAirPacket implements IThreadsafePacket {
  /** Right click with an empty main hand and a chestplate */
  MAINHAND(InteractionHand.MAIN_HAND),
  /** Right click with an empty off hand and a chestplate */
  OFFHAND(InteractionHand.OFF_HAND),
  /** Left click with a supported tool */
  LEFT_CLICK(InteractionHand.MAIN_HAND);

  private final InteractionHand hand;

  InteractWithAirPacket(InteractionHand hand) {
    this.hand = hand;
  }

  /** Gets the packet for the given hand */
  public static InteractWithAirPacket fromChestplate(InteractionHand hand) {
    return hand == InteractionHand.OFF_HAND ? OFFHAND : MAINHAND;
  }

  /** Gets the packet from the packet buffer */
  public static InteractWithAirPacket read(FriendlyByteBuf buffer) {
    return buffer.readEnum(InteractWithAirPacket.class);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(this);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ServerPlayer player = (context.player() instanceof net.minecraft.server.level.ServerPlayer sp ? sp : null);
    if (player != null && !player.isSpectator()) {
      if (this == LEFT_CLICK) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(TinkerTags.Items.INTERACTABLE_LEFT)) {
          InteractionResult result = InteractionHandler.onLeftClickInteraction(player, held, hand);
          if (result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE) {
            player.swing(hand, true);
          }
        }
      } else {
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.is(TinkerTags.Items.INTERACTABLE_ARMOR) && player.getItemInHand(hand).isEmpty()) {
          InteractionResult result = InteractionHandler.onChestplateUse(player, chestplate, hand);
          if (result instanceof InteractionResult.Success success && success.swingSource() != InteractionResult.SwingSource.NONE) {
            player.swing(hand, true);
          }
        }
      }
    }
  }
}
