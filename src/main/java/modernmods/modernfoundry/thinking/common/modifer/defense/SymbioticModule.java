package modernmods.modernfoundry.thinking.common.modifer.defense;

import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

import java.util.List;

import static modernmods.modernfoundry.library.modifiers.Modifier.RANDOM;

public record SymbioticModule(LevelingValue amount) implements ModifierModule, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, ProjectileLaunchModifierHook, ModifyDamageModifierHook, ModifierUtils {
    private static final List<ModuleHook<?>> DEFAULT_HOOKS;
    public static final RecordLoadable<SymbioticModule> LOADER;

    public @NotNull RecordLoadable<SymbioticModule> getLoader() {
        return LOADER;
    }

    public @NotNull List<ModuleHook<?>> getDefaultHooks() {
        return DEFAULT_HOOKS;
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        LivingEntity entity = context.getPlayerAttacker();
        float level = modifier.getEffectiveLevel();
        if (!context.isExtraAttack() && context.isFullyCharged()&&RANDOM.nextFloat() < (level * amount.eachLevel()) && entity!=null&&entity.getHealth()<entity.getMaxHealth()&& !tool.isBroken()) {
            eat(tool, modifier, entity);
        }
    }
    @Override
    public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
        float level = modifier.getEffectiveLevel();
        if (primary&&RANDOM.nextFloat() < (level * amount.eachLevel()) && shooter.getHealth()<shooter.getMaxHealth()&& !tool.isBroken()) {
            eat(tool, modifier, shooter);
        }
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float damage, boolean isDirectDamage) {
        LivingEntity living = context.getEntity();
        float level = modifier.getEffectiveLevel();
        if (RANDOM.nextFloat() < (level * amount.eachLevel()) && living.getHealth() < living.getMaxHealth() && !tool.isBroken() && tool.hasTag(TinkerTags.Items.ARMOR)) {
            eat(tool, modifier, living);
        }
        return damage;
    }
    private void eat(IToolStackView tool, ModifierEntry modifier, LivingEntity entity) {
        if (entity instanceof Player player) {
            // eat the food
            int level = modifier.getLevel();
            heal(player, Math.max(level, (player.getMaxHealth()-player.getHealth()) * 0.3f));
            // take a bit of extra damage to heal
            // 8 damage for a bite per level, does not process reinforced/overslime, your teeth are tough
            if (ToolDamageUtil.directDamage(tool, 8* level, player, player.getUseItem())) {
                player.onEquippedItemBroken(player.getItemInHand(player.getUsedItemHand()).getItem(), LivingEntity.getSlotForHand(player.getUsedItemHand()));
            }
        }
    }
    public LevelingValue amount() {
        return this.amount;
    }
    static {
        DEFAULT_HOOKS = HookProvider.defaultHooks(ModifierHooks.MELEE_HIT, ModifierHooks.MONSTER_MELEE_HIT, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.MODIFY_DAMAGE);
        LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(SymbioticModule::amount), SymbioticModule::new);
    }
}
