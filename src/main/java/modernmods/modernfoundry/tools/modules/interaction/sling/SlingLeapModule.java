package modernmods.modernfoundry.tools.modules.interaction.sling;

import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import modernmods.mantle.data.loadable.primitive.BooleanLoadable;
import modernmods.mantle.data.loadable.primitive.FloatLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierManager;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingAngleModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingForceModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.special.sling.SlingLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.SlimeBounceHandler;
import modernmods.modernfoundry.tools.TinkerToolActions;

/**
 * Module adding velocity to the player. Implements {@link modernmods.modernfoundry.tools.data.ModifierIds#flinging} and {@link modernmods.modernfoundry.tools.data.ModifierIds#flinging}.
 * @param forceMultiplier     Base force multiplier to apply.
 * @param drawtimeMultiplier  Multiplier on the drawspeed to apply.
 * @param heightReduction     Reduction on height of the force.
 * @param leaveGround         If true and the target is grounded, they will be forced into the air before moving
 * @param biasUpwards         If true, the look angle will be increased slightly upwards based on the force. Causes more of an arc instead of a straight line.
 * @param target              Conditions on the entity to use the sling
 */
public record SlingLeapModule(LevelingValue forceMultiplier, boolean leaveGround, float drawtimeMultiplier, float heightReduction, boolean biasUpwards, IJsonPredicate<LivingEntity> target, ModifierCondition<IToolStackView> condition) implements SlingModule {
  public static final RecordLoadable<SlingLeapModule> LOADER = RecordLoadable.create(
    FORCE_FIELD,
    BooleanLoadable.INSTANCE.requiredField("leave_ground", SlingLeapModule::leaveGround),
    DRAWTIME_FIELD,
    FloatLoadable.ANY.requiredField("height_reduction", SlingLeapModule::heightReduction),
    BooleanLoadable.INSTANCE.requiredField("bias_upwards", SlingLeapModule::biasUpwards),
    TARGET_FIELD, ModifierCondition.TOOL_FIELD, SlingLeapModule::new);

  @Override
  public RecordLoadable<SlingLeapModule> getLoader() {
    return LOADER;
  }

  @Override
  public void sling(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int chargeTime, ModifierEntry activeModifier) {
    Level level = entity.level();
    // must be sufficiently charged, not have too much knockback resistance, and not have any modifier zeroing its force
    float charge = GeneralInteractionModifierHook.getToolCharge(tool, chargeTime);
    if (charge > 0 && target.matches(entity)) {
      float forceMultiplier = this.forceMultiplier.compute(modifier);
      float multiplier = charge * Math.abs(forceMultiplier);
      float force = SlingForceModifierHook.modifySlingForce(tool, entity, entity, modifier, SlingModule.getPower(tool, entity) * multiplier, multiplier);
      if (force > 0) {
        Vec3 look = entity.getLookAngle();
        if (biasUpwards) {
          look = look.add(0, Math.max(0, 0.5 - force * 0.1 * Mth.sign(forceMultiplier)), 0);
        }
        look = look.normalize();

        RandomSource random = entity.getRandom();
        float inaccuracy = ModifierUtil.getInaccuracy(tool, entity) * 0.0075f;
        Vec3 angle = new Vec3(
          (look.x + random.nextGaussian() * inaccuracy),
          (look.y + random.nextGaussian() * inaccuracy) / heightReduction,
          (look.z + random.nextGaussian() * inaccuracy)
        );
        // fling in look direction, unless force is negative in which case reverse it
        if (forceMultiplier < 0) {
          angle = angle.multiply(-1, -1, -1);
        }
        angle = SlingAngleModifierHook.modifySlingAngle(tool, entity, entity, modifier, force, multiplier, angle);
        entity.push(force * angle.x, force * angle.y, force * angle.z);

        // if on the ground, get off the ground so jumping is not required before springing
        if (leaveGround && entity.onGround()) {
          entity.move(MoverType.SELF, new Vec3(0, 1.3f, 0));
        }

        // after sling callback
        SlimeBounceHandler.addBounceHandler(entity);
        SlingLaunchModifierHook.afterSlingLaunch(tool, entity, entity, modifier, force, multiplier, angle);

        if (!level.isClientSide()) {
          level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.SLIME_SLING.getSound(), entity.getSoundSource(), 1, 1);
          ToolDamageUtil.damageAnimated(tool, 1, entity, entity.getUsedItemHand(), modifier.getId());
        }
        // only need player for exhaustion, cooldowns, and drill attack
        if (entity instanceof Player player) {
          if (!level.isClientSide()) {
            player.causeFoodExhaustion(0.2F);
            player.getCooldowns().addCooldown(new ItemStack(tool.getItem()), 3);
          }
          // if supported, perform drill attack if the modifier is available
          if (ModifierManager.isInTag(modifier.getId(), TinkerTags.Modifiers.DRILL_ATTACKS) && ModifierUtil.canPerformAction(tool, TinkerToolActions.DRILL_ATTACK)) {
            player.startAutoSpinAttack(20, 8.0F, player.getMainHandItem());
          }
        }
        return;
      }
    }
    // play failure sound
    if (!level.isClientSide() && ModifierUtil.isActiveModifier(tool, modifier, activeModifier)) {
      level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), Sounds.SLIME_SLING.getSound(), entity.getSoundSource(), 1, 0.5f);
    }
  }
}
