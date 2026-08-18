package modernmods.modernfoundry.tools.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import modernmods.modernfoundry.library.client.armor.texture.ArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.DyedArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.FirstArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.FixedArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.MaterialArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.MaterialHasFallbackTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.TrimArmorTextureSupplier;
import modernmods.modernfoundry.library.client.data.AbstractArmorModelProvider;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.tools.ArmorDefinitions;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.TinkerTools;
import modernmods.modernfoundry.tools.item.SlimeskullItem;

public class ArmorModelProvider extends AbstractArmorModelProvider {
  public ArmorModelProvider(PackOutput packOutput) {
    super(packOutput);
  }

  @Override
  protected void addModels() {
    ModifierId dyed = TinkerModifiers.dyed.getId();
    addModel(ArmorDefinitions.TRAVELERS, name -> new ArmorTextureSupplier[] {
      FixedArmorTextureSupplier.builder(name, "/base_").build(),
      new FirstArmorTextureSupplier(
        new DyedArmorTextureSupplier(name, "/cuirass_", dyed, null),
        new MaterialArmorTextureSupplier.Material(name, "/cuirass_", 1)
      ),
      new MaterialArmorTextureSupplier.Material(name, "/metal_", 0),
      TrimArmorTextureSupplier.INSTANCE
    });
    addModel(ArmorDefinitions.PLATE, name -> new ArmorTextureSupplier[] {
      new MaterialArmorTextureSupplier.Material(name, "/plating_", 0),
      new FirstArmorTextureSupplier(
        new MaterialHasFallbackTextureSupplier(1, new DyedArmorTextureSupplier(name.withSuffix("/maille_"), "_metal", dyed, null, 0), "metal", "metal_contrast"),
        new DyedArmorTextureSupplier(name.withSuffix("/maille_"), "_cloth", dyed, null, 0),
        new MaterialArmorTextureSupplier.Material(name, "/maille_", 1)
      ),
      TrimArmorTextureSupplier.INSTANCE
    });
    Identifier slime = ArmorDefinitions.SLIMESUIT.getId();
    addModel(slime,
      new MaterialArmorTextureSupplier.Material(slime, "/", 1),
      TrimArmorTextureSupplier.INSTANCE
    );
    addModel(SlimeskullItem.MODEL_LOCATION,
      new MaterialArmorTextureSupplier.Material(slime, "/", 1),
      TrimArmorTextureSupplier.INSTANCE
    );
    addModel(TinkerTools.slimeWings,
      new MaterialArmorTextureSupplier.Material(slime, "/", 0),
      TrimArmorTextureSupplier.INSTANCE
    );
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Armor Models";
  }
}
