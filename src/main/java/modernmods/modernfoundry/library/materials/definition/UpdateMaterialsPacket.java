package modernmods.modernfoundry.library.materials.definition;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.utils.GenericTagUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateMaterialsPacket implements IThreadsafePacket {
  private final Map<MaterialId,IMaterial> materials;
  private final Map<MaterialId,MaterialId> redirects;
  private final Map<TagKey<IMaterial>,List<IMaterial>> tags;

  public UpdateMaterialsPacket(Map<MaterialId,IMaterial> materials, Map<MaterialId,MaterialId> redirects, Map<TagKey<IMaterial>,List<IMaterial>> tags) {
    this.materials = materials;
    this.redirects = redirects;
    this.tags = tags;
  }

  public Map<MaterialId,IMaterial> getMaterials() {
    return materials;
  }

  public Map<MaterialId,MaterialId> getRedirects() {
    return redirects;
  }

  public Map<TagKey<IMaterial>,List<IMaterial>> getTags() {
    return tags;
  }

  public UpdateMaterialsPacket(FriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    ImmutableMap.Builder<MaterialId,IMaterial> materials = ImmutableMap.builder();

    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readIdentifier());
      int tier = buffer.readVarInt();
      int sortOrder = buffer.readVarInt();
      boolean craftable = buffer.readBoolean();
      boolean hidden = buffer.readBoolean();
      materials.put(id, new Material(id.getIdentifier(), tier, sortOrder, craftable, hidden));
    }
    this.materials = materials.build();
    // process redirects
    int redirectCount = buffer.readVarInt();
    if (redirectCount == 0) {
      this.redirects = Collections.emptyMap();
    } else {
      this.redirects = new HashMap<>(redirectCount);
      for (int i = 0; i < redirectCount; i++) {
        this.redirects.put(new MaterialId(buffer.readUtf()), new MaterialId(buffer.readUtf()));
      }
    }
    this.tags = GenericTagUtil.decodeTags(buffer, MaterialManager.REGISTRY_KEY, id -> this.materials.get(new MaterialId(id)));
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(this.materials.size());
    this.materials.values().forEach(material -> {
      buffer.writeIdentifier(material.getIdentifier().getIdentifier());
      buffer.writeVarInt(material.getTier());
      buffer.writeVarInt(material.getSortOrder());
      buffer.writeBoolean(material.isCraftable());
      buffer.writeBoolean(material.isHidden());
    });
    buffer.writeVarInt(this.redirects.size());
    this.redirects.forEach((key, value) -> {
      buffer.writeUtf(key.toString());
      buffer.writeUtf(value.toString());
    });
    GenericTagUtil.encodeTags(buffer, (IMaterial m) -> m.getIdentifier().getIdentifier(), this.tags);
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    MaterialRegistry.updateMaterialsFromServer(this);
  }
}
