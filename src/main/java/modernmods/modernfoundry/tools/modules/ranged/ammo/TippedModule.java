package modernmods.modernfoundry.tools.modules.ranged.ammo;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import modernmods.modernfoundry.compat.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.EntityHitResult;
import modernmods.mantle.client.TooltipKey;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.loadable.record.SingletonLoader;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.build.ModifierRemovalHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DisplayNameModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.TooltipModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IModDataView;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ModifierNBT;
import modernmods.modernfoundry.library.utils.RomanNumeralHelper;

import javax.annotation.Nullable;
import java.util.List;

/** Module allowing arrows to be tipped, applying their effect to the target */
public enum TippedModule implements ModifierModule, ProjectileLaunchModifierHook.NoShooter, ProjectileHitModifierHook, ModifierRemovalHook, DisplayNameModifierHook, TooltipModifierHook {
  INSTANCE;

  private static final String FORMAT = TConstruct.makeTranslationKey("modifier", "tipped.format");
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<TippedModule>defaultHooks(ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_THROWN, ModifierHooks.PROJECTILE_HIT, ModifierHooks.DISPLAY_NAME, ModifierHooks.TOOLTIP, ModifierHooks.REMOVE);
  public static final RecordLoadable<TippedModule> LOADER = new SingletonLoader<>(INSTANCE);

  @Override
  public RecordLoadable<TippedModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }


  /* Data */

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    Identifier key = modifier.getId().getIdentifier();
    IModDataView toolData = tool.getPersistentData();
    if (toolData.contains(key)) {
      persistentData.putString(key, toolData.getString(key));
    }
  }

  @Nullable
  @Override
  public Component onRemoved(IToolStackView tool, Modifier modifier) {
    tool.getPersistentData().remove(modifier.getId().getIdentifier());
    return null;
  }


  /* Effects */

  /** Gets the divisor for the duration */
  private static int getDivisor(ModifierEntry modifier) {
    return 1 << Math.max(4 - modifier.intEffectiveLevel(), 0);
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target) {
    Identifier key = modifier.getId().getIdentifier();
    if (target != null && persistentData.contains(key)) {
      Identifier id = Identifier.tryParse(persistentData.getString(key));
      if (id != null) {
        Entity source = projectile.getEffectSource();
        int divisor = getDivisor(modifier);
        int oldHurtTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        // not a problem if the ID is invalid, will just do nothing
        for (MobEffectInstance instance : BuiltInRegistries.POTION.get(id).<Holder<Potion>>map(holder -> holder).orElse(Potions.WATER).value().getEffects()) {
          MobEffect effect = instance.getEffect().value();
          if (effect.isInstantenous()) {
            effect.applyInstantenousEffect((ServerLevel) projectile.level(), projectile, projectile.getOwner(), target, instance.getAmplifier(), 1f / (divisor * 0.75f));
          } else {
            target.addEffect(new MobEffectInstance(instance.getEffect(), Math.max(instance.mapDuration(i -> i / divisor), 1), instance.getAmplifier(), instance.isAmbient(), instance.isVisible()), source);
          }
        }
        target.invulnerableTime = oldHurtTime;
      }
    }
    return false;
  }


  /* Display */

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    Identifier key = modifier.getId().getIdentifier();
    IModDataView toolData = tool.getPersistentData();
    if (toolData.contains(key)) {
      Identifier id = Identifier.tryParse(toolData.getString(key));
      if (id != null) {
        Holder<Potion> potion = BuiltInRegistries.POTION.get(id).<Holder<Potion>>map(holder -> holder).orElse(Potions.WATER);
        if (potion != Potions.WATER) {
          PotionUtils.getColor(potion);
          PotionUtils.addPotionTooltip(potion.value().getEffects(), tooltip, 1f / getDivisor(modifier));
        }
      }
    }
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    Identifier key = entry.getId().getIdentifier();
    IModDataView toolData = tool.getPersistentData();
    if (toolData.contains(key)) {
      Identifier id = Identifier.tryParse(toolData.getString(key));
      if (id != null) {
        Holder<Potion> potion = BuiltInRegistries.POTION.get(id).<Holder<Potion>>map(holder -> holder).orElse(Potions.WATER);
        if (potion != Potions.WATER) {
          // formats as Tipped <level> (<potion>)
          return Component.translatable(FORMAT,
            RomanNumeralHelper.getNumeral(entry.getLevel()),
            new PotionContents(potion).getName("item.minecraft.potion.effect.")
          ).withStyle(style -> style.withColor(PotionUtils.getColor(potion)));
        }
      }
    }
    return name;
  }
}
