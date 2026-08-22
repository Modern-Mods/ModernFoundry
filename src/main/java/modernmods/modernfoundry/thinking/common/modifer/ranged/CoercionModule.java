package modernmods.modernfoundry.thinking.common.modifer.ranged;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileShootModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

import java.util.List;

public enum CoercionModule implements ModifierModule, ProjectileShootModifierHook, ModifierUtils {
    INSTANCE;
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<CoercionModule>defaultHooks(ModifierHooks.PROJECTILE_SHOT);
    public static final RecordLoadable<CoercionModule> LOADER = new SingletonLoader<>(INSTANCE);
    public @NotNull RecordLoadable<CoercionModule> getLoader() {
        return LOADER;
    }
    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    @Override
    public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
        if (shooter != null && shooter.hasEffect(MobEffects.INVISIBILITY)) {
            int x = shooter.getEffect(MobEffects.INVISIBILITY).getDuration();
            shooter.removeEffect(MobEffects.INVISIBILITY);
            addEffect(shooter,MobEffects.INVISIBILITY,x/2);
        }
    }
}
