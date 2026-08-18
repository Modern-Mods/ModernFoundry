package modernmods.modernfoundry.library.client.data.material;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import modernmods.modernfoundry.library.client.data.material.AbstractMaterialSpriteProvider.MaterialSpriteInfo;
import modernmods.modernfoundry.library.client.data.material.AbstractPartSpriteProvider.PartSpriteInfo;
import modernmods.modernfoundry.library.client.data.spritetransformer.ISpriteTransformer;
import modernmods.modernfoundry.library.client.data.util.AbstractSpriteReader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Helpers for generating material part textures, shared with the in-game part texture regeneration command.
 * <p>
 * The datagen provider flow (a {@code GenericTextureGenerator} that read existing sprites through the removed
 * {@code net.neoforged.neoforge.common.data.ExistingFileHelper} / {@code DataGenSpriteReader}) was dropped in the 26.1
 * port. Only the static generation utilities used at runtime remain here; the {@code ResourceManager}-based path
 * (see {@code ClientGeneratePartTexturesCommand}) is unaffected. Restore the provider instance flow when datagen is
 * re-enabled on 26.1.
 */
public class MaterialPartTextureGenerator {
  private MaterialPartTextureGenerator() {}

  /** Path to textures outputted by this generator */
  public static final String FOLDER = "textures";

  /** Gets the output path for a given sprite */
  public static Identifier outputPath(PartSpriteInfo part, MaterialSpriteInfo material) {
    // path format: pNamespace:pPath_mNamespace_mPath
    Identifier materialTexture = material.getTexture();
    return part.getPath().withSuffix("_" + materialTexture.getNamespace() + "_" + materialTexture.getPath());
  }

  /**
   * Generates the given sprite
   * @param spriteReader    Reader to find existing sprites
   * @param material        Material for the sprite
   * @param part            Part for the sprites
   * @param saver           Function to save the images
   * @param metaSaver       Function to save the animation metadata
   */
  public static void generateSprite(AbstractSpriteReader spriteReader, MaterialSpriteInfo material, PartSpriteInfo part, Identifier spritePath, BiConsumer<Identifier, NativeImage> saver, BiConsumer<Identifier,JsonObject> metaSaver) {
    // image does not exist? first step is to find a base image
    NativeImage base = null;
    for (String fallback : material.getFallbacks()) {
      base = part.getTexture(spriteReader, fallback);
      if (base != null) {
        break;
      }
    }
    // no fallback existed, try the main one
    if (base == null) {
      base = part.getTexture(spriteReader, "");
    }
    if (base == null) {
      throw new IllegalStateException("Missing sprite at " + part.getPath() + ".png, cannot generate textures");
    }
    // successfully found a texture, now transform and save
    ISpriteTransformer transformer = material.getTransformer();
    NativeImage transformed = transformer.transformCopy(base, part.isAllowAnimated());
    spriteReader.track(transformed);
    saver.accept(spritePath, transformed);
    if (part.isAllowAnimated()) {
      JsonObject meta = transformer.animationMeta(base);
      if (meta != null) {
        metaSaver.accept(spritePath, meta);
      }
    }
  }


  /* Static callbacks, handled this way as the event bus is a pain to use during datagen */

  /** List of callbacks */
  private static final List<IPartTextureCallback> TEXTURE_CALLBACKS = new ArrayList<>();

  /** Registers a callback to run whenever sprites are generated. */
  public static void registerCallback(IPartTextureCallback callback) {
    TEXTURE_CALLBACKS.add(callback);
  }

  /** Runs all callbacks. A nonnull manager means generation is starting; null means it is ending. */
  public static void runCallbacks(@Nullable ResourceManager manager) {
    for (IPartTextureCallback callback : TEXTURE_CALLBACKS) {
      callback.accept(manager);
    }
  }

  public interface IPartTextureCallback {
    /**
     * Tells the given callback that texture generating is either starting or ending.
     * @param manager  If nonnull, generation is starting (from the in-game command); null means generation is ending
     */
    void accept(@Nullable ResourceManager manager);
  }
}
