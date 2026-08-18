package modernmods.modernfoundry.library.tools.item.armor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import modernmods.modernfoundry.library.client.armor.ArmorModelManager.ArmorModelDispatcher;
import modernmods.modernfoundry.library.tools.definition.ModifiableArmorMaterial;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;

import java.util.function.Consumer;

/**
 * Armor item that applies multiple texture layers in order. In 26.1 the actual armor layer textures are data-driven
 * through the equipment asset referenced by the armor material; the extra rendering layers are supplied client-side by
 * {@link ArmorModelDispatcher}.
 */
public class MultilayerArmorItem extends ModifiableArmorItem {
  private final Identifier name;
  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorType slot, Properties properties) {
    this(material, slot, properties, material.getId());
  }

  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorType slot, Properties properties, Identifier name) {
    super(material, slot, properties);
    this.name = name;
  }

  public MultilayerArmorItem(DummyArmorMaterial material, ArmorType slot, Properties properties, ToolDefinition toolDefinition) {
    this(material.getArmorMaterial(), slot, properties, toolDefinition, material.getId());
  }

  public MultilayerArmorItem(ArmorMaterial material, ArmorType slot, Properties properties, ToolDefinition toolDefinition, Identifier name) {
    super(material, slot, properties, toolDefinition);
    this.name = name;
  }

  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorType slot, Properties properties, ToolDefinition toolDefinition, Identifier name) {
    this(material.getArmorMaterial(), slot, properties, toolDefinition, name);
  }

  // initializeClient removed from Item/MobEffect/FluidType in 26.1; registered via RegisterClientExtensionsEvent
  public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    consumer.accept(new ArmorModelDispatcher() {
      @Override
      protected Identifier getName() {
        return name;
      }
    });
  }
}
