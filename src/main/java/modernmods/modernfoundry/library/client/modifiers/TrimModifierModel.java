package modernmods.modernfoundry.library.client.modifiers;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.library.client.modifiers.model.TrimModifierModel.Armor;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Modifier model adding trim overlays to an item
 * @deprecated use {@link modernmods.modernfoundry.library.client.modifiers.model.TrimModifierModel.Armor}
 */
@Deprecated
public enum TrimModifierModel implements IBakedModifierModel {
  INSTANCE;

  public static final RecordLoadable<TrimModifierModel> LOADER = new SingletonLoader<>(INSTANCE);

  /** @deprecated use {@link Armor#getRoot()} */
  @Deprecated(forRemoval = true)
  public static final Identifier[] TRIM_TEXTURES = new Identifier[4];
  static {
    for (Armor type : Armor.values()) {
      TRIM_TEXTURES[type.ordinal()] = type.getRoot(false);
    }
  }

  /** @deprecated legacy system, use {@link #LOADER} */
  @Deprecated
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    // if we are loading the model, then we are reloading resources
    for (Armor type : Armor.values()) {
      type.clearCache();
    }
    return INSTANCE;
  };

  @Nullable
  @Override
  public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
    // cache key does not change per type
    return Armor.HELMET.getCacheKey(tool, modifier);
  }

  @Override
  public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {
    // 26.1 removed ArmorItem.Type; resolve the armor slot from the equippable data component instead
    if (!isLarge) {
      Equippable equippable = tool.getItem().components().get(DataComponents.EQUIPPABLE);
      if (equippable != null) {
        int ordinal = switch (equippable.slot()) {
          case HEAD -> 0;
          case CHEST -> 1;
          case LEGS -> 2;
          case FEET -> 3;
          default -> -1;
        };
        if (ordinal >= 0) {
          Armor model = Armor.values()[ordinal];
          model.addQuads(tool, modifier, spriteGetter, transforms, isLarge, startTintIndex, quadConsumer, pixels);
        }
      }
    }
  }
}
