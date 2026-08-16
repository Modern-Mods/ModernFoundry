package modernmods.modernfoundry.tools.modules.armor;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.damage.DamageSourcePredicate;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.events.teleport.EnderdodgingTeleportEvent;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModuleBuilder;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.utils.TeleportHelper;
import modernmods.modernfoundry.shared.TinkerEffects;

import java.util.List;

/** Module for armor to randomly teleport the wearer to prevent damage. */
public record TeleportDodgeModule(IJsonPredicate<LivingEntity> defender, IJsonPredicate<DamageSource> damageSource, LevelingValue chance, LevelingInt cooldown, ModifierCondition<IToolStackView> condition) implements ModifierModule, ModifyDamageModifierHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<TeleportDodgeModule>defaultHooks(ModifierHooks.MODIFY_HURT);
  public static final RecordLoadable<TeleportDodgeModule> LOADER = RecordLoadable.create(
    LivingEntityPredicate.LOADER.defaultField("defender", TeleportDodgeModule::defender),
    DamageSourcePredicate.LOADER.defaultField("damage_source", TeleportDodgeModule::damageSource),
    LevelingValue.LOADABLE.requiredField("chance", TeleportDodgeModule::chance),
    LevelingInt.LOADABLE.requiredField("cooldown", TeleportDodgeModule::cooldown),
    ModifierCondition.TOOL_FIELD, TeleportDodgeModule::new);

  /** @apiNote use {@link #builder()} */
  @Internal
  public TeleportDodgeModule {}

  @Override
  public RecordLoadable<? extends ModifierModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    LivingEntity entity = context.getEntity();
    // entity must not have enderference, and conditions must match
    if (!entity.hasEffect(TinkerEffects.holder(TinkerEffects.enderference)) && this.defender.matches(entity) && damageSource.matches(source)) {
      // chance of applying is boosted when blocking with a shield
      float level = CounterModule.getLevel(tool, modifier, slotType, entity);
      if (entity.getRandom().nextFloat() < chance.compute(level) && TeleportHelper.randomNearbyTeleport(context.getEntity(), (e, x, y, z) -> new EnderdodgingTeleportEvent(e, x, y, z, modifier))) {
        // if we successfully teleport, apply the cooldown
        int cooldown = this.cooldown.compute(level);
        if (cooldown > 0) {
          entity.addEffect(new MobEffectInstance(TinkerEffects.holder(TinkerEffects.enderference), cooldown));
        }
        // damage tool based on how much damage we blocked
        ToolDamageUtil.damageAnimated(tool, (int)amount, entity, slotType, modifier.getId());
        // prevent said damage
        return 0;
      }
    }
    return amount;
  }


  /* Builder */

  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(fluent = true)
  public static class Builder extends ModuleBuilder.Stack<Builder> implements LevelingInt.Builder<TeleportDodgeModule> {
    private IJsonPredicate<LivingEntity> defender = LivingEntityPredicate.ANY;
    private IJsonPredicate<DamageSource> damageSource = DamageSourcePredicate.ANY;
    private LevelingValue chance = LevelingValue.ONE;

    private Builder() {}

    @Override
    public TeleportDodgeModule amount(int flat, int eachLevel) {
      return new TeleportDodgeModule(defender, damageSource, chance, new LevelingInt(flat, eachLevel), condition);
    }
  }
}
