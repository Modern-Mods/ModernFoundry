package modernmods.modernfoundry.library.client.materials;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModLoader;
import modernmods.mantle.data.datamap.RegistryDataMapLoader;
import modernmods.mantle.data.listener.IEarlySafeManagerReloadListener;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.util.JsonHelper;
import modernmods.mantle.util.typed.TypedMap;
import modernmods.mantle.util.typed.TypedMapBuilder;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.utils.Util;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

/**
 * Loads the material render info from resource packs. Loaded independently of materials loaded in data packs, so a resource needs to exist in both lists to be used.
 * See {@link modernmods.modernfoundry.library.materials.stats.MaterialStatsManager} for stats.
 * <p>
 * The location inside resource packs is "tinkering/materials".
 * So if your mods name is "foobar", the location for your mods materials is "assets/foobar/tinkering/materials".
 */
@Log4j2
public class MaterialRenderInfoLoader implements IEarlySafeManagerReloadListener {
  public static final MaterialRenderInfoLoader INSTANCE = new MaterialRenderInfoLoader();

  /** Folder to scan for material render info JSONS */
  public static final String FOLDER = "tinkering/materials";

  /**
   * Called on mod construct to register the resource listener
   */
  public static void init()  {
    // bit of a hack: instead of registering our resource listener to the list as we should, we use the additional model registration event
    // we do this as we need to guarantee we run before models are baked, which happens in the first stage of listeners in the bakery constructor
    // the other option would be to wait until the atlas stitch event, though that would make it more difficult to know which sprites we need
    // 26.1.2 renamed ModelEvent.RegisterAdditional to RegisterStandalone; we only use it as an early timing hook (before model bake), not its API
    modernmods.modernfoundry.TConstruct.getModBus().addListener(EventPriority.NORMAL, false, ModelEvent.RegisterStandalone.class, event -> {
      if(!ModLoader.hasErrors()) {
        INSTANCE.onReloadSafe(Minecraft.getInstance().getResourceManager());
      }
    });
  }

  /** Map of all loaded materials */
  private Map<MaterialVariantId,MaterialRenderInfo> renderInfos = ImmutableMap.of();

  private MaterialRenderInfoLoader() {}

  /**
   * Gets a list of all loaded materials render infos
   * @return  All loaded material render infos
   */
  public Collection<MaterialRenderInfo> getAllRenderInfos() {
    return renderInfos.values();
  }

  /**
   * Gets the render info for the given material
   * @param variantId  Material loaded
   * @return  Material render info
   */
  public Optional<MaterialRenderInfo> getRenderInfo(MaterialVariantId variantId) {
    // if there is a variant, try fetching for the variant
    if (variantId.hasVariant()) {
      MaterialRenderInfo info = renderInfos.get(variantId);
      if (info != null) {
        return Optional.of(info);
      }
    }
    // no variant or the variant was not found? default to the material
    return Optional.ofNullable(renderInfos.get(variantId.getId()));
  }

  /** Gets the variant for the given render info path */
  public static MaterialVariantId variant(Identifier location) {
    String path = location.getPath();

    // locate variant as a subfolder, and create final ID
    String variant = "";
    int slashIndex = path.lastIndexOf('/');
    if (slashIndex >= 0) {
      variant = path.substring(slashIndex + 1);
      path = path.substring(0, slashIndex);
    }
    return MaterialVariantId.create(location.getNamespace(), path, variant);
  }

  /** Creates the context for the render info parser */
  public static TypedMap createContext(MaterialVariantId id) {
    return TypedMapBuilder.builder().put(MaterialVariantId.CONTEXT_KEY, id).put(ContextKey.DEBUG, "Material Render Info " + id).build();
  }

  @Override
  public void onReloadSafe(ResourceManager manager) {
    // first, we need to fetch all relevant JSON files
    Map<Identifier,JsonElement> jsons = new HashMap<>();
    SimpleJsonResourceReloadListener.scanDirectory(manager, net.minecraft.resources.FileToIdConverter.json(FOLDER), com.mojang.serialization.JsonOps.INSTANCE, net.minecraft.util.ExtraCodecs.JSON, jsons);
    // final result map
    Map<MaterialVariantId,MaterialRenderInfo> map = new HashMap<>();

    // iterate the files, handling parenting thanks to the data map loader
    for(Entry<Identifier, JsonElement> entry : jsons.entrySet()) {
      // clean up ID by trimming off the extension and folder
      Identifier location = entry.getKey();
      MaterialVariantId id = variant(location);

      // read in the JSON data
      try  {
        JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), location.toString());
        // skip empty objects, its the way to disable it
        if (json.keySet().isEmpty()) {
          continue;
        }
        // parse it into material render info
        map.put(id, RegistryDataMapLoader.parseData("Material Render Info", jsons, location, json, null, MaterialRenderInfo.LOADABLE, createContext(id)));
      } catch (IllegalArgumentException | JsonParseException ex) {
        log.error("Couldn't parse data file {} from {}", id, location, ex);
      }
    }

    // store the list immediately, otherwise it is not in place in time for models to load
    this.renderInfos = Map.copyOf(map);
    log.debug("Loaded material render infos: {}", Util.toIndentedStringList(map.keySet().stream().sorted(Comparator.comparing(MaterialVariantId::getId).thenComparing(MaterialVariantId::getVariant)).toList()));
    log.info("{} material render infos loaded", map.size());
  }


  /* Helpers */

  /** Checks if the given material has any of the given fallbacks. Used by {@link modernmods.modernfoundry.library.client.armor.texture.MaterialHasFallbackTextureSupplier} and {@link modernmods.modernfoundry.library.client.modifiers.model.MaterialHasFallbackModifierModel} */
  public boolean hasFallback(MaterialVariantId material, Set<String> fallbacks) {
    MaterialRenderInfo info = MaterialRenderInfoLoader.INSTANCE.getRenderInfo(material).orElse(null);
    if (info != null) {
      for (String fallback : info.fallbacks()) {
        if (fallbacks.contains(fallback)) {
          return true;
        }
      }
    }
    return false;
  }
}
