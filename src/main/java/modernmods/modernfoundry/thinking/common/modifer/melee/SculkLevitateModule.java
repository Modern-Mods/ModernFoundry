package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

public record SculkLevitateModule(LevelingValue amount) implements ModifierModule, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, ModifierUtils {
    private static final List<ModuleHook<?>> DEFAULT_HOOKS;
    public static final RecordLoadable<SculkLevitateModule> LOADER;

    public @NotNull RecordLoadable<SculkLevitateModule> getLoader() {
        return LOADER;
    }

    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isExtraAttack() && context.isFullyCharged()) {
            LivingEntity attacker = context.getAttacker();
            LivingEntity target = context.getLivingTarget();
            if (attacker.hasEffect(ModEffects.holder(ModEffects.sculk_power))&&target!=null&&!target.hasEffect(ModEffects.holder(ModEffects.modifier_immune))) {
                addEffect(target,ModEffects.weightless.get(),120,2);
                addEffect(target,ModEffects.modifier_immune.get(),(int) (amount.eachLevel()/modifier.getLevel()));
          }
        }
    }
    public LevelingValue amount() {
        return this.amount;
    }
    static {
        DEFAULT_HOOKS = HookProvider.defaultHooks(ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT);
        LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(SculkLevitateModule::amount), SculkLevitateModule::new);
    }
}
