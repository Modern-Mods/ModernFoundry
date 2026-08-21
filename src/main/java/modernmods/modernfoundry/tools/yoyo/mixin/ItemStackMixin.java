package modernmods.modernfoundry.tools.yoyo.mixin;

import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import modernmods.modernfoundry.tools.yoyo.YoyoEnchantments;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
   @Shadow
   public abstract Item getItem();

   @Inject(
      method = "addToTooltip",
      remap = false,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/item/component/TooltipProvider;addToTooltip(Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
         shift = Shift.BEFORE
      ),
      locals = LocalCapture.CAPTURE_FAILSOFT,
      cancellable = true
   )
   private <T extends TooltipProvider> void yoyos$addYoyosEnchantments(
      DataComponentType<T> type, TooltipContext ctx, Consumer<Component> p_331885_, TooltipFlag p_331177_, CallbackInfo ci, TooltipProvider t
   ) {
      if (type.equals(DataComponents.ENCHANTMENTS) && this.getItem() instanceof YoyoItem) {
         Mutable enchantments = new Mutable((ItemEnchantments)t);
         enchantments.removeIf(h -> Objects.equals(h.getKey(), YoyoEnchantments.COLLECTING));
         enchantments.removeIf(h -> Objects.equals(h.getKey(), YoyoEnchantments.BREAKING));
         enchantments.removeIf(h -> Objects.equals(h.getKey(), YoyoEnchantments.CRAFTING));
         enchantments.toImmutable().addToTooltip(ctx, p_331885_, p_331177_);
         ci.cancel();
      }
   }
}

