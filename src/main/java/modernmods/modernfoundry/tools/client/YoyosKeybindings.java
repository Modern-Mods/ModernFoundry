package modernmods.modernfoundry.tools.client;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import com.mojang.blaze3d.platform.InputConstants;

public final class YoyosKeybindings {
  private YoyosKeybindings() {}

  public static final KeyMapping OPEN_CONFIG = new KeyMapping(
    TConstruct.makeTranslationKey("key", "open_yoyo_config"),
    KeyConflictContext.IN_GAME,
    InputConstants.getKey("key.keyboard.b"),
    "key.categories.modernfoundry"
  );

  public static void register(RegisterKeyMappingsEvent event) {
    event.register(OPEN_CONFIG);
  }

  public static void handleEventInput(ClientTickEvent.Pre event) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player != null && OPEN_CONFIG.consumeClick() && minecraft.player.getMainHandItem().getItem() instanceof YoyoItem) {
      minecraft.setScreen(new YoyoConfigScreen());
    }
  }
}
