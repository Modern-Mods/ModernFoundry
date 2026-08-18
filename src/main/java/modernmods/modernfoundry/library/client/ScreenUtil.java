package modernmods.modernfoundry.library.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Client-only helpers for polling keyboard modifier state.
 * <p>
 * 26.1.2 removed the static {@code Screen#hasShiftDown()}/{@code hasControlDown()} helpers (modifier state now comes
 * from input events), so these replicate the former behavior for code that needs to poll it outside an event.
 */
public final class ScreenUtil {
  private ScreenUtil() {}

  private static com.mojang.blaze3d.platform.Window window() {
    return Minecraft.getInstance().getWindow();
  }

  /** Checks if either shift key is currently held */
  public static boolean hasShiftDown() {
    return InputConstants.isKeyDown(window(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window(), GLFW.GLFW_KEY_RIGHT_SHIFT);
  }

  /** Checks if either control key (or command on macOS) is currently held */
  public static boolean hasControlDown() {
    if (net.minecraft.util.Util.getPlatform() == net.minecraft.util.Util.OS.OSX) {
      return InputConstants.isKeyDown(window(), GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(window(), GLFW.GLFW_KEY_RIGHT_SUPER);
    }
    return InputConstants.isKeyDown(window(), GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(window(), GLFW.GLFW_KEY_RIGHT_CONTROL);
  }
}
