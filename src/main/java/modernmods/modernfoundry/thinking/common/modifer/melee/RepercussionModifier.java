package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolAttackUtil;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

import java.util.Objects;

public class RepercussionModifier extends Modifier implements MeleeDamageModifierHook, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, ProjectileHitModifierHook, ModifierUtils {
   private static boolean repercussion = true;
    @Override
    public int getPriority() {
        return 230;
    }
    //Before Reverse
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_DAMAGE, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_HIT);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity target = context.getLivingTarget();
        LivingEntity attacker = context.getAttacker();
        if (!context.isExtraAttack() && context.isFullyCharged() && target != null) {
            if (reverse(tool) && repercussion){
                ToolAttackContext.Builder builder = ToolAttackContext.attacker(attacker).target(target).hand(context.getHand()).cooldown(1);
                if (context.getHand() == InteractionHand.MAIN_HAND) {
                    builder.applyAttributes();
                    builder.baseKnockback(0.4f);
                } else {
                    builder.toolAttributes(tool);
                }
                ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
                int x =target.invulnerableTime;
                target.invulnerableTime = 0;
                repercussion = false;
                ToolAttackUtil.performAttack(tool, builder.build());
                repercussion = true;
                target.invulnerableTime = x;
                persistentData.remove(reverse_key);
                if (attacker instanceof Player player) player.causeFoodExhaustion(0.1F);
                ToolDamageUtil.damageAnimated(tool, 1, attacker);
            }
        }
        return damage;
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isExtraAttack() && !reverse(tool)&& context.getTarget().isAlive()) {
            addEffect(context.getAttacker(), ModEffects.disintegration.get(), 200 / modifier.getLevel(), 2);
        }
    }
}
