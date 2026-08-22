package modernmods.modernfoundry.thinking.common.modifer.durability;

import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.library.ModifierUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import modernmods.hilt.client.TooltipKey;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ModifyDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.behavior.ToolDamageModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.combat.MonsterMeleeHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.mining.BlockBreakModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.LauncherHitModifierHook;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.context.ToolAttackContext;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;

import java.util.List;
import java.util.Objects;

public class ReverseModifier extends NoLevelsModifier implements ToolDamageModifierHook, DurabilityDisplayModifierHook, ModifierRemovalHook, TooltipModifierHook, MeleeHitModifierHook, MonsterMeleeHitModifierHook.RedirectAfter, BlockBreakModifierHook, LauncherHitModifierHook, ProjectileLaunchModifierHook, ModifyDamageModifierHook, ModifierUtils {
    private static final Component n1 = TConstruct.makeTranslation("modifier", "reverse.1");
    private static final Component n2 = TConstruct.makeTranslation("modifier", "reverse.2");
    @Override
    public int getPriority() {
        return 225;
    }
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        hookBuilder.addHook(this, ModifierHooks.TOOL_DAMAGE,ModifierHooks.DURABILITY_DISPLAY,ModifierHooks.TOOLTIP,ModifierHooks.MELEE_HIT,ModifierHooks.MONSTER_MELEE_HIT,ModifierHooks.BLOCK_BREAK,ModifierHooks.LAUNCHER_HIT,ModifierHooks.PROJECTILE_LAUNCH,ModifierHooks.MODIFY_DAMAGE);
    }
    @Override
    public void afterMeleeHit(@NotNull IToolStackView tool, @NotNull ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!context.isExtraAttack()){
            change(tool);
        }
    }
    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (context.isEffective()) {
            change(tool);
        }
    }
    @Override
    public void onLauncherHitEntity(IToolStackView tool, ModifierEntry modifier, Projectile projectile, LivingEntity attacker, Entity target, @Nullable LivingEntity livingTarget, float damageDealt) {
        change(tool);
    }
    @Override
    public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
        if (primary) {
            change(tool);
            ModDataNBT data = PersistentDataCapability.getOrWarn(projectile);
            data.putBoolean(reverse_key, reverse(tool));
        }
    }
    @Override
    public float modifyDamageTaken(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, net.minecraft.world.entity.EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
       if(tool.hasTag((TinkerTags.Items.ARMOR))){
           change(tool);
       }
        return amount;
    }
    @Override
    public Component onRemoved(IToolStackView tool, Modifier modifier) {
        if (tool.getModifierLevel(this.getId()) == 0) {
            tool.getPersistentData().remove(reverse_key);
        }
        return n1;
    }
    @Override
    public int onDamageTool(IToolStackView tool, ModifierEntry modifier, int amount, @Nullable LivingEntity holder) {
        if (reverse(tool)){
            amount *= 2 ;
        }else amount = 0;
        return amount;
    }

    @Nullable
    @Override
    public Boolean showDurabilityBar(IToolStackView tool, ModifierEntry modifier) {
        return true;
    }
    @Override
    public int getDurabilityWidth(IToolStackView tool, ModifierEntry modifier) {
        return 0;
    }
    @Override
    public int getDurabilityRGB(IToolStackView tool, ModifierEntry modifier) {
        return tool.getPersistentData().getBoolean(reverse_key) ? 0x5555FF : 0xFF5555;
    }
    @Override
    public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey key, TooltipFlag tooltipFlag) {
        ModDataNBT persistentData = Objects.requireNonNull(tool.getPersistentData());
        tooltip.add(applyStyle((MutableComponent) (persistentData.getBoolean(reverse_key)?n2:n1)));
    }
}
