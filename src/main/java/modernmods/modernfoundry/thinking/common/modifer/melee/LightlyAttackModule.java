package modernmods.modernfoundry.thinking.common.modifer.melee;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStats;

import java.util.List;

public record LightlyAttackModule(LevelingValue amount) implements ModifierModule, MeleeHitModifierHook, ConditionalStatModifierHook, ModifierUtils {
    private static final Component Boost = TConstruct.makeTranslation("modifier", "lightly_attack.draw_speed");
    private static final List<ModuleHook<?>> DEFAULT_HOOKS;
    public static final RecordLoadable<LightlyAttackModule> LOADER;

    public @NotNull RecordLoadable<LightlyAttackModule> getLoader() {
        return LOADER;
    }

    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    private boolean isEmpty(LivingEntity living, int stack){
        return living.getSlot(stack).get().isEmpty();
    }
    private boolean isAllEmpty(LivingEntity living){
        int x = 0;
        for (int i=0;i<9;i++){
            if (!isEmpty(living,i)) {
                x++;
            }
        }
        return x < 2;
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isExtraAttack()) {
            if (isAllEmpty(context.getAttacker())) {
                addEffect(context.getAttacker(),ModEffects.quick_attack.get(), 60, modifier.getLevel()*3-1);
            }
        }
    }
    @Override
    public float modifyStat(IToolStackView tool, ModifierEntry modifier, LivingEntity living, FloatToolStat stat, float baseValue, float multiplier) {
          if (isAllEmpty(living)) {
              if (stat == ToolStats.DRAW_SPEED) {
                  return baseValue * (1 + (amount.eachLevel() * modifier.getLevel()));
              }
          }
        return baseValue;
    }
    public LevelingValue amount() {
        return this.amount;
    }
    static {
        DEFAULT_HOOKS = HookProvider.defaultHooks(ModifierHooks.MELEE_HIT,ModifierHooks.CONDITIONAL_STAT);
        LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(LightlyAttackModule::amount),LightlyAttackModule::new);
    }
}
