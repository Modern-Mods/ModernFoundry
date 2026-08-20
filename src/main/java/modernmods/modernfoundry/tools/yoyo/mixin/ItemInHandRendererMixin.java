package modernmods.modernfoundry.tools.yoyo.mixin;

import modernmods.modernfoundry.tools.yoyo.YoyoTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
  @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
  private ItemStack modernfoundry$hideThrownYoyoMainHand(LocalPlayer player) {
    return modernfoundry$isThrownYoyo(player, InteractionHand.MAIN_HAND) ? ItemStack.EMPTY : player.getMainHandItem();
  }

  @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getOffhandItem()Lnet/minecraft/world/item/ItemStack;"))
  private ItemStack modernfoundry$hideThrownYoyoOffhand(LocalPlayer player) {
    return modernfoundry$isThrownYoyo(player, InteractionHand.OFF_HAND) ? ItemStack.EMPTY : player.getOffhandItem();
  }

  private static boolean modernfoundry$isThrownYoyo(LocalPlayer player, InteractionHand hand) {
    return YoyoTracker.on(player).hasYoyo(hand);
  }
}
