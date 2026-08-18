package modernmods.modernfoundry.library.materials.stats;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.Logger;
import modernmods.mantle.data.loadable.Loadable;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.mantle.util.typed.TypedMapBuilder;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;
import modernmods.modernfoundry.library.utils.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateMaterialStatsPacket implements IThreadsafePacket {
  private static final Logger log = Util.getLogger("NetworkSync");

  protected final Map<MaterialId, Collection<IMaterialStats>> materialToStats;

  public UpdateMaterialStatsPacket(Map<MaterialId, Collection<IMaterialStats>> materialToStats) {
    this.materialToStats = materialToStats;
  }

  /** Gets the material to stats map */
  public Map<MaterialId, Collection<IMaterialStats>> getMaterialToStats() {
    return materialToStats;
  }

  public UpdateMaterialStatsPacket(FriendlyByteBuf buffer) {
    this(buffer, MaterialRegistry.getInstance().getStatTypeLoader());
  }

  public UpdateMaterialStatsPacket(FriendlyByteBuf buffer, Loadable<MaterialStatType<?>> statTypeLoader) {
    int materialCount = buffer.readInt();
    materialToStats = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readIdentifier());
      int statCount = buffer.readInt();
      List<IMaterialStats> statList = new ArrayList<>();
      for (int j = 0; j < statCount; j++) {
        try {
          MaterialStatType<?> statType = statTypeLoader.decode(buffer);
          statList.add(statType.getLoadable().decode(buffer, TypedMapBuilder.builder().put(MaterialStatType.CONTEXT_KEY, statType).build()));
        } catch (Exception e) {
          log.error("Could not deserialize stat. Are client and server in sync?", e);
        }
      }
      materialToStats.put(id, statList);
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(materialToStats.size());
    materialToStats.forEach((materialId, stats) -> {
      buffer.writeIdentifier(materialId.getIdentifier());
      buffer.writeInt(stats.size());
      stats.forEach(stat -> encodeStat(buffer, stat, stat.getType()));
    });
  }

  /**
   * Encodes a single material stat
   * @param buffer  Buffer instance
   * @param stat    Stat to encode
   */
  @SuppressWarnings("unchecked")
  private <T extends IMaterialStats> void encodeStat(FriendlyByteBuf buffer, IMaterialStats stat, MaterialStatType<T> type) {
    MaterialStatsId.PARSER.encode(buffer, type.getStatId());
    type.getLoadable().encode(buffer, (T) stat);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    MaterialRegistry.updateMaterialStatsFromServer(this);
  }
}
