package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import modernmods.modernfoundry.thinking.data.ModDataKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.library.events.teleport.SlingModifierTeleportEvent;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.DamageBlockModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.tools.TinkerModifiers;

import java.util.List;
import java.util.Optional;

public enum RemisdirectionModule implements ModifierModule, DamageBlockModifierHook, ModifierUtils {
    INSTANCE;
    private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<RemisdirectionModule>defaultHooks(ModifierHooks.DAMAGE_BLOCK);
    public static final RecordLoadable<RemisdirectionModule> LOADER = new SingletonLoader<>(INSTANCE);
    public @NotNull RecordLoadable<RemisdirectionModule> getLoader() {
        return LOADER;
    }
    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    @Override
    public boolean isDamageBlocked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount) {
        LivingEntity living = context.getEntity();
        if (source.getEntity() != null && !living.hasEffect(modernmods.modernfoundry.shared.TinkerEffects.holder(TinkerModifiers.teleportCooldownEffect)) && reverse(tool)){
            Vec3 look = living.getLookAngle();
            Level level = context.getLevel();
            double offX = look.x * 5;
            double offZ = look.y * 5;
            BlockPos furthestPos = null;
            // find teleport target
            while (Math.abs(offX) > .5 || Math.abs(offZ) > .5) { // while not too close to player
                BlockPos posAttempt = BlockPos.containing(living.getX() + offX, living.getY(), living.getZ() + offZ);
                // if we do not have a position yet, see if this one is valid
                if (furthestPos == null) {
                    if (level.getWorldBorder().isWithinBounds(posAttempt) && !level.getBlockState(posAttempt).isSuffocating(level, posAttempt)) {
                        furthestPos = posAttempt;
                    }
                } else {
                    // if we already have a position, clear if the new one is unbreakable
                    if (level.getBlockState(posAttempt).getDestroySpeed(level, posAttempt) == -1) {
                        furthestPos = null;
                    }
                }
                // update for next iteration
                offX -= (Math.abs(offX) > .25 ? (offX >= 0 ? 1 : -1) * .25 : 0);
                offZ -= (Math.abs(offZ) > .25 ? (offZ >= 0 ? 1 : -1) * .25 : 0);
            }
            if (furthestPos != null) {
                SlingModifierTeleportEvent event = new SlingModifierTeleportEvent(living, furthestPos.getX() + 0.5f, furthestPos.getY(), furthestPos.getZ() + 0.5f, tool, modifier);
                NeoForge.EVENT_BUS.post(event);
                if (!event.isCanceled()) {
                    block(living);
                    Optional<TinkerDataCapability.Holder> dataCap = TinkerDataCapability.getCapability(living).resolve();
                    dataCap.ifPresent(data -> addEffect(living, TinkerModifiers.teleportCooldownEffect.get(), Mth.clamp((int) amount / data.get(ModDataKeys.Remisdirection, 1), 10, 60) * 20));
                    living.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                    return true;
                }
            }
        }
        return false;
    }
}
