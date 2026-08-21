package modernmods.modernfoundry.tools.yoyo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {
  @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
  private void modernfoundry$hideThrownYoyo(LivingEntity living, ItemStack stack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo callback) {
    InteractionHand hand = living.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    if (living instanceof Player player && YoyoEntity.isCasting(player, hand)) callback.cancel();
  }
}
