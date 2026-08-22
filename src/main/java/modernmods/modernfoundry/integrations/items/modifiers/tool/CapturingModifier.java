package modernmods.modernfoundry.integrations.items.modifiers.tool;

import java.util.List;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ProcessLootModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class CapturingModifier extends Modifier implements ProcessLootModifierHook {

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public void processLoot(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, @NotNull List<ItemStack> generatedLoot, @NotNull LootContext context) {
        if (!context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
            return;
        }

        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

        if (entity != null) {
            float level = modifier.getEffectiveLevel();

            Object bannedMobs = OptionalIntegrationHelper.unwrap(OptionalIntegrationHelper.staticField(
                    "dev.shadowsoffire.apotheosis.spawn.SpawnerModule", "bannedMobs"));
            if (bannedMobs instanceof Collection<?> banned && banned.contains(EntityType.getKey(entity.getType()))) return;

            if (entity.level().random.nextFloat() < level / 250F) {
                Item eggItem = SpawnEggItem.byId(entity.getType());

                if (eggItem == null) return;

                ItemStack egg = new ItemStack(eggItem);

                generatedLoot.add(egg);
            }
        }
    }

}

