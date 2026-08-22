package modernmods.modernfoundry.thinking.common.register;

import modernmods.modernfoundry.thinking.common.things.entity.SeekingArrow;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities extends ModModule {
    public static final DeferredHolder<EntityType<?>, EntityType<SeekingArrow>> Seeking_Arrow = ENTITIES.register("seeking_arrow", () -> EntityType.Builder.<SeekingArrow>of(SeekingArrow::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(2));
}
