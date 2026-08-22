package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.utils.Util;

import java.util.List;
import java.util.Objects;

public class SculkDashModifier extends Modifier implements MeleeHitModifierHook, MeleeDamageModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, ModifierRemovalHook, TooltipModifierHook {
    private static final Component Times = TConstruct.makeTranslation("modifier", "sculk_dash.times");
    private final ResourceLocation KEY = TConstruct.getResource("sculk_dash");
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.MONSTER_MELEE_DAMAGE, ModifierHooks.REMOVE, ModifierHooks.TOOLTIP);
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        Player attacker = context.getPlayerAttacker();
        ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
        if (attacker == null) {
            return;
        }
        if (attacker.hasEffect(ModEffects.holder(ModEffects.sculk_power)) && (context.getLivingTarget()==null||context.getLivingTarget().isDeadOrDying())) {
            persistentData.putInt(this.KEY,persistentData.getInt(KEY)+modifier.getLevel());
            attacker.addEffect(new MobEffectInstance(ModEffects.holder(ModEffects.strength_reset),5,0,true,false));
        }
    }
    @Nullable
    @Override
    public Component onRemoved(IToolStackView tool, Modifier modifier) {
        if (tool.getModifierLevel(this.getId()) == 0) {
            tool.getPersistentData().remove(KEY);
        }
        return null;
    }

    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
        if (context.isFullyCharged()&&!context.isExtraAttack()&&persistentData.contains(KEY,3)&&persistentData.getInt(KEY)>0){
            persistentData.putInt(KEY,persistentData.getInt(KEY)-1);
            damage *= 1.25F;
        }
        return damage;
    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag) {
        ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
        if (player!=null) {
            int x = persistentData.getInt(KEY);
            tooltip.add(applyStyle(Component.literal(Util.COMMA_FORMAT.format(x) + " ").append(Times)));
        }
    }
}
