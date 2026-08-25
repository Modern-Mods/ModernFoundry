package modernmods.modernfoundry.world.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SlimeArmorLayerTest {
  @Test
  void rendersEveryArmorMaterialLayer() throws IOException {
    String source = Files.readString(Path.of(
      "src/main/java/modernmods/modernfoundry/world/client/SlimeArmorLayer.java"));

    assertThat(source).doesNotContain("layers().get(0)");
    assertThat(source).contains("for (ArmorMaterial.Layer layer : armor.getMaterial().value().layers())");
    assertThat(source).contains(
      "ClientHooks.getArmorTexture(entity, helmet, layer, false, EquipmentSlot.HEAD)");
  }
}
