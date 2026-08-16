package modernmods.modernfoundry.tools.stats;

import net.minecraft.network.chat.Component;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.materials.stats.IRepairableMaterialStats;
import modernmods.modernfoundry.library.materials.stats.MaterialStatType;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.tools.stat.ModifierStatsBuilder;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;

/**
 * Stat type for slime on armor. Intended to be used for all 4 pieces using the factor. See {@link modernmods.modernfoundry.tools.modules.ArmorModuleBuilder#MAX_DAMAGE_ARRAY} for factors.
 * @param durability
 * @param overslime
 */
public record SlimeStats(int durability, int overslime) implements IRepairableMaterialStats.ScaledTooltip {
  public static final MaterialStatsId ID = new MaterialStatsId(TConstruct.getResource("slime"));
  public static final MaterialStatType<SlimeStats> TYPE = new MaterialStatType<>(ID, new SlimeStats(1, 0), RecordLoadable.create(
    IRepairableMaterialStats.DURABILITY_FIELD,
    IntLoadable.FROM_ZERO.requiredField("overslime_capacity", SlimeStats::overslime),
    SlimeStats::new));
  private static final List<Component> DESCRIPTION = List.of(
    ToolStats.DURABILITY.getDescription(),
    OverslimeModule.OVERSLIME_STAT.getDescription());

  @Override
  public MaterialStatType<?> getType() {
    return TYPE;
  }

  @Override
  public void apply(ModifierStatsBuilder builder, float scale) {
    ToolStats.DURABILITY.update(builder, durability * scale); // TODO: may want to rename this to durability factor
    OverslimeModule.OVERSLIME_STAT.add(builder, durability * scale);
  }

  @Override
  public List<Component> getLocalizedInfo(float scale) {
    return List.of(
      ToolStats.DURABILITY.formatValue(durability * scale),
      OverslimeModule.OVERSLIME_STAT.formatValue(overslime * scale)
    );
  }

  @Override
  public List<Component> getLocalizedDescriptions() {
    return DESCRIPTION;
  }
}
