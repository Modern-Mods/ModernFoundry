package modernmods.modernfoundry.tools.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.common.config.Config;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

/** Rapier-family leap; rapier variant also performs the reference sting attack. */
public class RapierItem extends ModifiableSwordItem {
  private final boolean sting;

  public RapierItem(Properties properties, ToolDefinition definition, boolean sting) {
    super(properties, definition);
    this.sting = sting;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    super.use(level, player, hand);

    ItemStack stack = player.getItemInHand(hand);
    boolean airborne = !player.onGround();
    float strength = airborne ? 0.25F : 0.5F;
    float x = Mth.sin(player.getYRot() * Mth.DEG_TO_RAD) * Mth.cos(player.getXRot() * Mth.DEG_TO_RAD) * strength;
    float z = -Mth.cos(player.getYRot() * Mth.DEG_TO_RAD) * Mth.cos(player.getXRot() * Mth.DEG_TO_RAD) * strength;
    player.setDeltaMovement(x, airborne ? 0.2F : 0.1F, z);
    player.hasImpulse = true;
    if (!level.isClientSide) {
      if (airborne) {
        player.causeFoodExhaustion(0.1F);
      }
      player.getCooldowns().addCooldown(this, 5);
    }

    InteractionResult interaction = hand == InteractionHand.MAIN_HAND && !player.getOffhandItem().isEmpty()
      ? InteractionResult.PASS : InteractionResult.SUCCESS;
    return new InteractionResultHolder<>(interaction, stack);
  }

  @Override
  public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
    if (sting && !player.level().isClientSide && entity instanceof LivingEntity target
        && !target.hasItemInSlot(EquipmentSlot.CHEST)) {
      float baseDamage = ToolAttackUtil.getAttributeAttackDamage(ToolStack.from(stack), player, EquipmentSlot.MAINHAND);
      float strength = player.getAttackStrengthScale(0.5F);
      float damage = (baseDamage * 0.75F) * (0.2F + strength * strength * 0.8F) + baseDamage * strength;
      damage *= Config.COMMON.rapierAttackBonus.get().floatValue() * 0.8F;
      boolean hit = target.hurt(player.damageSources().playerAttack(player), damage);
      if (hit) {
        target.invulnerableTime = 0;
        target.hurtTime = 0;
        target.level().playSound(null, target, SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
          target.getSoundSource(), Math.max(1.0F, damage),
          (target.getRandom().nextFloat() - target.getRandom().nextFloat()) * 0.5F + 1.0F);
      }
    }
    return super.onLeftClickEntity(stack, player, entity);
  }
}
