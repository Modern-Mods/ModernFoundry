package modernmods.modernfoundry.library.modifiers.modules.behavior;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.common.ItemStackLoadable;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.BlockItemProviderModifierHook;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A module that uses {@link modernmods.modernfoundry.library.tools.capability.BlockItemProviderCapability BlockItemProviderCapability} via {@link BlockItemProviderModifierHook} to provide BlockItems to modifiers like exchanging at the cost of durability.
 * Note this does not let the tool place blocks, it only exposes this capability. See {@link modernmods.modernfoundry.tools.modules.interaction.PlaceGlowModule PlaceGlowModule} for an example of a custom module that lets the tool place blocks.
 * @param item The BlockItem to provide, wrapped in an ItemStack
 * @param damage The amount of damage it takes to provide one block (can be 0)
 * @param condition Other conditions that you might want to condition the providing on, such as only happening on certain tool types.
 */
public record BlockItemProviderModule(ItemStack item, int damage, ModifierCondition<IToolStackView> condition) implements ModifierModule, BlockItemProviderModifierHook, ModifierCondition.ConditionalModule<IToolStackView> {
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = List.of(ModifierHooks.BLOCK_ITEM_PROVIDER);
    public static final RecordLoadable<BlockItemProviderModule> LOADER = RecordLoadable.create(
      ItemStackLoadable.REQUIRED_ITEM_NBT.validate((stack, error) -> {
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
          return stack;
        }
        throw error.create(String.format("Expected item %s to be instance of BlockItem, but was %s instead", BuiltInRegistries.ITEM.getKey(item), item.getClass().getName()));
      }).requiredField("item", BlockItemProviderModule::item),
      IntLoadable.FROM_ZERO.defaultField("tool_damage", 1, BlockItemProviderModule::damage),
      ModifierCondition.TOOL_FIELD,
      BlockItemProviderModule::new);

    @Override
    public RecordLoadable<BlockItemProviderModule> getLoader() {
        return LOADER;
    }

    @Override
    public List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public ItemStack getBlockItemStack(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity entity) {
        return !tool.isBroken() && condition.matches(tool, modifier) ? item : ItemStack.EMPTY;
    }

    @Override
    public boolean consumeBlockItem(IToolStackView tool, ModifierEntry modifier, ItemStack backingStack, @Nullable LivingEntity entity) {
        // if this is not our item, then we did not provide it so we should avoid consuming
        if (item != backingStack) return false;

        // we did provide it, so damage and show animation if possible
        if (damage > 0) {
            ToolDamageUtil.damageAnimated(tool, damage, entity, modifier.getId());
        }
        return true;
    }
}
