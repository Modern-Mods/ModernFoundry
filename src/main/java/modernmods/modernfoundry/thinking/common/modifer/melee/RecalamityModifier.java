package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.shared.TinkerEffects;

import java.util.List;

public class RecalamityModifier extends Modifier implements MeleeDamageModifierHook, ProjectileHitModifierHook, ModifierUtils {
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.MELEE_DAMAGE, ModifierHooks.PROJECTILE_HIT);
    }
    private static final List<Holder<MobEffect>> EFFECTS = List.of(
            MobEffects.POISON, MobEffects.WITHER, TinkerEffects.bleeding, ModEffects.disintegration
    );
    private float calamity(LivingEntity target,LivingEntity attacker,boolean a,float damage,int l){
        Holder<MobEffect> effect = ModEffects.disintegration;
        int x = 0;
        if (a) {
            switch (RANDOM.nextInt(4)) {
                case 1 -> effect = MobEffects.POISON;
                case 2 -> effect = MobEffects.WITHER;
                case 3 -> effect = TinkerEffects.bleeding;
            }
            MobEffectInstance current = target.getEffect(effect);
            target.addEffect(new MobEffectInstance(effect, current == null ? 80*l : current.getDuration() + 80*l));
            target.setLastHurtMob(attacker);
        }else for (Holder<MobEffect> i : EFFECTS) {
            x += target.hasEffect(i) ? 1 : 0;
        }
        return (float) (damage * (1 + 0.1 * x));
    }
    @Override
    public float getMeleeDamage(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float baseDamage, float damage) {
        LivingEntity target = context.getLivingTarget();
        if (!context.isExtraAttack() && context.isFullyCharged()&&target!=null&&target.isAlive()) {
            calamity(target,context.getAttacker(),reverse(tool),damage,modifier.getLevel());
        }
        return damage;
    }
    @Override
    public boolean onProjectileHitEntity(@NotNull ModifierNBT modifiers, ModDataNBT persistentData, @NotNull ModifierEntry modifier, @NotNull Projectile projectile, EntityHitResult hit, @javax.annotation.Nullable LivingEntity attacker, @javax.annotation.Nullable LivingEntity target) {
        if (target != null) {
            float damage=1;
            setPower(projectile,calamity(target,attacker, reverseProjectile(projectile),damage,modifier.getLevel()));
        }
        return false;
    }
}
