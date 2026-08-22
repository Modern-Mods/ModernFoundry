package modernmods.modernfoundry.thinking.common.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import modernmods.hilt.client.model.NBTKeyModel;
import modernmods.modernfoundry.library.tools.SlotType;

public class ModSlots {
    public static SlotType ANCIENT = SlotType.getOrCreate("ancient");

    public ModSlots(){
    }
    @OnlyIn(Dist.CLIENT)
    public static void init() {
        NBTKeyModel.registerExtraTexture(ResourceLocation.fromNamespaceAndPath("modernfoundry", "creative_slot")
                ,ANCIENT.getName(),ResourceLocation.fromNamespaceAndPath("modernfoundry", "item/ancient_ceramic"));
    }
}
