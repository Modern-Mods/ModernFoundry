package modernmods.modernfoundry.library.json.predicate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.predicate.IJsonPredicate;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;

/**
 * Predicate that checks if an entity has the given mob effect.
 * @deprecated use {@link modernmods.mantle.data.predicate.entity.HasMobEffectPredicate}
 */
@Deprecated
public record HasMobEffectPredicate(MobEffect effect) implements LivingEntityPredicate {
  public static final RecordLoadable<HasMobEffectPredicate> LOADER = RecordLoadable.create(Loadables.MOB_EFFECT.requiredField("effect", HasMobEffectPredicate::effect), HasMobEffectPredicate::new);

  @Override
  public boolean matches(LivingEntity living) {
    return living.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
  }

  @Override
  public RecordLoadable<? extends IJsonPredicate<LivingEntity>> getLoader() {
    return LOADER;
  }
}
