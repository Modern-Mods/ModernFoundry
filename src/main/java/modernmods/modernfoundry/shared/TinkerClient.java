package modernmods.modernfoundry.shared;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.common.recipe.RecipeCacheInvalidator;
import modernmods.modernfoundry.library.client.armor.texture.ArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.DyedArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.FirstArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.FixedArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.MaterialArmorTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.MaterialHasFallbackTextureSupplier;
import modernmods.modernfoundry.library.client.armor.texture.TrimArmorTextureSupplier;
import modernmods.modernfoundry.library.client.book.TinkerBook;
import modernmods.modernfoundry.library.client.data.spritetransformer.FramesSpriteTransformer;
import modernmods.modernfoundry.library.client.data.spritetransformer.GreyToColorMapping;
import modernmods.modernfoundry.library.client.data.spritetransformer.GreyToSpriteTransformer;
import modernmods.modernfoundry.library.client.data.spritetransformer.IColorMapping;
import modernmods.modernfoundry.library.client.data.spritetransformer.ISpriteTransformer;
import modernmods.modernfoundry.library.client.data.spritetransformer.OffsettingSpriteTransformer;
import modernmods.modernfoundry.library.client.data.spritetransformer.RecolorSpriteTransformer;
import modernmods.modernfoundry.library.client.materials.MaterialRenderInfoLoader;
import modernmods.modernfoundry.library.client.modifiers.DyedModifierModel;
import modernmods.modernfoundry.library.client.modifiers.MaterialModifierModel;
import modernmods.modernfoundry.library.client.modifiers.ModifierIconManager;
import modernmods.modernfoundry.library.client.modifiers.NormalModifierModel;
import modernmods.modernfoundry.library.client.modifiers.PotionModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.BannerModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.CompoundModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.ConditionalModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.FluidModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.MaterialHasFallbackModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.ModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.TankModifierModel;
import modernmods.modernfoundry.library.client.modifiers.model.TraitModel;
import modernmods.modernfoundry.library.client.modifiers.model.TrimModifierModel;

import java.util.function.Consumer;

import static modernmods.modernfoundry.TConstruct.getResource;

/**
 * This class should only be referenced on the client side
 */
@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class TinkerClient {
  /**
   * Called by TConstruct to handle any client side logic that needs to run during the constructor
   */
  public static void onConstruct() {
    TinkerBook.initBook();
    // needs to register listeners early enough for minecraft to load
    ModifierIconManager.init();
    MaterialRenderInfoLoader.init();

    // add the recipe cache invalidator to the client
    Consumer<RecipesReceivedEvent> recipesUpdated = event -> RecipeCacheInvalidator.reload(true);
    NeoForge.EVENT_BUS.addListener(recipesUpdated);

    // register datagen serializers
    ISpriteTransformer.SERIALIZER.registerDeserializer(RecolorSpriteTransformer.NAME, RecolorSpriteTransformer.DESERIALIZER);
    GreyToSpriteTransformer.init();
    ISpriteTransformer.SERIALIZER.registerDeserializer(OffsettingSpriteTransformer.NAME, OffsettingSpriteTransformer.DESERIALIZER);
    ISpriteTransformer.SERIALIZER.registerDeserializer(FramesSpriteTransformer.NAME, FramesSpriteTransformer.DESERIALIZER);
    IColorMapping.SERIALIZER.registerDeserializer(GreyToColorMapping.NAME, GreyToColorMapping.DESERIALIZER);

    // armor textures
    ArmorTextureSupplier.LOADER.register(getResource("fixed"), FixedArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("dyed"), DyedArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("first_present"), FirstArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("material"), MaterialArmorTextureSupplier.Material.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("persistent_data"), MaterialArmorTextureSupplier.PersistentData.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("trim"), TrimArmorTextureSupplier.LOADER);
    ArmorTextureSupplier.LOADER.register(getResource("material_has_fallback"), MaterialHasFallbackTextureSupplier.LOADER);

    // modifier models
    ModifierModel.LOADER.register(getResource("empty"), ModifierModel.EMPTY.getLoader());
    ModifierModel.LOADER.register(getResource("compound"), CompoundModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("conditional"), ConditionalModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("trait"), TraitModel.LOADER);
    ModifierModel.LOADER.register(getResource("basic"), NormalModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("dyed"), DyedModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("material"), MaterialModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("potion"), PotionModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("armor_trim"), TrimModifierModel.Armor.LOADER);
    ModifierModel.LOADER.register(getResource("custom_trim"), TrimModifierModel.Custom.LOADER);
    ModifierModel.LOADER.register(getResource("banner"), BannerModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("fluid"), FluidModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("tank"), TankModifierModel.LOADER);
    ModifierModel.LOADER.register(getResource("material_has_fallback"), MaterialHasFallbackModifierModel.LOADER);
  }

  @SubscribeEvent
  static void renderBlockOverlay(RenderBlockScreenEffectEvent event) {
    BlockState state = event.getBlockState();
    if (state.is(TinkerTags.Blocks.TRANSPARENT_OVERLAY)) {
      Minecraft minecraft = Minecraft.getInstance();
      assert minecraft.level != null;
      assert minecraft.player != null;
      BlockPos pos = event.getBlockPos();
      float width = minecraft.player.getBbWidth() * 0.8F;
      // check collision of the block again, for non-full blocks
      if (Shapes.joinIsNotEmpty(state.getShape(minecraft.level, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(AABB.ofSize(minecraft.player.getEyePosition(), width, 1.0E-6D, width)), BooleanOp.AND)) {
        // DEFERRED RENDER: the pre-26.1 custom transparent overlay quad was drawn with the removed immediate-mode pipeline
        // (RenderSystem.setShader/setShaderTexture, Tesselator/BufferBuilder + BufferUploader.drawWithShader, and
        // LightTexture.getBrightness). In 26.1 the screen-effect overlay must be rebuilt against the new render pipeline
        // (ScreenEffectRenderer). Cancelling the vanilla overlay below already leaves the block view transparent (the intent
        // of TRANSPARENT_OVERLAY); re-adding the tinted quad is validated in-game.
      }
      event.setCanceled(true);
    }
  }
}
