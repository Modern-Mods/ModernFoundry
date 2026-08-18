package modernmods.modernfoundry.library.json.loot.equipment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.bus.api.EventPriority;
import org.jetbrains.annotations.ApiStatus.Internal;
import modernmods.mantle.data.loadable.Loadable;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.field.ContextKey;
import modernmods.mantle.util.JsonHelper;
import modernmods.mantle.util.typed.TypedMapBuilder;
import modernmods.modernfoundry.TConstruct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Loads the list of mob equipment replacements from JSON */
public class MobEquipmentManager extends SimpleJsonResourceReloadListener<JsonElement> {
  public static final String FOLDER = "tinkering/mob_equipment";
  /** Singleton instance of the manager */
  private static final MobEquipmentManager INSTANCE = new MobEquipmentManager();
  /** Loadable for the list of entities */
  private static final Loadable<List<EntityType<?>>> ENTITY_LIST = Loadables.ENTITY_TYPE.list(1);

  /** Map of active replacements */
  private Map<EntityType<?>,List<MobEquipment>> replacements = Map.of();
  private IContext context = IContext.EMPTY;
  private RegistryAccess registryAccess = RegistryAccess.EMPTY;

  private MobEquipmentManager() {
    super(net.minecraft.util.ExtraCodecs.JSON, net.minecraft.resources.FileToIdConverter.json(FOLDER));
  }

  /** @apiNote no need for addons to call this */
  @Internal
  public static void init() {
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AddServerReloadListenersEvent.class, INSTANCE::addDataPackListeners);
    NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, FinalizeSpawnEvent.class, INSTANCE::finalizeSpawn);
  }

  @Override
  protected void apply(Map<Identifier, JsonElement> jsons, ResourceManager manager, ProfilerFiller profiler) {
    long time = System.nanoTime();
    int loaded = 0;

    // location of the objects only matters for debug, just parse each one
    Map<EntityType<?>, List<MobEquipment>> parsed = new HashMap<>();
    Function<EntityType<?>, List<MobEquipment>> ifAbsent = type -> new ArrayList<>();
    for (Entry<Identifier,JsonElement> entry : jsons.entrySet()) {
      Identifier key = entry.getKey();
      try {
        JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), key.toString());
        // skip if conditions fail
        if (!conditionsMatch(json)) {
          continue;
        }
        // parse the object
        List<MobEquipment> equipment = MobEquipment.LIST_LOADABLE.getIfPresent(json, "equip", TypedMapBuilder.builder().put(ContextKey.ID, key).put(ContextKey.DEBUG, "Mob Equipment " + key).build());

        // determine the entities
        JsonElement entityElement = JsonHelper.getElement(json, "entity");
        // primitive is either single value or tag
        if (entityElement.isJsonPrimitive()) {
          String type = entityElement.getAsString();
          // starting with # is a tag
          if (type.charAt(0) == '#') {
            // need to use the condition context to fetch tag values as they are not yet in the mananger
            TagKey<EntityType<?>> tag = Loadables.ENTITY_TYPE_TAG.parseString(type.substring(1), "entity");
            for (Holder<EntityType<?>> holder : context.getTag(tag)) {
              parsed.computeIfAbsent(holder.value(), ifAbsent).addAll(equipment);
            }
          } else {
            parsed.computeIfAbsent(Loadables.ENTITY_TYPE.parseString(type, "entity"), ifAbsent).addAll(equipment);
          }
        } else if (entityElement.isJsonArray()) {
          // if its an array, assume an array of entitiy type names. No support for tags in the array
          for (EntityType<?> type : ENTITY_LIST.convert(entityElement, "entity")) {
            parsed.computeIfAbsent(type, ifAbsent).addAll(equipment);
          }
        } else {
          throw new JsonSyntaxException("Expected entity to be either a string or an array");
        }

        // add the value to the map
        loaded++;
      } catch (Exception e) {
        TConstruct.LOG.error("Failed to replacement from {}", key, e);
      }
    }

    // build the final map
    this.replacements = parsed.entrySet().stream()
      .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().stream().sorted(Comparator.comparing(MobEquipment::priority).reversed()).toList()));

    TConstruct.LOG.info("Loaded {} mob equipment replacements targeting {} mobs in {} ms", loaded, replacements.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Gets the equipment for the given entity */
  public List<MobEquipment> get(EntityType<?> type) {
    return replacements.getOrDefault(type, List.of());
  }


  /* Events */

  /** Adds the managers as datapack listeners */
  private void addDataPackListeners(AddServerReloadListenersEvent event) {
    event.addListener(TConstruct.getResource("mob_equipment"), this);
    context = event.getConditionContext();
    registryAccess = event.getRegistryAccess();
  }

  /** Checks both legacy "conditions" and modern "neoforge:conditions" blocks. */
  private boolean conditionsMatch(JsonObject json) {
    ConditionalOps<JsonElement> ops = new ConditionalOps<>(RegistryOps.create(JsonOps.INSTANCE, registryAccess), context);
    if (json.has("conditions")) {
      List<ICondition> conditions = ICondition.LIST_CODEC.parse(ops, json.get("conditions")).getOrThrow(JsonSyntaxException::new);
      return conditions.stream().allMatch(condition -> condition.test(context));
    }
    return ICondition.conditionsMatched(ops, json);
  }

  /** Handler for the finalize spawn event */
  private void finalizeSpawn(FinalizeSpawnEvent event) {
    Mob mob = event.getEntity();
    List<MobEquipment> equipment = get(mob.getType());
    if (!equipment.isEmpty() && MobEquipment.apply(equipment, mob, event)) {
      event.setCanceled(true);
    }
  }
}
