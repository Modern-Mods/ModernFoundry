package modernmods.modernfoundry.library.client.model;

import modernmods.modernfoundry.TConstruct;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import modernmods.mantle.data.listener.ResourceValidator;
import modernmods.modernfoundry.common.config.Config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Logic to handle dynamic texture scans. Really just logging missing textures at this point.
 */
@Log4j2
public class DynamicTextureLoader extends ResourceValidator {
  /** Instance to register with the loader */
  private static final DynamicTextureLoader INSTANCE = new DynamicTextureLoader();

  private DynamicTextureLoader() {
    super("textures/item", "textures", ".png");
  }

  @Override
  public void onReloadSafe(ResourceManager manager) {
    // if we are logging missing textures we can use the vanilla validator instead of needing our own
    if (!Config.CLIENT.logMissingModifierTextures.get()) {
      super.onReloadSafe(manager);
    }
  }

  @Override
  public CompletableFuture<Void> reload(SharedState sharedState, Executor backgroundExecutor, PreparationBarrier stage, Executor gameExecutor) {
    // 26.1 changed the PreparableReloadListener#reload signature; clear the cache once the reload completes to save RAM
    return super.reload(sharedState, backgroundExecutor, stage, gameExecutor).thenRunAsync(this::clear);
  }

  /** Registers this manager */
  public static void init(AddClientReloadListenersEvent event) {
    event.addListener(TConstruct.getResource("dynamic_texture_loader"), INSTANCE);
  }

  /**
   * Gets a consumer to add textures to the given collection
   *
   * @param spriteGetter        Function mapping material names to sprites
   * @param logMissingTextures  If true, log textures that were not found
   * @return  Texture consumer
   */
  public static Predicate<Material> getTextureValidator(Function<Material,TextureAtlasSprite> spriteGetter, boolean logMissingTextures) {
    if (logMissingTextures || INSTANCE.resources.isEmpty()) {
      // this logs due to the vanilla sprite getter logging
      return mat -> !MissingTextureAtlasSprite.getLocation().equals(spriteGetter.apply(mat).contents().name());
    } else {
      return mat -> {
        // to suppress logging, load from our own list. In 26.1 sprite Material no longer carries an atlas location
        // (it is just a sprite identifier), so we validate item textures directly against our scanned resource list.
        Identifier texture = mat.sprite();
        if (texture.getPath().startsWith("item/")) {
          return INSTANCE.test(texture);
        }
        // failed preconditions? can't stop logging even if the boolean says to
        return !MissingTextureAtlasSprite.getLocation().equals(spriteGetter.apply(mat).contents().name());
      };
    }
  }
}
