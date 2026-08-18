package modernmods.modernfoundry.library.client.armor.texture;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.common.ColorLoadable;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.primitive.StringLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.Objects;

import static modernmods.modernfoundry.library.client.armor.texture.FixedArmorTextureSupplier.getTexture;

/**
 * Armor texture supplier that supplies a fixed texture that is colored using the given persistent data key
 */
public class DyedArmorTextureSupplier implements ArmorTextureSupplier {
  public static final RecordLoadable<DyedArmorTextureSupplier> LOADER = RecordLoadable.create(
    Loadables.RESOURCE_LOCATION.requiredField("prefix", s -> s.prefix),
    StringLoadable.DEFAULT.defaultField("suffix", "", s -> s.suffix),
    ModifierId.PARSER.defaultField("modifier", TinkerModifiers.dyed.getId(), s -> s.modifier),
    ColorLoadable.NO_ALPHA.nullableField("default_color", s -> s.alwaysRender ? s.defaultColor : null),
    IntLoadable.range(0, 15).defaultField("luminosity", 0, false, s -> s.luminosity),
    DyedArmorTextureSupplier::new);

  private final Identifier prefix;
  private final String suffix;
  private final ModifierId modifier;
  private final boolean alwaysRender;
  private final int defaultColor;
  private final int luminosity;
  private final TintedArmorTexture[] textures;

  public DyedArmorTextureSupplier(Identifier prefix, String suffix, ModifierId modifier, @Nullable Integer defaultColor, int luminosity) {
    this.prefix = prefix;
    this.suffix = suffix;
    this.modifier = modifier;
    this.alwaysRender = defaultColor != null;
    this.defaultColor = Objects.requireNonNullElse(defaultColor, -1);
    this.luminosity = luminosity;
    this.textures = new TintedArmorTexture[] {
      getTexture(prefix, "armor" + suffix, -1, luminosity),
      getTexture(prefix, "leggings" + suffix, -1, luminosity),
      getTexture(prefix, "wings" + suffix, -1, luminosity),
    };
  }

  // TODO 1.21: cleanup constructor variants
  public DyedArmorTextureSupplier(Identifier prefix, ModifierId modifier, @Nullable Integer defaultColor, int luminosity) {
    this(prefix, "", modifier, defaultColor, luminosity);
  }

  public DyedArmorTextureSupplier(Identifier prefix, ModifierId modifier, @Nullable Integer defaultColor) {
    this(prefix, modifier, defaultColor, 0);
  }

  public DyedArmorTextureSupplier(Identifier base, String variant, ModifierId modifier, @Nullable Integer defaultColor) {
    this(base.withSuffix(variant), modifier, defaultColor);
  }

  @Override
  public ArmorTexture getArmorTexture(ItemStack stack, TextureType textureType, RegistryAccess access) {
    TintedArmorTexture texture = textures[textureType.ordinal()];
    if (texture != null && (alwaysRender || ModifierUtil.getModifierLevel(stack, modifier) > 0)) {
      int color = ModifierUtil.getPersistentInt(stack, modifier.getIdentifier(), defaultColor);
      return texture.color(0xFF000000 | color);
    }
    return ArmorTexture.EMPTY;
  }

  @Override
  public RecordLoadable<DyedArmorTextureSupplier> getLoader() {
    return LOADER;
  }
}
