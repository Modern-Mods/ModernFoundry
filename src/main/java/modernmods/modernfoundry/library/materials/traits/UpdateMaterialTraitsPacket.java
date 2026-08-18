package modernmods.modernfoundry.library.materials.traits;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.MaterialId;

import java.util.HashMap;
import java.util.Map;

public class UpdateMaterialTraitsPacket implements IThreadsafePacket {
  protected final Map<MaterialId,MaterialTraits> materialToTraits;

  public UpdateMaterialTraitsPacket(Map<MaterialId,MaterialTraits> materialToTraits) {
    this.materialToTraits = materialToTraits;
  }

  /** Gets the material to traits map */
  public Map<MaterialId,MaterialTraits> getMaterialToTraits() {
    return materialToTraits;
  }

  public UpdateMaterialTraitsPacket(FriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    materialToTraits = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readIdentifier());
      MaterialTraits traits = MaterialTraits.read(buffer);
      materialToTraits.put(id, traits);
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(materialToTraits.size());
    materialToTraits.forEach((materialId, traits) -> {
      buffer.writeIdentifier(materialId.getIdentifier());
      traits.write(buffer);
    });
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    MaterialRegistry.updateMaterialTraitsFromServer(this);
  }
}
