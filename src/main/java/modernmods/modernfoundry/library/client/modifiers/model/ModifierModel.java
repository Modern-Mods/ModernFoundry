package modernmods.modernfoundry.library.client.modifiers.model;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.InventoryMenu;
import modernmods.mantle.data.loadable.Loadables;
import modernmods.mantle.data.loadable.primitive.StringLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.mantle.data.registry.GenericLoaderRegistry;
import modernmods.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import modernmods.mantle.util.ItemLayerPixels;
import modernmods.modernfoundry.library.client.modifiers.IBakedModifierModel;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

/** Represents a model defined for the given tool */
public interface ModifierModel extends IBakedModifierModel, IHaveLoader {
  ModifierModel EMPTY = SingletonLoader.singleton(loader -> new ModifierModel() {
    @Override
    public void addQuads(IToolStackView tool, ModifierEntry modifier, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, Consumer<Collection<BakedQuad>> quadConsumer, @Nullable ItemLayerPixels pixels) {}

    @Nullable
    @Override
    public Object getCacheKey(IToolStackView tool, ModifierEntry modifier) {
      return null;
    }

    @Override
    public RecordLoadable<? extends ModifierModel> getLoader() {
      return loader;
    }

    @Override
    public void validate(Function<Material, TextureAtlasSprite> spriteGetter) {}
  });

  /** Loader for registering modifier models */
  GenericLoaderRegistry<ModifierModel> LOADER = new GenericLoaderRegistry<>("Modifier Model", EMPTY, false);
  /** Loadable for reading materials, a common feature of modifier models */
  StringLoadable<Material> MATERIAL_LOADABLE = Loadables.RESOURCE_LOCATION.flatXmap(ModifierModel::blockAtlas, Material::sprite);

  @Override
  RecordLoadable<? extends ModifierModel> getLoader();

  /** Validates that all textures in this model exist. */
  void validate(Function<Material, TextureAtlasSprite> spriteGetter);

  static Material blockAtlas(Identifier path) {
    // in 26.1 the sprite Material references the sprite id directly; the block atlas is implicit
    return new Material(path);
  }
}
