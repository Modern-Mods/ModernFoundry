package modernmods.modernfoundry.library.modifiers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import modernmods.mantle.network.packet.IThreadsafePacket;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.impl.ComposableModifier;
import modernmods.modernfoundry.library.utils.GenericTagUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Packet to sync modifiers */
public class UpdateModifiersPacket implements IThreadsafePacket {
  /** Collection of all modifiers */
  private final Map<ModifierId,Modifier> allModifiers;
  /** Map of all modifier tags */
  private final Map<TagKey<Modifier>,List<Modifier>> tags;
  /** Collection of non-redirect modifiers */
  private Collection<ComposableModifier> modifiers;
  /** Map of modifier redirect ID pairs */
  private Map<ModifierId,ModifierId> redirects;
  /** Map of enchantment to modifier pair */
  private final Map<Enchantment,Modifier> enchantmentMap;
  /** Collection of all enchantment tag mappings */
  private final Map<TagKey<Enchantment>, Modifier> enchantmentTagMappings;

  public UpdateModifiersPacket(Map<ModifierId,Modifier> allModifiers, Map<TagKey<Modifier>,List<Modifier>> tags, Map<Enchantment,Modifier> enchantmentMap, Map<TagKey<Enchantment>,Modifier> enchantmentTagMappings) {
    this.allModifiers = allModifiers;
    this.tags = tags;
    this.enchantmentMap = enchantmentMap;
    this.enchantmentTagMappings = enchantmentTagMappings;
  }

  /** Ensures both the modifiers and redirects lists are calculated, allows one packet to be used multiple times without redundant work */
  private void ensureCalculated() {
    if (this.modifiers == null || this.redirects == null) {
      ImmutableList.Builder<ComposableModifier> modifiers = ImmutableList.builder();
      ImmutableMap.Builder<ModifierId,ModifierId> redirects = ImmutableMap.builder();
      for (Entry<ModifierId,Modifier> entry : allModifiers.entrySet()) {
        ModifierId id = entry.getKey();
        Modifier value = entry.getValue();
        ModifierId actual = value.getId();
        if (id.equals(actual)) {
          // we can't sync anything that is not composable
          if (value instanceof ComposableModifier composable) {
            modifiers.add(composable);
          } else {
            TConstruct.LOG.warn("Unable to sync modifier {} as its not ComposableModifier; got class {}", id, value.getClass().getName());
          }
        } else {
          redirects.put(id, actual);
        }
      }
      this.modifiers = modifiers.build();
      this.redirects = redirects.build();
    }
  }

  /** Gets a modifier by the given ID, falling back to the map if needed */
  private static Modifier getModifier(Map<ModifierId,Modifier> modifiers, ModifierId id) {
    Modifier modifier = ModifierManager.INSTANCE.getStatic(id);
    if (modifier == ModifierManager.INSTANCE.getDefaultValue()) {
      modifier = modifiers.get(id);
      if (modifier == null) {
        throw new DecoderException("Unknown modifier " + id);
      }
    }
    return modifier;
  }

  /** Gets the active enchantment registry lookup for packet encoding or decoding. */
  private static HolderLookup.RegistryLookup<Enchantment> enchantmentLookup() {
    HolderLookup.RegistryLookup<Enchantment> lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
    if (lookup == null) {
      throw new DecoderException("Enchantment registry is unavailable");
    }
    return lookup;
  }

  /** Looks up an enchantment by ID. */
  private static Enchantment getEnchantment(Identifier id) {
    return enchantmentLookup().get(ResourceKey.create(Registries.ENCHANTMENT, id)).map(Holder::value).orElseThrow(() -> new DecoderException("Unknown enchantment " + id));
  }

  /** Gets the ID for the given enchantment. */
  private static Identifier getEnchantmentId(Enchantment enchantment) {
    return enchantmentLookup().listElements()
      .filter(holder -> holder.value() == enchantment)
      .findFirst()
      .map(holder -> holder.key().identifier())
      .orElseThrow(() -> new EncoderException("Unknown enchantment " + enchantment.description().getString()));
  }

  public UpdateModifiersPacket(FriendlyByteBuf buffer) {
    // read in modifiers
    int size = buffer.readVarInt();
    Map<ModifierId,Modifier> modifiers = new HashMap<>();
    for (int i = 0; i < size; i++) {
      ModifierId id = new ModifierId(buffer.readUtf(Short.MAX_VALUE));
      Modifier modifier = ComposableModifier.LOADER.decode(buffer, ModifierManager.contextBuilder(id.getIdentifier()).build());
      // need cast to call package private method
      modifier.setId(id);
      modifiers.put(id, modifier);
    }
    // read in redirects
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      ModifierId from = new ModifierId(buffer.readUtf(Short.MAX_VALUE));
      modifiers.put(from, getModifier(modifiers, new ModifierId(buffer.readUtf(Short.MAX_VALUE))));
    }
    this.allModifiers = modifiers;
    this.tags = GenericTagUtil.decodeTags(buffer, ModifierManager.REGISTRY_KEY, id -> getModifier(modifiers, new ModifierId(id)));

    // read in enchantment to modifier mapping
    ImmutableMap.Builder<Enchantment,Modifier> enchantmentBuilder = ImmutableMap.builder();
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      enchantmentBuilder.put(
        getEnchantment(buffer.readIdentifier()),
        getModifier(modifiers, new ModifierId(buffer.readIdentifier())));
    }
    enchantmentMap = enchantmentBuilder.build();
    ImmutableMap.Builder<TagKey<Enchantment>, Modifier> enchantmentTagBuilder = ImmutableMap.builder();
    size = buffer.readVarInt();
    for (int i = 0; i < size; i++) {
      enchantmentTagBuilder.put(
        TagKey.create(Registries.ENCHANTMENT, buffer.readIdentifier()),
        getModifier(modifiers, new ModifierId(buffer.readIdentifier())));
    }
    enchantmentTagMappings = enchantmentTagBuilder.build();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    ensureCalculated();
    // write modifiers
    buffer.writeVarInt(modifiers.size());
    for (ComposableModifier modifier : modifiers) {
      buffer.writeIdentifier(modifier.getId().getIdentifier());
      ComposableModifier.LOADER.encode(buffer, modifier);
    }
    // write redirects
    buffer.writeVarInt(redirects.size());
    for (Entry<ModifierId,ModifierId> entry : redirects.entrySet()) {
      buffer.writeIdentifier(entry.getKey().getIdentifier());
      buffer.writeIdentifier(entry.getValue().getIdentifier());
    }
    GenericTagUtil.encodeTags(buffer, (Modifier m) -> m.getId().getIdentifier(), this.tags);

    // enchantment mapping
    buffer.writeVarInt(enchantmentMap.size());
    for (Entry<Enchantment,Modifier> entry : enchantmentMap.entrySet()) {
      buffer.writeIdentifier(getEnchantmentId(entry.getKey()));
      buffer.writeIdentifier(entry.getValue().getId().getIdentifier());
    }
    buffer.writeVarInt(enchantmentTagMappings.size());
    for (Entry<TagKey<Enchantment>, Modifier> entry : enchantmentTagMappings.entrySet()) {
      buffer.writeIdentifier(entry.getKey().location());
      buffer.writeIdentifier(entry.getValue().getId().getIdentifier());
    }
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    ModifierManager.INSTANCE.updateModifiersFromServer(allModifiers, tags, enchantmentMap, enchantmentTagMappings);
  }
}
