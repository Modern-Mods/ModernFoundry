package modernmods.modernfoundry.integrations.items.modifiers.tool;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.minecraft.core.Holder;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.common.util.Lazy;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ProcessLootModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import modernmods.modernfoundry.integrations.common.TagManager;
import modernmods.modernfoundry.integrations.data.integration.ModIntegration;

import static modernmods.modernfoundry.integrations.util.TagHelper.getTag;

/*
 * Pretty much a copy of:
 * https://github.com/SlimeKnights/TinkersConstruct/blob/1.18.2/src/main/java/slimeknights/tconstruct/tools/modifiers/traits/general/TastyModifier.java
 */

public class CheesyModifier extends Modifier implements GeneralInteractionModifierHook, ProcessLootModifierHook {

    private static final Lazy<ItemStack> CHEESE_STACK = Lazy.of(() -> new ItemStack(getCheese()));

    private static ItemLike getCheese() {
        List<Holder<Item>> cheeses = new ArrayList<>();
        getTag(TagManager.Items.CHEESE).forEach(cheeses::add);

        if (!cheeses.isEmpty()) {
            return cheeses.get(RandomSource.create().nextInt(cheeses.size())).value();
        }

        return ItemStack.EMPTY.getItem();
    }

    @Override
    protected void registerHooks(Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.GENERAL_INTERACT, ModifierHooks.PROCESS_LOOT);
    }

    @Override
    public InteractionResult onToolUse(IToolStackView tool, ModifierEntry modifier, Player player, InteractionHand hand, InteractionSource source) {
        if (source == InteractionSource.RIGHT_CLICK && !tool.isBroken() && player.canEat(false)) {
            GeneralInteractionModifierHook.startUsing(tool, modifier.getId(), player, hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onFinishUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
        if (!tool.isBroken() && entity instanceof Player player && player.canEat(false)) {
            // eat the food
            int level = modifier.getLevel();
            Level world = entity.level();
            player.getFoodData().eat(level, level * 0.1F);
            ModifierUtil.foodConsumer.onConsume(player, CHEESE_STACK.get(), level, level * 0.1F);
            player.awardStat(Stats.ITEM_USED.get(tool.getItem()));
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0F,
                    1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.4F);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_BURP, SoundSource.NEUTRAL, 0.5F,
                    world.random.nextFloat() * 0.1F + 0.9F);

            // 15 damage for a bite per level, does not process reinforced/overslime, your teeth are tough
            if (ToolDamageUtil.directDamage(tool, 15 * level, player, player.getUseItem())) {
                player.onEquippedItemBroken(player.getUseItem().getItem(), LivingEntity.getSlotForHand(player.getUsedItemHand()));
            }
        }
    }

    @Override
    public UseAnim getUseAction(IToolStackView tool, ModifierEntry modifier) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(IToolStackView tool, ModifierEntry modifier) {
        return 16;
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot, LootContext context) {
        // if no damage source, probably not a mob
        // otherwise blocks breaking (where THIS_ENTITY is the player) start dropping cheese
        if (!context.hasParam(LootContextParams.DAMAGE_SOURCE)) {
            return;
        }

        // must have an entity
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity != null && entity.getType().is(TagManager.EntityTypes.MILK_PRODUCER)) {
            // at cheesy 1, 2, 3, and 4 its a 2%, 4.15%, 6.25%, 8% per level
            Integer lootingLevel = context.getParamOrNull(LootContextParams.ENCHANTMENT_LEVEL);
            int looting = lootingLevel == null ? 0 : lootingLevel;
            if (RANDOM.nextInt(48 / modifier.intEffectiveLevel()) <= looting) {
                // cheese
                generatedLoot.add(new ItemStack(ModIntegration.BEYOND_EARTH_CHEESE));
            }
        }
    }

}
