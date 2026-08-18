package modernmods.modernfoundry.library.utils;

/**
 * Helper for packing and unpacking colors in ABGR order, the format used by {@link net.minecraft.client.renderer.texture.NativeImage}.
 * Replaces the removed {@code net.minecraft.util.FastColor.ABGR32}. A packed int is laid out as
 * {@code (alpha << 24) | (blue << 16) | (green << 8) | red}.
 */
public final class ABGR {
  private ABGR() {}

  /** Extracts the alpha channel (0-255) */
  public static int alpha(int color) {
    return color >>> 24 & 0xFF;
  }

  /** Extracts the blue channel (0-255) */
  public static int blue(int color) {
    return color >> 16 & 0xFF;
  }

  /** Extracts the green channel (0-255) */
  public static int green(int color) {
    return color >> 8 & 0xFF;
  }

  /** Extracts the red channel (0-255) */
  public static int red(int color) {
    return color & 0xFF;
  }

  /** Packs the given channels into an ABGR color int */
  public static int color(int alpha, int blue, int green, int red) {
    return (alpha & 0xFF) << 24 | (blue & 0xFF) << 16 | (green & 0xFF) << 8 | red & 0xFF;
  }
}
