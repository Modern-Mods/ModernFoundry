package modernmods.modernfoundry.integrations.items.modifiers.traits;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import modernmods.modernfoundry.TConstruct;
public class WaterPowered extends NoLevelsModifier implements ToolDamageModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE);
    }

    @Override
    public int getPriority() {
        return 180;
    }

    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        final Player player = holder instanceof Player ? (Player) holder : null;

        if (player != null && !player.level().isClientSide) {
            final ServerPlayer sp = (ServerPlayer) player;
            boolean isPartialSubmersion = !sp.isUnderWater() && sp.isInWater() && sp.isInWaterRainOrBubble();
            boolean isSubmerged = sp.isUnderWater() && sp.isInWater() && sp.isInWaterRainOrBubble();
            float chance = 0.0F;

            if (isSubmerged) {
                chance = 0.75F;
            }
            else if (isPartialSubmersion) {
                chance = 0.50F;
            }
            else if (sp.isInWaterRainOrBubble()) {
                chance = 0.30F;
            }

            if (chance > 0.0F) {
                int maxDamage = amount;

                for (int i = 0; i < maxDamage; i++) {
                    if (TConstruct.RANDOM.nextFloat() <= chance) {
                        amount--;
                    }
                }
            }
        }

        return amount;
    }

}


