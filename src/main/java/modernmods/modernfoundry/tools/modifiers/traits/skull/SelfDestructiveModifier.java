package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import modernmods.mantle.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.EquipmentChangeModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.KeybindInteractModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;

public class SelfDestructiveModifier extends NoLevelsModifier implements KeybindInteractModifierHook, EquipmentChangeModifierHook {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    hookBuilder.addHook(this, ModifierHooks.ARMOR_INTERACT, ModifierHooks.EQUIPMENT_CHANGE);
  }

  @Override
  public boolean startInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot, TooltipKey keyModifier) {
    if (player.isShiftKeyDown()) {
      TinkerEffects.selfDestructing.get().apply(player, 30, 2, true);
      player.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
      return true;
    }
    return false;
  }

  @Override
  public void stopInteract(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot slot) {
    player.removeEffect(TinkerEffects.holder(TinkerEffects.selfDestructing));
  }

  @Override
  public void onUnequip(IToolStackView tool, ModifierEntry modifier, EquipmentChangeContext context) {
    context.getEntity().removeEffect(TinkerEffects.holder(TinkerEffects.selfDestructing));
  }

  /** Internal potion effect handling the explosion */
  public static class SelfDestructiveEffect extends NoMilkEffect {
    public SelfDestructiveEffect() {
      super(MobEffectCategory.HARMFUL, 0x59D24A, true);
      // make the player slow
      addAttributeModifier(Attributes.MOVEMENT_SPEED, "68ee3026-1d50-4eb4-914e-a8b05fbfdb71", -0.9f, Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
      return duration == 1;
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity living, int amplifier) {
      // effect level is the explosion radius
      Level level = living.level();
      if (!level.isClientSide()) {
        // TODO: is firing mob grief event with a player okay?
        level.explode(living, living.getX(), living.getY(), living.getZ(), amplifier + 1, ExplosionInteraction.MOB);
        living.hurt(TinkerDamageTypes.source(level.registryAccess(), TinkerDamageTypes.SELF_DESTRUCT), 99999);
      }
      return true;
    }
  }
}
