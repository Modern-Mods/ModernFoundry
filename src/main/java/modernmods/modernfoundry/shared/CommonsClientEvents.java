package modernmods.modernfoundry.shared;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.color.block.BlockTintSource;
import modernmods.modernfoundry.shared.block.ClearStainedGlassBlock;
import modernmods.modernfoundry.shared.block.ClearStainedGlassBlock.GlassColor;
import java.util.List;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.ClientEventBase;
import modernmods.modernfoundry.library.client.book.TinkerBook;
import modernmods.modernfoundry.library.client.model.UniqueGuiModel;
import modernmods.modernfoundry.library.utils.DomainDisplayName;
import modernmods.modernfoundry.shared.client.FluidParticle;

@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT)
public class CommonsClientEvents extends ClientEventBase {
  private static final Identifier BLOCK_ITEM_LANGUAGE = TConstruct.getResource("block_item_language");
  private static final SimplePreparableReloadListener<Void> BLOCK_ITEM_LANGUAGE_LISTENER = new SimplePreparableReloadListener<>() {
    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
      return null;
    }

    @Override
    protected void apply(Void ignored, ResourceManager resourceManager, ProfilerFiller profiler) {
      Language.inject(new BlockItemLanguage(Language.getInstance()));
    }
  };

  @SubscribeEvent
  static void addResourceListeners(AddClientReloadListenersEvent event) {
    DomainDisplayName.addResourceListener(event);
    event.addListener(BLOCK_ITEM_LANGUAGE, BLOCK_ITEM_LANGUAGE_LISTENER);
    event.addDependency(VanillaClientListeners.LANGUAGE, BLOCK_ITEM_LANGUAGE);
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelEvent.RegisterLoaders event) {
    event.register(TConstruct.getResource("gui"), UniqueGuiModel.LOADER);
  }

  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    Font unicode = unicodeFontRender();
    TinkerBook.MATERIALS_AND_YOU.fontRenderer = unicode;
    TinkerBook.TINKERS_GADGETRY.fontRenderer = unicode;
    TinkerBook.PUNY_SMELTING.fontRenderer = unicode;
    TinkerBook.MIGHTY_SMELTING.fontRenderer = unicode;
    TinkerBook.FANTASTIC_FOUNDRY.fontRenderer = unicode;
    TinkerBook.ENCYCLOPEDIA.fontRenderer = unicode;
  }

  @SubscribeEvent
  static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpecial(TinkerCommons.fluidParticle.get(), new FluidParticle.Factory());
  }

  @SubscribeEvent
  static void registerBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
    // Coloured clear glass bakes its colour into the connected-model quads (the plain cube_all model has no tintindex),
    // so break/terrain particles — which tint via BlockColors.getTintSource(state, 0) — rendered grey/white. Register a
    // constant tint at index 0 per glass so the particles pick up the colour; the block model has no tintindex-0 face,
    // so this does not affect the block's own rendering.
    for (GlassColor color : GlassColor.values()) {
      int rgb = color.getColor() & 0xFFFFFF;
      BlockTintSource source = state -> rgb;
      event.register(List.of(source), TinkerCommons.clearStainedGlass.get(color), TinkerCommons.clearStainedGlassPane.get(color));
    }
  }

  private static Font unicodeRenderer;

  /** Gets the unicode font renderer */
  public static Font unicodeFontRender() {
    if (unicodeRenderer == null)
      unicodeRenderer = Minecraft.getInstance().font;

    return unicodeRenderer;
  }

  /** Minecraft 26.1 no longer makes BlockItem names use the block translation key. */
  private static final class BlockItemLanguage extends Language {
    private static final String ITEM_PREFIX = "item.modernfoundry.";
    private static final String BLOCK_PREFIX = "block.modernfoundry.";
    private final Language delegate;

    private BlockItemLanguage(Language delegate) {
      this.delegate = delegate instanceof BlockItemLanguage wrapped ? wrapped.delegate : delegate;
    }

    @Override
    public String getOrDefault(String key, String defaultValue) {
      if (!delegate.has(key) && key.startsWith(ITEM_PREFIX)) {
        String blockKey = BLOCK_PREFIX + key.substring(ITEM_PREFIX.length());
        if (delegate.has(blockKey)) {
          return delegate.getOrDefault(blockKey, defaultValue);
        }
      }
      return delegate.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean has(String key) {
      return delegate.has(key) || key.startsWith(ITEM_PREFIX) && delegate.has(BLOCK_PREFIX + key.substring(ITEM_PREFIX.length()));
    }

    @Override
    public boolean isDefaultRightToLeft() {
      return delegate.isDefaultRightToLeft();
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText text) {
      return delegate.getVisualOrder(text);
    }
  }
}
