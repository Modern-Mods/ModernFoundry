package modernmods.modernfoundry.compat.minecraft.world.entity;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.predicate.entity.LivingEntityPredicate;

/** Compatibility enum for the removed vanilla MobType API. */
public enum MobType implements LivingEntityPredicate {
  UNDEFINED,
  UNDEAD,
  ARTHROPOD,
  ILLAGER,
  WATER;

  @Override
  public boolean matches(LivingEntity entity) {
    return switch (this) {
      case UNDEAD -> entity.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
      case ARTHROPOD -> entity.getType().builtInRegistryHolder().is(EntityTypeTags.ARTHROPOD);
      case ILLAGER -> entity.getType().builtInRegistryHolder().is(EntityTypeTags.ILLAGER);
      case WATER -> entity.getType().builtInRegistryHolder().is(EntityTypeTags.AQUATIC);
      case UNDEFINED -> !entity.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD) && !entity.getType().builtInRegistryHolder().is(EntityTypeTags.ARTHROPOD) && !entity.getType().builtInRegistryHolder().is(EntityTypeTags.ILLAGER) && !entity.getType().builtInRegistryHolder().is(EntityTypeTags.AQUATIC);
    };
  }

  @Override
  public modernmods.mantle.data.loadable.record.RecordLoadable<? extends LivingEntityPredicate> getLoader() {
    return modernmods.mantle.data.predicate.entity.MobTypePredicate.LOADER;
  }
}
