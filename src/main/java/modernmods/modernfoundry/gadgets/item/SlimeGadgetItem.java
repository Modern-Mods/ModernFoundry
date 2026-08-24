package modernmods.modernfoundry.gadgets.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.compat.minecraft.world.item.DyeableLeatherItem;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.library.utils.SlimeBounceHandler;
import modernmods.modernfoundry.tools.network.EntityMovementChangePacket;

import java.util.List;
import java.util.Map;

/** Native owners for the 1.12 slime boots and slime sling. */
public final class SlimeGadgetItem {
  private static final Holder<ArmorMaterial> BOOTS_MATERIAL = Holder.direct(new ArmorMaterial(
    Map.of(), 0, Holder.direct(SoundEvents.SLIME_BLOCK_PLACE), () -> Ingredient.EMPTY,
    List.of(
      new ArmorMaterial.Layer(TConstruct.getResource("slime_gadget"), "", true),
      new ArmorMaterial.Layer(TConstruct.getResource("slime_gadget"), "_overlay", false)), 0, 0));

  private SlimeGadgetItem() {}

  private static Component name(String item, ItemStack stack) {
    return Component.translatable("item.modernfoundry." + item + "." + SlimeGadgetDataComponents.getTypeName(stack));
  }

  private static void tooltip(String item, List<Component> tooltip) {
    tooltip.add(Component.translatable("item.modernfoundry." + item + ".tooltip"));
  }

  public static final class Boots extends ArmorItem implements DyeableLeatherItem {
    public Boots(Properties properties) {
      super(BOOTS_MATERIAL, Type.BOOTS, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
      return 1;
    }

    @Override
    public Component getName(ItemStack stack) {
      return name("slime_gadget_boots", stack);
    }

    @Override
    public int getColor(ItemStack stack) {
      return SlimeGadgetDataComponents.getColor(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip("slime_gadget_boots", tooltip);
      super.appendHoverText(stack, context, tooltip, flag);
    }

  }

  public static final class Sling extends Item {
    public Sling(Properties properties) {
      super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
      return name("slimesling", stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      player.startUsingItem(hand);
      return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
      if (!(entity instanceof Player player) || !player.onGround()) {
        return;
      }

      HitResult hit = level.clip(new ClipContext(
        player.getEyePosition(), player.getEyePosition().add(player.getLookAngle().scale(5.0)),
        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
      if (hit.getType() != HitResult.Type.BLOCK) {
        return;
      }

      int chargeTicks = getUseDuration(stack, player) - timeLeft;
      float charge = chargeTicks / 20.0F;
      charge = (charge * charge + charge * 2.0F) / 3.0F * 4.0F;
      charge = Math.min(charge, 6.0F);
      if (charge <= 0) {
        return;
      }

      Vec3 look = player.getLookAngle().normalize();
      player.push(-look.x * charge, -look.y * charge / 3.0F, -look.z * charge);
      player.hasImpulse = true;
      SlimeBounceHandler.addBounceHandler(player);

      if (!level.isClientSide) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), Sounds.SLIME_SLING.getSound(), player.getSoundSource(), 1.0F, 1.0F);
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
          TinkerNetwork.getInstance().sendTo(new EntityMovementChangePacket(player), serverPlayer);
        }
      }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip("slimesling", tooltip);
      super.appendHoverText(stack, context, tooltip, flag);
    }
  }
}
