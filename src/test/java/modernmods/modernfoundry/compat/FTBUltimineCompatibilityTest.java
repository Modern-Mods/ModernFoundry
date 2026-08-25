package modernmods.modernfoundry.compat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class FTBUltimineCompatibilityTest {
  @Test
  void includesOnlyPrimaryAndStoneMiningTools() throws IOException {
    var resource = getClass().getClassLoader().getResourceAsStream(
      "data/ftbultimine/tags/item/included_tools.json");
    assertThat(resource).as("FTB Ultimine included-tools tag").isNotNull();

    try (resource) {
      JsonObject tag = new Gson().fromJson(new InputStreamReader(resource, StandardCharsets.UTF_8), JsonObject.class);
      assertThat(tag.get("replace").getAsBoolean()).isFalse();
      assertThat(tag.getAsJsonArray("values")).extracting(element -> element.getAsString())
        .containsExactly(
          "#modernfoundry:modifiable/harvest/primary",
          "#modernfoundry:modifiable/harvest/stone");
    }
  }
}
