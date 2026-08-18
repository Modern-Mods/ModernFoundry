package modernmods.modernfoundry.library.client.data;

import com.google.common.hash.Hashing;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import modernmods.mantle.data.GenericDataProvider;
import modernmods.modernfoundry.TConstruct;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Data generator to create png image files.
 * <p>
 * 26.1 dropped {@code net.neoforged.neoforge.common.data.ExistingFileHelper}, so the "mark file as existing" tracking
 * that the old constructor did is gone. Subclasses that need to read existing sprites now pull them from the
 * {@link ResourceManager} (see {@link modernmods.modernfoundry.library.client.data.util.ResourceManagerSpriteReader}),
 * which is supplied by {@code GatherDataEvent.Client#getResourceManager}.
 */
public abstract class GenericTextureGenerator extends GenericDataProvider {
  /** Resource manager used to read existing sprites, or null if this generator does not read any */
  @Nullable
  protected final ResourceManager resourceManager;

  /** Constructor which can read existing sprites through the given resource manager */
  public GenericTextureGenerator(PackOutput packOutput, @Nullable ResourceManager resourceManager, String folder) {
    super(packOutput, Target.RESOURCE_PACK, folder);
    this.resourceManager = resourceManager;
  }

  /** Constructor which does not read existing sprites */
  public GenericTextureGenerator(PackOutput packOutput, String folder) {
    this(packOutput, null, folder);
  }

  /** Saves the given image to the given location */
  protected CompletableFuture<?> saveImage(CachedOutput cache, Identifier location, NativeImage image) {
    return CompletableFuture.runAsync(() -> {
      try {
        Path path = this.pathProvider.file(location, "png");
        // 26.1 removed NativeImage#asByteArray(); encode to PNG in-memory via writeToChannel (exposed by AT)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        image.writeToChannel(Channels.newChannel(out));
        byte[] bytes = out.toByteArray();
        cache.writeIfNeeded(path, bytes, Hashing.sha1().hashBytes(bytes));
      } catch (IOException e) {
        TConstruct.LOG.error("Couldn't write image for {}", location, e);
        throw new CompletionException(e);
      }
    }, Util.backgroundExecutor());
  }

  /** Saves metadata for the given image */
  protected CompletableFuture<?> saveMetadata(CachedOutput cache, Identifier location, JsonObject metadata) {
    return DataProvider.saveStable(cache, metadata, this.pathProvider.file(location, "png.mcmeta"));
  }
}
