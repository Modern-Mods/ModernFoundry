package modernmods.modernfoundry.library.client.armor.texture;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.conditions.ICondition;
import modernmods.mantle.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import modernmods.modernfoundry.library.utils.Util;

/**
 * Datagen helper for making conditional {@link ArmorTextureSupplier}.
 * @param ifTrue      Supplier to use if all conditions are true.
 * @param ifFalse     Supplier to use if any condition is false. Defaults to {@link ArmorTextureSupplier#EMPTY}
 * @param conditions  Conditions to evaluate.
 */
@SuppressWarnings("unused") // API
public record ConditionalArmorTextureSupplier(ArmorTextureSupplier ifTrue, ArmorTextureSupplier ifFalse, ICondition... conditions) implements ArmorTextureSupplier, ConditionalObject<ArmorTextureSupplier> {
  public ConditionalArmorTextureSupplier(ArmorTextureSupplier ifTrue, ICondition... conditions) {
    this(ifTrue, ArmorTextureSupplier.EMPTY, conditions);
  }

  @Override
  public ArmorTexture getArmorTexture(ItemStack stack, TextureType leggings, RegistryAccess access) {
    return (Util.testConditions(conditions) ? ifTrue : ifFalse).getArmorTexture(stack, leggings, access);
  }

  @Override
  public RecordLoadable<? extends IHaveLoader> getLoader() {
    return ArmorTextureSupplier.LOADER.getConditionalLoader();
  }
}
