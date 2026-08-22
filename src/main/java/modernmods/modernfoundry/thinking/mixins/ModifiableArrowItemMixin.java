package modernmods.modernfoundry.thinking.mixins;

import modernmods.modernfoundry.thinking.common.things.entity.SeekingArrow;
import modernmods.modernfoundry.thinking.data.ModModifierIds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import modernmods.modernfoundry.library.tools.item.ModifiableArrowItem;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

@Mixin({ModifiableArrowItem.class})
public class ModifiableArrowItemMixin {
    @Inject(method = "createArrow", at = @At(value = "HEAD"), cancellable = true)
    public void createArrow(Level level, ItemStack stack, LivingEntity shooter, ItemStack weapon, CallbackInfoReturnable<AbstractArrow> cir) {
        if (ToolStack.from(stack).getModifierLevel(ModModifierIds.Seeking)>0) {
            SeekingArrow arrow = new SeekingArrow(level, shooter);
            arrow.onCreate(stack, shooter);
            cir.setReturnValue(arrow);
        }
    }
}
