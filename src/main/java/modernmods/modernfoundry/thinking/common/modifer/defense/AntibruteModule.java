package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.common.register.ModEffects;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Optional;

public enum AntibruteModule implements ModifierModule, ModifyDamageModifierHook, ModifierUtils {
    INSTANCE;
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<AntibruteModule>defaultHooks(ModifierHooks.MODIFY_HURT);
    public static final RecordLoadable<AntibruteModule> LOADER = new SingletonLoader<>(INSTANCE);
    public @NotNull RecordLoadable<AntibruteModule> getLoader() {
        return LOADER;
    }
    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
        LivingEntity living = context.getEntity();
        float x = living.getHealth()/2;
        if (!source.is(DamageTypeTags.BYPASSES_RESISTANCE)&&!context.getEntity().hasEffect(ModEffects.holder(ModEffects.antibrute_cooldown))&&amount>x) {
            Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
            dataCap.ifPresent(data -> addEffect(context.getEntity(), ModEffects.antibrute_cooldown.get(), 240 / data.get(ModDataKeys.Antibrute, 1)));
            block(living);
            return x;
        }
        return amount;
    }
}
