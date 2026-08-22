package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

public enum SculkStruggleModule implements ModifierModule, MeleeHitModifierHook, ModifierUtils {
    INSTANCE;
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<SculkStruggleModule>defaultHooks(ModifierHooks.MELEE_HIT);
    public static final RecordLoadable<SculkStruggleModule> LOADER = new SingletonLoader<>(INSTANCE);
    public @NotNull RecordLoadable<SculkStruggleModule> getLoader() {
        return LOADER;
    }
    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity attacker = context.getAttacker();
        LivingEntity living = context.getLivingTarget();
        if (!context.isExtraAttack()&&attacker.hasEffect(ModEffects.holder(ModEffects.sculk_power))&&living!=null&&living.isDeadOrDying()) {
            int x = 0;
            int y = 0;
            if (attacker.hasEffect(ModEffects.holder(ModEffects.last_effort))){
                x = attacker.getEffect(ModEffects.holder(ModEffects.last_effort)).getDuration();
                y = attacker.getEffect(ModEffects.holder(ModEffects.last_effort)).getAmplifier();
            }
            addEffect(attacker,ModEffects.last_effort.get(),100 * modifier.getLevel()+x,y);
        }
    }
}
