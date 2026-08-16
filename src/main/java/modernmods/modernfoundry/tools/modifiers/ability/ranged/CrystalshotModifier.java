package modernmods.modernfoundry.tools.modifiers.ability.ranged;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.client.ResourceColorManager;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.modifiers.modules.behavior.InfinityModule;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.TinkerTools;
import modernmods.modernfoundry.tools.item.CrystalshotItem;

import javax.annotation.Nullable;

/** @deprecated use {@link InfinityModule} and {@link modernmods.modernfoundry.library.modifiers.modules.display.ModifierVariantColorModule} */
@Deprecated(forRemoval = true)
public class CrystalshotModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(new InfinityModule(new ItemStack(TinkerTools.crystalshotItem), CrystalshotItem.TAG_VARIANT,4, false));
  }

  @Override
  public int getPriority() {
    return 60; // after trick quiver, before bulk quiver, can't go after bulk due to desire to use inventory
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, @Nullable RegistryAccess access) {
    // color the display name for the variant
    String variant = tool.getPersistentData().getString(getId());
    if (!variant.isEmpty()) {
      String key = getTranslationKey();
      return Component.translatable(getTranslationKey())
        .withStyle(style -> style.withColor(ResourceColorManager.getTextColor(key + "." + variant)));
    }
    return super.getDisplayName();
  }
}
