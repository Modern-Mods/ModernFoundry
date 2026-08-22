package modernmods.modernfoundry.thinking.common.client;

import modernmods.modernfoundry.TConstruct;
import net.minecraft.network.chat.Component;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import modernmods.hilt.client.book.BookLoader;
import modernmods.hilt.client.book.data.BookData;
import modernmods.hilt.client.book.repository.FileRepository;
import modernmods.hilt.client.book.transformer.BookTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.ModifierTagInjectorTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.ToolSectionTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.ToolTagInjectorTransformer;
import modernmods.modernfoundry.library.client.book.sectiontransformer.materials.TierRangeMaterialSectionTransformer;

public class ModBooks extends BookData {
    public static final ResourceLocation FANTASTIC_GADGETRY_ID = TConstruct.getResource("fantastic_gadgetry");
    public static final BookData FANTASTIC_GADGETRY = BookLoader.registerBook(FANTASTIC_GADGETRY_ID,    false, false);
    public static void initBook() {
        BookLoader.registerGsonTypeAdapter(Component.class, new Component.SerializerAdapter(RegistryAccess.EMPTY));
        addStandardData(FANTASTIC_GADGETRY, FANTASTIC_GADGETRY_ID);
    }
    private static void addStandardData(BookData book, ResourceLocation id) {
        book.addRepository(new FileRepository(new ResourceLocation(id.getNamespace(), "book/" + id.getPath())));
        book.addTransformer(BookTransformer.indexTranformer());
        book.addTransformer(TierRangeMaterialSectionTransformer.INSTANCE);
        // padding needs to be last to ensure page counts are right
        book.addTransformer(BookTransformer.paddingTransformer());
    }
    public static BookData getBook() {
        return FANTASTIC_GADGETRY;
    }
}
