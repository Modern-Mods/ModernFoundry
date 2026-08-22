package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.thinking.data.ModModifierIds;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerEffects;

import java.util.Objects;

public class FallingAttackModifier extends Modifier implements MeleeDamageModifierHook, MonsterMeleeHitModifierHook.RedirectAfter{
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.MONSTER_MELEE_DAMAGE);
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        float h = context.getAttacker().fallDistance;
        int p =3+tool.getModifierLevel(ModModifierIds.DensityAdvanced);
        float x = (float) (Objects.requireNonNull(context.getAttacker().getAttribute(Attributes.GRAVITY)).getValue()/0.08);
        if (!context.isExtraAttack() && context.isFullyCharged()&&x>0) {
            damage += (float) ((Math.min(6,2*h)+Math.min(8,h)+(1+0.5*p)*h)*x);
            context.getAttacker().resetFallDistance();
            if (context.getAttacker().isFallFlying()) {
                TinkerEffects.repulsive.get().apply(context.getAttacker(), 50, h > 5 ? 3 : 1);
            }
        }
        return damage;
    }
}
