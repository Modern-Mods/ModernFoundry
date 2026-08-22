package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerEffects;

import java.util.List;
import java.util.Optional;

public record RetransitModule(LevelingValue amount) implements ModifierModule, ModifyDamageModifierHook, ModifierUtils {
    private static final List<ModuleHook<?>> DEFAULT_HOOKS;
    public static final RecordLoadable<RetransitModule> LOADER;

    public @NotNull RecordLoadable<RetransitModule> getLoader() {
        return LOADER;
    }

    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }

    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, net.minecraft.world.entity.EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity living = context.getEntity();
        Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
        dataCap.ifPresent(data -> {
            int x = (int)amount().eachLevel() * data.get(ModDataKeys.Retransit, 0) *20;
            if (reverse(tool)&&!living.hasEffect(TinkerEffects.holder(TinkerEffects.returning))) {
                addEffect(living,TinkerEffects.returning.get(),x);
            }
            if (!reverse(tool)&&!living.hasEffect(ModEffects.holder(ModEffects.reminiscence))) {
                addEffect(living,ModEffects.reminiscence.get(),x);
            }
        });
        return amount;
    }
    public LevelingValue amount() {
        return this.amount;
    }
    static {
        DEFAULT_HOOKS = HookProvider.defaultHooks(ModifierHooks.MODIFY_DAMAGE);
        LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(RetransitModule::amount), RetransitModule::new);
    }
}
