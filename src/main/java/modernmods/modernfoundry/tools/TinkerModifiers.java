package modernmods.modernfoundry.tools;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import modernmods.hilt.recipe.helper.LoadableRecipeSerializer;
import modernmods.hilt.recipe.helper.SimpleRecipeSerializer;
import modernmods.hilt.registration.object.EnumObject;
import modernmods.hilt.registration.object.ItemObject;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerEffect;
import modernmods.modernfoundry.common.TinkerModule;
import modernmods.modernfoundry.library.json.predicate.modifier.ModifierPredicate;
import modernmods.modernfoundry.library.json.predicate.modifier.SingleModifierPredicate;
import modernmods.modernfoundry.library.json.predicate.modifier.SlotTypeModifierPredicate;
import modernmods.modernfoundry.library.json.predicate.modifier.TagModifierPredicate;
import modernmods.modernfoundry.library.json.variable.block.BlockVariable;
import modernmods.modernfoundry.library.json.variable.block.ConditionalBlockVariable;
import modernmods.modernfoundry.library.json.variable.block.StatePropertyVariable;
import modernmods.modernfoundry.library.json.variable.entity.AttributeEntityVariable;
import modernmods.modernfoundry.library.json.variable.entity.ConditionalEntityVariable;
import modernmods.modernfoundry.library.json.variable.entity.EntityEffectLevelVariable;
import modernmods.modernfoundry.library.json.variable.entity.EntityLightVariable;
import modernmods.modernfoundry.library.json.variable.entity.EntityVariable;
import modernmods.modernfoundry.library.json.variable.entity.EquipmentCountEntityVariable;
import modernmods.modernfoundry.library.json.variable.entity.PlayerStatVariable;
import modernmods.modernfoundry.library.json.variable.melee.EntityMeleeVariable;
import modernmods.modernfoundry.library.json.variable.melee.MeleeVariable;
import modernmods.modernfoundry.library.json.variable.mining.BlockLightVariable;
import modernmods.modernfoundry.library.json.variable.mining.BlockMiningSpeedVariable;
import modernmods.modernfoundry.library.json.variable.mining.BlockTemperatureVariable;
import modernmods.modernfoundry.library.json.variable.mining.EffectiveMiningSpeedVariable;
import modernmods.modernfoundry.library.json.variable.mining.MiningSpeedVariable;
import modernmods.modernfoundry.library.json.variable.power.EntityPowerVariable;
import modernmods.modernfoundry.library.json.variable.power.PersistentDataPowerVariable;
import modernmods.modernfoundry.library.json.variable.power.PowerVariable;
import modernmods.modernfoundry.library.json.variable.protection.EntityProtectionVariable;
import modernmods.modernfoundry.library.json.variable.protection.ProtectionVariable;
import modernmods.modernfoundry.library.json.variable.stat.ConditionalStatVariable;
import modernmods.modernfoundry.library.json.variable.stat.EntityConditionalStatVariable;
import modernmods.modernfoundry.library.json.variable.tool.ConditionalToolVariable;
import modernmods.modernfoundry.library.json.variable.tool.ModDataVariable;
import modernmods.modernfoundry.library.json.variable.tool.ModifierLevelVariable;
import modernmods.modernfoundry.library.json.variable.tool.StatMultiplierVariable;
import modernmods.modernfoundry.library.json.variable.tool.ToolStatVariable;
import modernmods.modernfoundry.library.json.variable.tool.ToolVariable;
import modernmods.modernfoundry.library.modifiers.FakeModifier;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.modifiers.ModifierManager;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffectManager;
import modernmods.modernfoundry.library.modifiers.fluid.block.BlockInteractFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.BreakBlockFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.MeltBlockFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.MobEffectCloudFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.MoveBlocksFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.OffsetBlockFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.PlaceBlockFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.block.PotionCloudFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.AddBreathFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.AwardStatFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.CureEffectsFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.DamageFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.EntityInteractFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.FireFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.FreezeFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.MobEffectFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.PotionFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.PushEntityFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.RandomTeleportFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.RemoveEffectFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.entity.RestoreHungerFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.AlternativesFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.AreaMobEffectFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.ConditionalFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.DropItemFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.ExplosionFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.ScalingFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.SequenceFluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.general.SetBlockFluidEffect;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.AdjustDamageModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.BlockDamageSourceModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.CoverGroundWalkerModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.EffectImmunityModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.MaxArmorAttributeModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.MobDisguiseModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ProtectionModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ReplaceBlockWalkerModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ToolActionWalkerTransformModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.AttributeModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.BlockItemProviderModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ConditionalStatModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.EdibleModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.InfinityModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.MaterialRepairModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ReduceToolDamageModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.RepairModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ShowOffhandModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ToolActionTransformModule;
import modernmods.modernfoundry.library.modifiers.modules.behavior.ToolActionsModule;
import modernmods.modernfoundry.library.modifiers.modules.build.EnchantmentModule;
import modernmods.modernfoundry.library.modifiers.modules.build.ModifierRequirementsModule;
import modernmods.modernfoundry.library.modifiers.modules.build.ModifierSlotModule;
import modernmods.modernfoundry.library.modifiers.modules.build.ModifierTraitModule;
import modernmods.modernfoundry.library.modifiers.modules.build.RarityModule;
import modernmods.modernfoundry.library.modifiers.modules.build.SetStatModule;
import modernmods.modernfoundry.library.modifiers.modules.build.StatBoostModule;
import modernmods.modernfoundry.library.modifiers.modules.build.StatCopyModule;
import modernmods.modernfoundry.library.modifiers.modules.build.SwappableSlotModule;
import modernmods.modernfoundry.library.modifiers.modules.build.SwappableToolTraitsModule;
import modernmods.modernfoundry.library.modifiers.modules.build.VolatileFlagModule;
import modernmods.modernfoundry.library.modifiers.modules.build.VolatileFloatModule;
import modernmods.modernfoundry.library.modifiers.modules.build.VolatileIntModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.CapacityBarModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.DamageToCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.DurabilityShieldModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.EnergyAsCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.LaunchCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.LootToCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.MeleeCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.MiningCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.capacity.TimeToCapacityModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.ConditionalMeleeDamageModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.ConditionalPowerModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.KnockbackModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.LootingModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.MeleeAttributeModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.MobEffectModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.ProjectileExplosionModule;
import modernmods.modernfoundry.library.modifiers.modules.combat.SlingForceModule;
import modernmods.modernfoundry.library.modifiers.modules.display.DurabilityBarColorModule;
import modernmods.modernfoundry.library.modifiers.modules.display.MaterialVariantColorModule;
import modernmods.modernfoundry.library.modifiers.modules.display.ModifierVariantColorModule;
import modernmods.modernfoundry.library.modifiers.modules.display.ModifierVariantNameModule;
import modernmods.modernfoundry.library.modifiers.modules.display.ShowInteractionSourceModule;
import modernmods.modernfoundry.library.modifiers.modules.mining.ConditionalMiningSpeedModule;
import modernmods.modernfoundry.library.modifiers.modules.technical.ArmorLevelModule;
import modernmods.modernfoundry.library.modifiers.modules.technical.ArmorStatModule;
import modernmods.modernfoundry.library.modifiers.modules.technical.MaxArmorStatModule;
import modernmods.modernfoundry.library.modifiers.util.DynamicModifier;
import modernmods.modernfoundry.library.modifiers.util.ModifierDeferredRegister;
import modernmods.modernfoundry.library.modifiers.util.ModifierLevelDisplay;
import modernmods.modernfoundry.library.modifiers.util.StaticModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.recipe.modifiers.ModifierSalvage;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IncrementalModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.ModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.MultilevelIncrementalModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.MultilevelModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.OverslimeCraftingTableRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.OverslimeModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.SwappableModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.severing.AgeableSeveringRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.severing.SeveringRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.repairing.ModifierMaterialRepairKitRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.repairing.ModifierMaterialRepairRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.repairing.ModifierRepairCraftingRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.repairing.ModifierRepairTinkerStationRecipe;
import modernmods.modernfoundry.library.recipe.worktable.ModifierSetWorktableRecipe;
import modernmods.modernfoundry.library.tools.capability.BlockItemProviderCapability;
import modernmods.modernfoundry.library.tools.capability.EntityModifierCapability;
import modernmods.modernfoundry.library.tools.capability.PersistentDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataKeys;
import modernmods.modernfoundry.library.tools.capability.fluid.TankModule;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.capability.inventory.InventoryMenuModule;
import modernmods.modernfoundry.library.tools.capability.inventory.InventoryModule;
import modernmods.modernfoundry.library.tools.capability.inventory.InventorySlotMenuModule;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tables.TinkerTables;
import modernmods.modernfoundry.tools.entity.CustomFireball;
import modernmods.modernfoundry.tools.entity.FluidEffectProjectile;
import modernmods.modernfoundry.tools.item.CreativeSlotItem;
import modernmods.modernfoundry.tools.item.DragonScaleItem;
import modernmods.modernfoundry.tools.item.ModifierCrystalItem;
import modernmods.modernfoundry.tools.modifiers.EnergyHandlerModifier;
import modernmods.modernfoundry.tools.modifiers.ModifierLootModifier;
import modernmods.modernfoundry.tools.modifiers.ability.armor.AmbidextrousModifier;
import modernmods.modernfoundry.tools.modifiers.ability.fluid.BurstingModifier;
import modernmods.modernfoundry.tools.modifiers.ability.fluid.WettingModifier;
import modernmods.modernfoundry.tools.modifiers.ability.tool.DuelWieldingModifier;
import modernmods.modernfoundry.tools.modifiers.ability.tool.ExchangingModifier;
import modernmods.modernfoundry.tools.modifiers.ability.tool.OffhandAttackModifier;
import modernmods.modernfoundry.tools.modifiers.ability.tool.ParryingModifier;
import modernmods.modernfoundry.tools.modifiers.effect.BleedingEffect;
import modernmods.modernfoundry.tools.modifiers.effect.HelmetChargingEffect;
import modernmods.modernfoundry.tools.modifiers.effect.MagneticEffect;
import modernmods.modernfoundry.tools.modifiers.effect.NoMilkEffect;
import modernmods.modernfoundry.tools.modifiers.effect.RepulsiveEffect;
import modernmods.modernfoundry.tools.modifiers.loot.ChrysophiliteBonusFunction;
import modernmods.modernfoundry.tools.modifiers.loot.ChrysophiliteLootCondition;
import modernmods.modernfoundry.tools.modifiers.loot.HasModifierLootCondition;
import modernmods.modernfoundry.tools.modifiers.loot.ModifierBonusLootFunction;
import modernmods.modernfoundry.tools.modifiers.slotless.CreativeSlotModifier;
import modernmods.modernfoundry.tools.modifiers.slotless.OverslimeModifier;
import modernmods.modernfoundry.tools.modifiers.slotless.StatOverrideModifier;
import modernmods.modernfoundry.tools.modifiers.traits.general.EnderportingModifier;
import modernmods.modernfoundry.tools.modifiers.traits.harvest.DwarvenModifier;
import modernmods.modernfoundry.tools.modifiers.traits.harvest.MomentumModifier;
import modernmods.modernfoundry.tools.modifiers.traits.melee.ConductingModifier;
import modernmods.modernfoundry.tools.modifiers.traits.melee.InsatiableModifier;
import modernmods.modernfoundry.tools.modifiers.traits.melee.LaceratingModifier;
import modernmods.modernfoundry.tools.modifiers.traits.ranged.OlympicModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.BreathtakingModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.ChrysophiliteModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.FirebreathModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.GoldGuardModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.PlagueModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.SelfDestructiveModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.StrongBonesModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.WildfireModifier;
import modernmods.modernfoundry.tools.modifiers.traits.skull.WitheredModifier;
import modernmods.modernfoundry.tools.modifiers.upgrades.melee.PiercingModifier;
import modernmods.modernfoundry.tools.modifiers.upgrades.melee.SweepingEdgeModifier;
import modernmods.modernfoundry.tools.modifiers.upgrades.ranged.SinistralModifier;
import modernmods.modernfoundry.tools.modules.AutosmeltModule;
import modernmods.modernfoundry.tools.modules.ClearEffectOnUnequipModule;
import modernmods.modernfoundry.tools.modules.CraftCountModule;
import modernmods.modernfoundry.tools.modules.DamageOnUnequipModule;
import modernmods.modernfoundry.tools.modules.FovModule;
import modernmods.modernfoundry.tools.modules.HeadlightModule;
import modernmods.modernfoundry.tools.modules.MeltingModule;
import modernmods.modernfoundry.tools.modules.OverburnModule;
import modernmods.modernfoundry.tools.modules.OvergrowthModule;
import modernmods.modernfoundry.tools.modules.ReduceEffectOnUnequipModule;
import modernmods.modernfoundry.tools.modules.SmeltingModule;
import modernmods.modernfoundry.tools.modules.TheOneProbeModule;
import modernmods.modernfoundry.tools.modules.ZoomModule;
import modernmods.modernfoundry.tools.modules.armor.DepthProtectionModule;
import modernmods.modernfoundry.tools.modules.armor.EnderclearanceModule;
import modernmods.modernfoundry.tools.modules.armor.FieryCounterModule;
import modernmods.modernfoundry.tools.modules.armor.FireWalkerModule;
import modernmods.modernfoundry.tools.modules.armor.FlameBarrierModule;
import modernmods.modernfoundry.tools.modules.armor.FreezingCounterModule;
import modernmods.modernfoundry.tools.modules.armor.GlowWalkerModule;
import modernmods.modernfoundry.tools.modules.armor.KineticModule;
import modernmods.modernfoundry.tools.modules.armor.KnockbackCounterModule;
import modernmods.modernfoundry.tools.modules.armor.LightspeedAttributeModule;
import modernmods.modernfoundry.tools.modules.armor.MinimapModule;
import modernmods.modernfoundry.tools.modules.armor.OvershieldModule;
import modernmods.modernfoundry.tools.modules.armor.RecurrentProtectionModule;
import modernmods.modernfoundry.tools.modules.armor.RestoreLostHealthModule;
import modernmods.modernfoundry.tools.modules.armor.ShieldStrapModule;
import modernmods.modernfoundry.tools.modules.armor.SleevesModule;
import modernmods.modernfoundry.tools.modules.armor.SoulSpeedModule;
import modernmods.modernfoundry.tools.modules.armor.TeleportDodgeModule;
import modernmods.modernfoundry.tools.modules.armor.ThornsModule;
import modernmods.modernfoundry.tools.modules.armor.ToolBeltModule;
import modernmods.modernfoundry.tools.modules.armor.UpdateHealthModule;
import modernmods.modernfoundry.tools.modules.combat.BlockingModule;
import modernmods.modernfoundry.tools.modules.combat.ChannelingModule;
import modernmods.modernfoundry.tools.modules.combat.DamageOnShootModule;
import modernmods.modernfoundry.tools.modules.combat.FieryArmorAttackModule;
import modernmods.modernfoundry.tools.modules.combat.FieryAttackModule;
import modernmods.modernfoundry.tools.modules.combat.FreezingAttackModule;
import modernmods.modernfoundry.tools.modules.combat.LifestealModule;
import modernmods.modernfoundry.tools.modules.combat.SeveringModule;
import modernmods.modernfoundry.tools.modules.combat.SpillingModule;
import modernmods.modernfoundry.tools.modules.combat.SweepingEdgeModule;
import modernmods.modernfoundry.tools.modules.cosmetic.BannerModule;
import modernmods.modernfoundry.tools.modules.cosmetic.DyeModule;
import modernmods.modernfoundry.tools.modules.cosmetic.EmbellishmentModule;
import modernmods.modernfoundry.tools.modules.cosmetic.TrimModule;
import modernmods.modernfoundry.tools.modules.durability.DurabilityAsCapacityModule;
import modernmods.modernfoundry.tools.modules.durability.ShareDurabilityModule;
import modernmods.modernfoundry.tools.modules.durability.ToolDamageRangeModule;
import modernmods.modernfoundry.tools.modules.interaction.BrushModule;
import modernmods.modernfoundry.tools.modules.interaction.BucketModule;
import modernmods.modernfoundry.tools.modules.interaction.ExtinguishCampfireModule;
import modernmods.modernfoundry.tools.modules.interaction.FireballModule;
import modernmods.modernfoundry.tools.modules.interaction.FishingModule;
import modernmods.modernfoundry.tools.modules.interaction.HarvestModule;
import modernmods.modernfoundry.tools.modules.interaction.PlaceFireModule;
import modernmods.modernfoundry.tools.modules.interaction.PlaceGlowModule;
import modernmods.modernfoundry.tools.modules.interaction.ShearsModule;
import modernmods.modernfoundry.tools.modules.interaction.SlurpingModule;
import modernmods.modernfoundry.tools.modules.interaction.SpittingModule;
import modernmods.modernfoundry.tools.modules.interaction.SplashingModule;
import modernmods.modernfoundry.tools.modules.interaction.TankInteractionModule;
import modernmods.modernfoundry.tools.modules.interaction.ThrowingModule;
import modernmods.modernfoundry.tools.modules.interaction.sling.SlingKnockbackModule;
import modernmods.modernfoundry.tools.modules.interaction.sling.SlingLeapModule;
import modernmods.modernfoundry.tools.modules.interaction.sling.SlingTeleportModule;
import modernmods.modernfoundry.tools.modules.ranged.BulkQuiverModule;
import modernmods.modernfoundry.tools.modules.ranged.RestrictAngleModule;
import modernmods.modernfoundry.tools.modules.ranged.TrickQuiverModule;
import modernmods.modernfoundry.tools.modules.ranged.ammo.ProjectileFuseModule;
import modernmods.modernfoundry.tools.modules.ranged.ammo.ProjectileGravityModule;
import modernmods.modernfoundry.tools.modules.ranged.ammo.SmashingModule;
import modernmods.modernfoundry.tools.modules.ranged.ammo.TippedModule;
import modernmods.modernfoundry.tools.modules.ranged.bow.QuiverInventoryModule;
import modernmods.modernfoundry.tools.modules.ranged.common.ArrowPierceModule;
import modernmods.modernfoundry.tools.modules.ranged.common.ProjectileAttractMobsModule;
import modernmods.modernfoundry.tools.modules.ranged.common.ProjectileBounceModule;
import modernmods.modernfoundry.tools.modules.ranged.common.ProjectilePlaceGlowModule;
import modernmods.modernfoundry.tools.modules.ranged.common.PunchModule;
import modernmods.modernfoundry.tools.modules.ranged.common.ReversePunchModule;
import modernmods.modernfoundry.tools.recipe.ArmorDyeingRecipe;
import modernmods.modernfoundry.tools.recipe.ArmorTrimRecipe;
import modernmods.modernfoundry.tools.recipe.BannerModifierRecipe;
import modernmods.modernfoundry.tools.recipe.EnchantmentConvertingRecipe;
import modernmods.modernfoundry.tools.recipe.ExtractModifierRecipe;
import modernmods.modernfoundry.tools.recipe.ModifierRemovalRecipe;
import modernmods.modernfoundry.tools.recipe.ModifierSortingRecipe;
import modernmods.modernfoundry.tools.recipe.TippedToolTransformRecipe;
import modernmods.modernfoundry.tools.recipe.ToggleInteractionWorktableRecipe;
import modernmods.modernfoundry.tools.recipe.severing.MooshroomDemushroomingRecipe;
import modernmods.modernfoundry.tools.recipe.severing.PlayerBeheadingRecipe;
import modernmods.modernfoundry.tools.recipe.severing.SheepShearingRecipe;
import modernmods.modernfoundry.tools.recipe.severing.SnowGolemBeheadingRecipe;
import modernmods.modernfoundry.tools.stats.ToolType;

import static modernmods.modernfoundry.TConstruct.getResource;

/**
 * Contains modifiers and the items or blocks used to craft modifiers
 */
@SuppressWarnings("unused")
public final class TinkerModifiers extends TinkerModule {
  private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TConstruct.MOD_ID);

  public TinkerModifiers() {
    ModifierManager.INSTANCE.init();
    DynamicModifier.init();
    FluidEffectManager.INSTANCE.init();
    MODIFIERS.register(modernmods.modernfoundry.TConstruct.getModBus());
    TinkerDataKeys.init();
  }

  /*
   * Items
   */
  public static final ItemObject<Item> silkyCloth = ITEMS.register("silky_cloth", ITEM_PROPS);
  public static final ItemObject<Item> dragonScale = ITEMS.register("dragon_scale", () -> new DragonScaleItem(new Item.Properties().rarity(Rarity.RARE)));
  // durability reinforcements
  public static final ItemObject<Item> emeraldReinforcement = ITEMS.register("emerald_reinforcement", ITEM_PROPS);
  public static final ItemObject<Item> slimesteelReinforcement = ITEMS.register("slimesteel_reinforcement", ITEM_PROPS);
  // armor reinforcements
  public static final ItemObject<Item> ironReinforcement = ITEMS.register("iron_reinforcement", ITEM_PROPS);
  public static final ItemObject<Item> searedReinforcement = ITEMS.register("seared_reinforcement", ITEM_PROPS);
  public static final ItemObject<Item> goldReinforcement = ITEMS.register("gold_reinforcement", ITEM_PROPS);
  public static final ItemObject<Item> cobaltReinforcement = ITEMS.register("cobalt_reinforcement", ITEM_PROPS);
  public static final ItemObject<Item> obsidianReinforcement = ITEMS.register("obsidian_reinforcement", ITEM_PROPS);
  // special
  public static final ItemObject<Item> modifierCrystal = ITEMS.register("modifier_crystal", () -> new ModifierCrystalItem(new Item.Properties().stacksTo(16)));
  public static final ItemObject<CreativeSlotItem> creativeSlotItem = ITEMS.register("creative_slot", () -> new CreativeSlotItem(ITEM_PROPS));

  // entity
  public static final DeferredHolder<? super EntityType<FluidEffectProjectile>, EntityType<FluidEffectProjectile>> fluidSpitEntity = ENTITIES.register("fluid_spit", () ->
    EntityType.Builder.<FluidEffectProjectile>of(FluidEffectProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).setShouldReceiveVelocityUpdates(false));
  public static final DeferredHolder<? super EntityType<CustomFireball>, EntityType<CustomFireball>> fireball = ENTITIES.register("fireball", () -> EntityType.Builder.<CustomFireball>of(CustomFireball::new, MobCategory.MISC).sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10));

  /*
   * Modifiers
   */

  // upgrades
  public static final StaticModifier<SinistralModifier> sinistral = MODIFIERS.register("sinistral", SinistralModifier::new);

  // abilities
  public static final StaticModifier<ExchangingModifier> exchanging = MODIFIERS.register("exchanging", ExchangingModifier::new);
  public static final StaticModifier<BurstingModifier> bursting = MODIFIERS.register("bursting", BurstingModifier::new);
  public static final StaticModifier<WettingModifier> wetting = MODIFIERS.register("wetting", WettingModifier::new);
  // dual wielding
  public static final StaticModifier<OffhandAttackModifier> offhandAttack = MODIFIERS.register("offhand_attack", OffhandAttackModifier::new);
  public static final StaticModifier<DuelWieldingModifier> dualWielding = MODIFIERS.register("dual_wielding", DuelWieldingModifier::new);
  public static final StaticModifier<AmbidextrousModifier> ambidextrous = MODIFIERS.register("ambidextrous", AmbidextrousModifier::new);
  public static final StaticModifier<ParryingModifier> parrying = MODIFIERS.register("parrying", ParryingModifier::new);

  // traits - tier 1
  public static final StaticModifier<DwarvenModifier> dwarven = MODIFIERS.register("dwarven", DwarvenModifier::new);
  // traits - tier 2
  public static final DynamicModifier golden = MODIFIERS.registerDynamic("golden");
  // traits - tier 3
  public static final StaticModifier<LaceratingModifier> lacerating = MODIFIERS.register("lacerating", LaceratingModifier::new);
  public static final StaticModifier<Modifier> overworked = MODIFIERS.register("overworked", Modifier::new);
  // traits - tier 4
  public static final StaticModifier<MomentumModifier> momentum = MODIFIERS.register("momentum", MomentumModifier::new);
  public static final StaticModifier<InsatiableModifier> insatiable = MODIFIERS.register("insatiable", InsatiableModifier::new);
  public static final StaticModifier<ConductingModifier> conducting = MODIFIERS.register("conducting", ConductingModifier::new);
  public static final StaticModifier<EnderportingModifier> enderporting = MODIFIERS.register("enderporting", EnderportingModifier::new);
  // traits - slimeskull
  public static final StaticModifier<SelfDestructiveModifier> selfDestructive = MODIFIERS.register("self_destructive", SelfDestructiveModifier::new);
  public static final StaticModifier<StrongBonesModifier> strongBones = MODIFIERS.register("strong_bones", StrongBonesModifier::new);
  public static final StaticModifier<PlagueModifier> plague = MODIFIERS.register("plague", PlagueModifier::new);
  public static final StaticModifier<ChrysophiliteModifier> chrysophilite = MODIFIERS.register("chrysophilite", ChrysophiliteModifier::new);
  public static final StaticModifier<GoldGuardModifier> goldGuard = MODIFIERS.register("gold_guard", GoldGuardModifier::new);

  // slotless - cosmetic - used as defaults for rendering modules and recipes
  public static final StaticModifier<?> embellishment = MODIFIERS.registerDynamic("embellishment");
  public static final StaticModifier<?> dyed = MODIFIERS.registerDynamic("dyed");
  public static final StaticModifier<?> trim = MODIFIERS.registerDynamic("trim");
  public static final StaticModifier<?> banner = MODIFIERS.registerDynamic("banner");

  // used in client renderer
  public static final DynamicModifier itemFrame = MODIFIERS.registerDynamic("item_frame");
  public static final DynamicModifier sleeves = MODIFIERS.registerDynamic("sleeves");
  public static final DynamicModifier shieldStrap = MODIFIERS.registerDynamic("shield_strap");

  // used in JEI
  /** Used in JEI to add tools to the severing tab */
  public static final StaticModifier<?> severing = MODIFIERS.registerDynamic("severing");
  /** Used in JEI to add tools to the melting tabs */
  public static final DynamicModifier melting = MODIFIERS.registerDynamic("melting");

  // logic handlers - used as modifier traits
  /** Handles the fluid tank logic for any fluid using modifiers. */
  public static final StaticModifier<Modifier> tankHandler = MODIFIERS.register("tank_handler", () -> ModuleHookMap.builder().addModule(new TankModule(ToolTankHelper.TANK_HELPER)).modifier().levelDisplay(ModifierLevelDisplay.NO_LEVELS).priority(300).build());
  /** Handles the energy bar for Forge Energy using modifiers. */
  public static final StaticModifier<Modifier> energyHandler = MODIFIERS.register("energy_handler", EnergyHandlerModifier::new);

  // creative
  /** Handles adding extra modifier slots to a tool in creative */
  public static final StaticModifier<CreativeSlotModifier> creativeSlot = MODIFIERS.register("creative_slot", CreativeSlotModifier::new);
  /** Handles overriding stats on a tool. */
  public static final StaticModifier<StatOverrideModifier> statOverride = MODIFIERS.register("stat_override", StatOverrideModifier::new);


  /* deprecated fields */

  // modifiers with deprecated API
  /** Use API from {@link modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule} */
  public static final StaticModifier<OverslimeModifier> overslime = MODIFIERS.register("overslime", OverslimeModifier::new);
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#sweeping} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  public static final StaticModifier<SweepingEdgeModifier> sweeping = new FakeModifier<>(new ModifierId(TConstruct.MOD_ID, "sweeping_edge"), SweepingEdgeModifier::new);
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#expanded} (modifier) or {@link modernmods.modernfoundry.library.tools.item.IModifiable#EXPANDED} (querying) */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> expanded = MODIFIERS.registerDynamic("expanded");


  // modifiers pending removal - reimplement if you need them
  /** @deprecated Piercing now removes armor instead of ignoring armor. See {@link SpillingModule} for a similar effect. */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  public static final StaticModifier<PiercingModifier> piercing = MODIFIERS.register("piercing", PiercingModifier::new);
  /** @deprecated Platinum was removed. */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<OlympicModifier> olympic = MODIFIERS.register("olympic", OlympicModifier::new);
  /** @deprecated invar's trait was switched to {@link modernmods.modernfoundry.tools.data.ModifierIds#solid}. Reimplement if you need its behavior. */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier invariant = MODIFIERS.registerDynamic("invariant");
  // slimeskull
  /** @deprecated zombie's trait was switched to {@link modernmods.modernfoundry.tools.data.ModifierIds#consecrated}. Reimplement if you need its behavior. */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<WildfireModifier> wildfire = MODIFIERS.register("wildfire", WildfireModifier::new);
  /** @deprecated drowned's trait was switched to {@link modernmods.modernfoundry.tools.data.ModifierIds#respiration}. Reimplement if you need its behavior. */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<BreathtakingModifier> breathtaking = MODIFIERS.register("breathtaking", BreathtakingModifier::new);
  /** @deprecated wither skeleton's trait was switched to {@link modernmods.modernfoundry.tools.data.ModifierIds#restore}. Reimplement if you need its behavior. */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<WitheredModifier> withered = MODIFIERS.register("withered", WitheredModifier::new);
  /** @deprecated blazes trait was switched to {@link modernmods.modernfoundry.tools.data.ModifierIds#fireborn} and helmet projectile was switched to {@link modernmods.modernfoundry.tools.data.ModifierIds#spitting} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<FirebreathModifier> firebreath = MODIFIERS.register("firebreath", FirebreathModifier::new);

  // fields that have been relocated to ModifierIds
  // slotless
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#farsighted} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> farsighted = MODIFIERS.registerDynamic("farsighted");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#nearsighted} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> nearsighted = MODIFIERS.registerDynamic("nearsighted");

  // upgrades
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#magnetic} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> magnetic = MODIFIERS.registerDynamic("magnetic");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#knockback} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier knockback = MODIFIERS.registerDynamic("knockback");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#padded} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier padded = MODIFIERS.registerDynamic("padded");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#zoom} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier zoom = MODIFIERS.registerDynamic("zoom");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#scope} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier scope = MODIFIERS.registerDynamic("scope");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#necrotic} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> necrotic = MODIFIERS.registerDynamic("necrotic");

  // combat upgrades
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#fiery} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier fiery = MODIFIERS.registerDynamic("fiery");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#freezing} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier freezing = MODIFIERS.registerDynamic("freezing");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#punch} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier punch = MODIFIERS.registerDynamic("punch");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#arrowPierce} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier impaling = MODIFIERS.registerDynamic("impaling");

  // armor upgrades
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#thorns} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier thorns = MODIFIERS.registerDynamic("thorns");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#springy} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier springy = MODIFIERS.registerDynamic("springy");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#soulspeed} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> soulspeed = MODIFIERS.registerDynamic("soulspeed");

  // abilities
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#unbreakable} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier unbreakable = MODIFIERS.registerDynamic("unbreakable");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#crystalshot} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier crystalshot = MODIFIERS.registerDynamic("crystalshot");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#multishot} (modifier) or {@link modernmods.modernfoundry.library.modifiers.hook.ranged.BowAmmoModifierHook#MULTISHOT} (querying) */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> multishot = MODIFIERS.registerDynamic("multishot");

  // harvest abilities
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#silky} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier silky = MODIFIERS.registerDynamic("silky");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#autosmelt} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> autosmelt = MODIFIERS.registerDynamic("autosmelt");

  // armor abilities
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#doubleJump} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier doubleJump = MODIFIERS.registerDynamic("double_jump");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#bouncy} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier bouncy = MODIFIERS.registerDynamic("bouncy");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#flamewake} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> flamewake = MODIFIERS.registerDynamic("flamewake");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#reflecting} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> reflecting = MODIFIERS.registerDynamic("reflecting");

  // interaction abilities
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#firestarter} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> firestarter = MODIFIERS.registerDynamic("firestarter");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#fireprimer} (modifier), or {@link modernmods.modernfoundry.library.tools.item.IModifiable#EXPANDED} (querying) */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> fireprimer = MODIFIERS.registerDynamic("fireprimer");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#blocking} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> blocking = MODIFIERS.registerDynamic("blocking");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#jagged} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> harvest = MODIFIERS.registerDynamic("harvest");

  // shears
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#shears} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> shears = MODIFIERS.registerDynamic("shears");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#silkyShears} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> silkyShears = MODIFIERS.registerDynamic("silky_shears");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#silkyShears} at level 2 */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> aoeSilkyShears = MODIFIERS.registerDynamic("silky_aoe_shears");

  // slings
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#flinging} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> flinging = MODIFIERS.registerDynamic("flinging");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#springing} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> springing = MODIFIERS.registerDynamic("springing");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#bonking} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> bonking = MODIFIERS.registerDynamic("bonking");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#warping} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> warping = MODIFIERS.registerDynamic("warping");

  // fluid abilities
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#bucketing} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> bucketing = MODIFIERS.registerDynamic("bucketing");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#spilling} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier spilling = MODIFIERS.registerDynamic("spilling");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#splashing} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> splashing = MODIFIERS.registerDynamic("splashing");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#slurping} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> slurping = MODIFIERS.registerDynamic("slurping");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#spitting} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> spitting = MODIFIERS.registerDynamic("spitting");

  // traits - tier 1
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#jagged} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier jagged = MODIFIERS.registerDynamic("jagged");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#stonebound} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier stonebound = MODIFIERS.registerDynamic("stonebound");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#tanned} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> tanned = MODIFIERS.registerDynamic("tanned");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#solarPowered} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> solarPowered = MODIFIERS.registerDynamic("solar_powered");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#frostshield} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier frostshield = MODIFIERS.registerDynamic("frostshield");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#enderference} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> enderference = MODIFIERS.registerDynamic("enderference");

  // traits - tier 2
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#stoneshield} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier stoneshield = MODIFIERS.registerDynamic("stoneshield");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#holy} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier holy = MODIFIERS.registerDynamic("holy");

  // traits - tier 3
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#tasty} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> tasty = MODIFIERS.registerDynamic("tasty");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#temperate} */
  @Deprecated(forRemoval = true)
  public static final DynamicModifier temperate = MODIFIERS.registerDynamic("temperate");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#decay} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> decay = MODIFIERS.registerDynamic("decay");

  // traits - slimeskull
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#boonOfSssss} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> boonOfSssss = MODIFIERS.registerDynamic("boon_of_sssss");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#slowBones} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> frosttouch = MODIFIERS.registerDynamic("frosttouch");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#revenge} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> revenge = MODIFIERS.registerDynamic("revenge");
  /** @deprecated use {@link modernmods.modernfoundry.tools.data.ModifierIds#revenge} */
  @Deprecated(forRemoval = true)
  public static final StaticModifier<?> enderdodging = MODIFIERS.registerDynamic("enderdodging");


  /*
   * Effects
   */
  /** @deprecated use {@link TinkerEffects#bleeding} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, BleedingEffect> bleeding = TinkerEffects.bleeding;
  /** @deprecated use {@link TinkerEffects#magnetic} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, MagneticEffect> magneticEffect = TinkerEffects.magnetic;
  /** @deprecated use {@link TinkerEffects#repulsive} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, RepulsiveEffect> repulsiveEffect = TinkerEffects.repulsive;
  /** @deprecated use {@link TinkerEffects#enderference} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, TinkerEffect> enderferenceEffect = TinkerEffects.enderference;
  /** @deprecated use {@link TinkerEffects#selfDestructing} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, TinkerEffect> selfDestructiveEffect = TinkerEffects.selfDestructing;
  /** @deprecated use {@link TinkerEffects#pierce} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, TinkerEffect> pierceEffect = TinkerEffects.pierce;

  /** Effect for rendering the helmet charging icon in the GUI */
  public static final DeferredHolder<MobEffect, HelmetChargingEffect> helmetCharging = MOB_EFFECTS.register("helmet_charging", HelmetChargingEffect::new);
  // cooldown
  /** @deprecated use {@link TinkerEffects#enderference} */
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<MobEffect, TinkerEffect> teleportCooldownEffect = MOB_EFFECTS.register("teleport_cooldown", () -> new NoMilkEffect(MobEffectCategory.HARMFUL, 0xCC00FA, true));
  public static final DeferredHolder<MobEffect, TinkerEffect> fireballCooldownEffect = MOB_EFFECTS.register("fireball_cooldown", () -> new NoMilkEffect(MobEffectCategory.HARMFUL, 0xFC9600, true));
  // internal
  public static final DeferredHolder<MobEffect, TinkerEffect> calcifiedEffect = MOB_EFFECTS.register("calcified", () -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, -1, true));
  // markers
  public static final EnumObject<ToolType,TinkerEffect> momentumEffect = MOB_EFFECTS.registerEnum("momentum", ToolType.NO_MELEE, type -> new NoMilkEffect(MobEffectCategory.BENEFICIAL, 0x60496b, true));
  public static final EnumObject<ToolType,TinkerEffect> insatiableEffect = MOB_EFFECTS.registerEnum("insatiable", new ToolType[] {ToolType.MELEE, ToolType.RANGED, ToolType.ARMOR}, type -> {
    TinkerEffect effect = new NoMilkEffect(MobEffectCategory.BENEFICIAL, 0x9261cc, true);
    if (type == ToolType.ARMOR) {
      effect.addAttributeModifier(Attributes.ATTACK_DAMAGE, "cc6904f7-674a-4e6a-b992-4f3cb8edfef4", 1, AttributeModifier.Operation.ADD_VALUE);
    }
    return effect;
  });

  /*
   * Recipes
   */
  public static final DeferredHolder<? super RecipeSerializer<ModifierRecipe>, RecipeSerializer<ModifierRecipe>> modifierSerializer = RECIPE_SERIALIZERS.register("modifier", () -> LoadableRecipeSerializer.of(ModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<IncrementalModifierRecipe>, RecipeSerializer<IncrementalModifierRecipe>> incrementalModifierSerializer = RECIPE_SERIALIZERS.register("incremental_modifier", () -> LoadableRecipeSerializer.of(IncrementalModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<SwappableModifierRecipe>, RecipeSerializer<SwappableModifierRecipe>> swappableModifierSerializer = RECIPE_SERIALIZERS.register("swappable_modifier", () -> LoadableRecipeSerializer.of(SwappableModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<MultilevelModifierRecipe>, RecipeSerializer<MultilevelModifierRecipe>> multilevelModifierSerializer = RECIPE_SERIALIZERS.register("multilevel_modifier", () -> LoadableRecipeSerializer.of(MultilevelModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<MultilevelIncrementalModifierRecipe>, RecipeSerializer<MultilevelIncrementalModifierRecipe>> multilevelIncrementalModifierSerializer = RECIPE_SERIALIZERS.register("multilevel_incremental_modifier", () -> LoadableRecipeSerializer.of(MultilevelIncrementalModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<OverslimeModifierRecipe>, RecipeSerializer<OverslimeModifierRecipe>> overslimeSerializer = RECIPE_SERIALIZERS.register("overslime_modifier", () -> LoadableRecipeSerializer.of(OverslimeModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<OverslimeCraftingTableRecipe>, RecipeSerializer<OverslimeCraftingTableRecipe>> craftingOverslimeSerializer = RECIPE_SERIALIZERS.register("crafting_overslime_modifier", () -> LoadableRecipeSerializer.of(OverslimeCraftingTableRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ModifierSalvage>, RecipeSerializer<ModifierSalvage>> modifierSalvageSerializer = RECIPE_SERIALIZERS.register("modifier_salvage", () -> LoadableRecipeSerializer.of(ModifierSalvage.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ArmorDyeingRecipe>, RecipeSerializer<ArmorDyeingRecipe>> armorDyeingSerializer = RECIPE_SERIALIZERS.register("armor_dyeing_modifier", () -> new SimpleRecipeSerializer<>(ArmorDyeingRecipe::new));
  public static final DeferredHolder<? super RecipeSerializer<BannerModifierRecipe>, RecipeSerializer<BannerModifierRecipe>> bannerModifierSerializer = RECIPE_SERIALIZERS.register("banner_modifier", () -> new SimpleRecipeSerializer<>(BannerModifierRecipe::new));
  public static final DeferredHolder<? super RecipeSerializer<ArmorTrimRecipe>, RecipeSerializer<ArmorTrimRecipe>> armorTrimSerializer = RECIPE_SERIALIZERS.register("armor_trim_modifier", () -> new SimpleRecipeSerializer<>(ArmorTrimRecipe::new));
  public static final DeferredHolder<? super RecipeSerializer<TippedToolTransformRecipe>, RecipeSerializer<TippedToolTransformRecipe>> tippedToolTransformRecipeSerializer = RECIPE_SERIALIZERS.register("tipped_tool_transform", () -> LoadableRecipeSerializer.of(TippedToolTransformRecipe.LOADER));
  // modifiers
  public static final DeferredHolder<? super RecipeSerializer<ModifierRepairTinkerStationRecipe>, RecipeSerializer<ModifierRepairTinkerStationRecipe>> modifierRepair = RECIPE_SERIALIZERS.register("modifier_repair", () -> LoadableRecipeSerializer.of(ModifierRepairTinkerStationRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ModifierRepairCraftingRecipe>, RecipeSerializer<ModifierRepairCraftingRecipe>> craftingModifierRepair = RECIPE_SERIALIZERS.register("crafting_modifier_repair", () -> LoadableRecipeSerializer.of(ModifierRepairCraftingRecipe.LOADER));
  /** @deprecated use {@link MaterialRepairModule} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<? super RecipeSerializer<ModifierMaterialRepairRecipe>, RecipeSerializer<ModifierMaterialRepairRecipe>> modifierMaterialRepair = RECIPE_SERIALIZERS.register("modifier_material_repair", () -> LoadableRecipeSerializer.deprecated(ModifierMaterialRepairRecipe.LOADER, "use the modernfoundry:material_repair modifier module instead"));
  /** @deprecated use {@link MaterialRepairModule} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  public static final DeferredHolder<? super RecipeSerializer<ModifierMaterialRepairKitRecipe>, RecipeSerializer<ModifierMaterialRepairKitRecipe>> craftingModifierMaterialRepair = RECIPE_SERIALIZERS.register("crafting_modifier_material_repair", () -> LoadableRecipeSerializer.deprecated(ModifierMaterialRepairKitRecipe.LOADER, "use the modernfoundry:material_repair modifier module instead"));
  // worktable
  public static final DeferredHolder<? super RecipeSerializer<ModifierRemovalRecipe>, RecipeSerializer<ModifierRemovalRecipe>> removeModifierSerializer = RECIPE_SERIALIZERS.register("remove_modifier", () -> LoadableRecipeSerializer.of(ModifierRemovalRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ExtractModifierRecipe>, RecipeSerializer<ExtractModifierRecipe>> extractModifierSerializer = RECIPE_SERIALIZERS.register("extract_modifier", () -> LoadableRecipeSerializer.of(ExtractModifierRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ModifierSortingRecipe>, RecipeSerializer<ModifierSortingRecipe>> modifierSortingSerializer = RECIPE_SERIALIZERS.register("modifier_sorting", () -> LoadableRecipeSerializer.of(ModifierSortingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ModifierSetWorktableRecipe>, RecipeSerializer<ModifierSetWorktableRecipe>> modifierSetWorktableSerializer = RECIPE_SERIALIZERS.register("modifier_set_worktable", () -> LoadableRecipeSerializer.of(ModifierSetWorktableRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<EnchantmentConvertingRecipe>, RecipeSerializer<EnchantmentConvertingRecipe>> enchantmentConvertingSerializer = RECIPE_SERIALIZERS.register("enchantment_converting", () -> LoadableRecipeSerializer.of(EnchantmentConvertingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<ToggleInteractionWorktableRecipe>, RecipeSerializer<ToggleInteractionWorktableRecipe>> toggleInteractionSerializer = RECIPE_SERIALIZERS.register("toggle_interaction", () -> LoadableRecipeSerializer.of(ToggleInteractionWorktableRecipe.LOADER));

  // severing
  public static final DeferredHolder<? super RecipeSerializer<SeveringRecipe>, RecipeSerializer<SeveringRecipe>> severingSerializer = RECIPE_SERIALIZERS.register("severing", () -> LoadableRecipeSerializer.of(SeveringRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<AgeableSeveringRecipe>, RecipeSerializer<AgeableSeveringRecipe>> ageableSeveringSerializer = RECIPE_SERIALIZERS.register("ageable_severing", () -> LoadableRecipeSerializer.of(AgeableSeveringRecipe.LOADER));
  // special severing
  public static final DeferredHolder<? super RecipeSerializer<PlayerBeheadingRecipe>, RecipeSerializer<PlayerBeheadingRecipe>> playerBeheadingSerializer = RECIPE_SERIALIZERS.register("player_beheading", () -> LoadableRecipeSerializer.of(PlayerBeheadingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<SnowGolemBeheadingRecipe>, RecipeSerializer<SnowGolemBeheadingRecipe>> snowGolemBeheadingSerializer = RECIPE_SERIALIZERS.register("snow_golem_beheading", () -> LoadableRecipeSerializer.of(SnowGolemBeheadingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<MooshroomDemushroomingRecipe>, RecipeSerializer<MooshroomDemushroomingRecipe>> mooshroomDemushroomingSerializer = RECIPE_SERIALIZERS.register("mooshroom_demushrooming", () -> LoadableRecipeSerializer.of(MooshroomDemushroomingRecipe.LOADER));
  public static final DeferredHolder<? super RecipeSerializer<SheepShearingRecipe>, RecipeSerializer<SheepShearingRecipe>> sheepShearing = RECIPE_SERIALIZERS.register("sheep_shearing", () -> LoadableRecipeSerializer.of(SheepShearingRecipe.LOADER));

  /**
   * Loot
   */
  public static final DeferredHolder<? super MapCodec<ModifierLootModifier>, MapCodec<ModifierLootModifier>> modifierLootModifier = GLOBAL_LOOT_MODIFIERS.register("modifier_hook", () -> ModifierLootModifier.CODEC);
  public static final DeferredHolder<? super LootItemConditionType, LootItemConditionType> hasModifierLootCondition = LOOT_CONDITIONS.register("has_modifier", () -> new LootItemConditionType(HasModifierLootCondition.CODEC));
  public static final DeferredHolder<LootItemFunctionType<?>, ? extends LootItemFunctionType<?>> modifierBonusFunction = LOOT_FUNCTIONS.register("modifier_bonus", () -> new LootItemFunctionType<>(ModifierBonusLootFunction.CODEC));
  public static final DeferredHolder<? super LootItemConditionType, LootItemConditionType> chrysophiliteLootCondition = LOOT_CONDITIONS.register("has_chrysophilite", () -> new LootItemConditionType(ChrysophiliteLootCondition.CODEC));
  public static final DeferredHolder<LootItemFunctionType<?>, ? extends LootItemFunctionType<?>> chrysophiliteBonusFunction = LOOT_FUNCTIONS.register("chrysophilite_bonus", () -> new LootItemFunctionType<>(ChrysophiliteBonusFunction.CODEC));

  /*
   * Events
   */

  @SubscribeEvent
  void registerSerializers(RegisterEvent event) {
    if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
      // combinations
      FluidEffect.BLOCK_EFFECTS.register(getResource("conditional"), ConditionalFluidEffect.Block.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("conditional"), ConditionalFluidEffect.Entity.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("scaling"), ScalingFluidEffect.BLOCK_LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("scaling"), ScalingFluidEffect.ENTITY_LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("alternatives"), AlternativesFluidEffect.BLOCK_LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("alternatives"), AlternativesFluidEffect.ENTITY_LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("sequence"), SequenceFluidEffect.BLOCK_LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("sequence"), SequenceFluidEffect.ENTITY_LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("offset"), OffsetBlockFluidEffect.LOADER);
      // simple
      FluidEffect.ENTITY_EFFECTS.register(getResource("calcified"), StrongBonesModifier.FLUID_EFFECT.getLoader());
      FluidEffect.ENTITY_EFFECTS.register(getResource("extinguish"), FluidEffect.EXTINGUISH_FIRE.getLoader());
      FluidEffect.ENTITY_EFFECTS.register(getResource("teleport"), RandomTeleportFluidEffect.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("weather"), FluidEffect.WEATHER.getLoader());
      // potions
      FluidEffect.ENTITY_EFFECTS.register(getResource("cure_effects"), CureEffectsFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("remove_effect"), RemoveEffectFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("mob_effect"), MobEffectFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("potion"), PotionFluidEffect.LOADER);
      // misc
      FluidEffect.ENTITY_EFFECTS.register(getResource("damage"), DamageFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("restore_hunger"), RestoreHungerFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("fire"), FireFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("freeze"), FreezeFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("award_stat"), AwardStatFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("add_breath"), AddBreathFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("push_entity"), PushEntityFluidEffect.LOADER);
      FluidEffect.ENTITY_EFFECTS.register(getResource("interact"), EntityInteractFluidEffect.INSTANCE.getLoader());
      // block
      FluidEffect.BLOCK_EFFECTS.register(getResource("place_block"), PlaceBlockFluidEffect.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("break_block"), BreakBlockFluidEffect.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("remove_block"), FluidEffect.REMOVE_BLOCK.getLoader());
      FluidEffect.BLOCK_EFFECTS.register(getResource("mob_effect_cloud"), MobEffectCloudFluidEffect.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("potion_cloud"), PotionCloudFluidEffect.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("move_block"), MoveBlocksFluidEffect.LOADER);
      FluidEffect.BLOCK_EFFECTS.register(getResource("interact"), BlockInteractFluidEffect.INSTANCE.getLoader());
      FluidEffect.BLOCK_EFFECTS.register(getResource("melt_block"), MeltBlockFluidEffect.LOADER);
      // shared
      FluidEffect.registerGeneral(getResource("drop_item"), DropItemFluidEffect.LOADER);
      FluidEffect.registerGeneral(getResource("explosion"), ExplosionFluidEffect.LOADER);
      FluidEffect.registerGeneral(getResource("set_block"), SetBlockFluidEffect.LOADER);
      FluidEffect.registerGeneral(getResource("area_mob_effect"), AreaMobEffectFluidEffect.LOADER);


      // modifier names, sometimes I wonder if I have too many registries for tiny JSON pieces
      ModifierLevelDisplay.LOADER.register(getResource("default"), ModifierLevelDisplay.DEFAULT.getLoader());
      ModifierLevelDisplay.LOADER.register(getResource("single_level"), ModifierLevelDisplay.SINGLE_LEVEL.getLoader());
      ModifierLevelDisplay.LOADER.register(getResource("no_levels"), ModifierLevelDisplay.NO_LEVELS.getLoader());
      ModifierLevelDisplay.LOADER.register(getResource("pluses"), ModifierLevelDisplay.PLUSES.getLoader());
      ModifierLevelDisplay.LOADER.register(getResource("unique"), ModifierLevelDisplay.UniqueForLevels.LOADER);
      ModifierLevelDisplay.LOADER.register(getResource("cap_level"), ModifierLevelDisplay.LevelCap.LOADER);

      // modifier modules //
      ModifierModule.LOADER.register(getResource("empty"), ModifierModule.EMPTY.getLoader());
      // armor
      ModifierModule.LOADER.register(getResource("max_armor_attribute"), MaxArmorAttributeModule.LOADER);
      ModifierModule.LOADER.register(getResource("effect_immunity"), EffectImmunityModule.LOADER);
      ModifierModule.LOADER.register(getResource("mob_disguise"), MobDisguiseModule.LOADER);
      ModifierModule.LOADER.register(getResource("block_damage"), BlockDamageSourceModule.LOADER);
      ModifierModule.LOADER.register(getResource("cover_ground"), CoverGroundWalkerModule.LOADER);
      ModifierModule.LOADER.register(getResource("protection"), ProtectionModule.LOADER);
      ModifierModule.LOADER.register(getResource("adjust_damage"), AdjustDamageModule.LOADER);
      ModifierModule.LOADER.register(getResource("replace_fluid"), ReplaceBlockWalkerModule.LOADER);
      ModifierModule.LOADER.register(getResource("tool_action_walk_transform"), ToolActionWalkerTransformModule.LOADER);
      // behavior
      ModifierModule.LOADER.register(getResource("attribute"), AttributeModule.LOADER);
      ModifierModule.LOADER.register(getResource("reduce_tool_damage"), ReduceToolDamageModule.LOADER);
      // TODO 1.21: rename to repair_factor?
      ModifierModule.LOADER.register(getResource("repair"), RepairModule.LOADER);
      ModifierModule.LOADER.register(getResource("material_repair"), MaterialRepairModule.LOADER);
      ModifierModule.LOADER.register(getResource("show_offhand"), ShowOffhandModule.LOADER);
      ModifierModule.LOADER.register(getResource("tool_actions"), ToolActionsModule.LOADER);
      ModifierModule.LOADER.register(getResource("tool_action_transform"), ToolActionTransformModule.LOADER);
      ModifierModule.LOADER.register(getResource("edible"), EdibleModule.LOADER);
      // build
      ModifierModule.LOADER.register(getResource("conditional_stat"), ConditionalStatModule.LOADER);
      ModifierModule.LOADER.register(getResource("modifier_slot"), ModifierSlotModule.LOADER);
      ModifierModule.LOADER.register(getResource("rarity"), RarityModule.LOADER);
      ModifierModule.LOADER.register(getResource("requirements"), ModifierRequirementsModule.LOADER);
      ModifierModule.LOADER.register(getResource("swappable_slot"), SwappableSlotModule.LOADER);
      ModifierModule.LOADER.register(getResource("swappable_bonus_slot"), SwappableSlotModule.BonusSlot.LOADER);
      ModifierModule.LOADER.register(getResource("swappable_tool_traits"), SwappableToolTraitsModule.LOADER);
      ModifierModule.LOADER.register(getResource("stat_boost"), StatBoostModule.LOADER);
      ModifierModule.LOADER.register(getResource("stat_copy"), StatCopyModule.LOADER);
      ModifierModule.LOADER.register(getResource("set_stat"), SetStatModule.LOADER);
      ModifierModule.LOADER.register(getResource("trait"), ModifierTraitModule.LOADER);
      ModifierModule.LOADER.register(getResource("volatile_flag"), VolatileFlagModule.LOADER);
      ModifierModule.LOADER.register(getResource("volatile_int"), VolatileIntModule.LOADER);
      ModifierModule.LOADER.register(getResource("volatile_float"), VolatileFloatModule.LOADER);
      // combat
      ModifierModule.LOADER.register(getResource("conditional_melee_damage"), ConditionalMeleeDamageModule.LOADER);
      ModifierModule.LOADER.register(getResource("conditional_power"), ConditionalPowerModule.LOADER);
      ModifierModule.LOADER.register(getResource("knockback"), KnockbackModule.LOADER);
      ModifierModule.LOADER.register(getResource("melee_attribute"), MeleeAttributeModule.LOADER);
      ModifierModule.LOADER.register(getResource("projectile_explosion"), ProjectileExplosionModule.LOADER);
      ModifierModule.LOADER.register(getResource("sling_force"), SlingForceModule.LOADER);
      // mob effect
      ModifierModule.LOADER.register(getResource("mob_effect"), MobEffectModule.LOADER);
      ModifierModule.LOADER.register(getResource("weapon_mob_effect"), MobEffectModule.Weapon.LOADER);
      ModifierModule.LOADER.register(getResource("counter_mob_effect"), MobEffectModule.ArmorCounter.LOADER);
      ModifierModule.LOADER.register(getResource("tool_usage_mob_effect"), MobEffectModule.ToolUsage.LOADER);
      ModifierModule.LOADER.register(getResource("armor_attack_mob_effect"), MobEffectModule.ArmorAttack.LOADER);
      // display
      ModifierModule.LOADER.register(getResource("durability_color"), DurabilityBarColorModule.LOADER);
      ModifierModule.LOADER.register(getResource("variant_name"), ModifierVariantNameModule.LOADER);
      ModifierModule.LOADER.register(getResource("variant_color"), ModifierVariantColorModule.LOADER);
      ModifierModule.LOADER.register(getResource("material_variant_color"), MaterialVariantColorModule.LOADER);
      ModifierModule.LOADER.register(getResource("show_interaction_source"), ShowInteractionSourceModule.LOADER);
      // enchantment
      ModifierModule.LOADER.register(getResource("constant_enchantment"), EnchantmentModule.Constant.LOADER);
      ModifierModule.LOADER.register(getResource("main_hand_harvest_enchantment"), EnchantmentModule.MainHandHarvest.LOADER);
      ModifierModule.LOADER.register(getResource("armor_harvest_enchantment"), EnchantmentModule.ArmorHarvest.LOADER);
      ModifierModule.LOADER.register(getResource("enchantment_ignoring_protection"), EnchantmentModule.Protection.LOADER);
      ModifierModule.LOADER.register(getResource("weapon_looting"), LootingModule.Weapon.LOADER);
      ModifierModule.LOADER.register(getResource("armor_looting"), LootingModule.Armor.LOADER);
      // mining
      ModifierModule.LOADER.register(getResource("conditional_mining_speed"), ConditionalMiningSpeedModule.LOADER);
      // capacity
      ModifierModule.LOADER.register(getResource("capacity_bar"), CapacityBarModule.LOADER);
      ModifierModule.LOADER.register(getResource("durability_as_capacity"), DurabilityAsCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("energy_as_capacity"), EnergyAsCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("durability_shield"), DurabilityShieldModule.LOADER);
      ModifierModule.LOADER.register(getResource("loot_to_capacity"), LootToCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("damage_to_capacity"), DamageToCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("time_to_capacity"), TimeToCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("launch_capacity"), LaunchCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("mining_capacity"), MiningCapacityModule.LOADER);
      ModifierModule.LOADER.register(getResource("melee_capacity"), MeleeCapacityModule.LOADER);
      // technical
      ModifierModule.LOADER.register(getResource("armor_level"), ArmorLevelModule.LOADER);
      ModifierModule.LOADER.register(getResource("max_armor_stat"), MaxArmorStatModule.LOADER);
      ModifierModule.LOADER.register(getResource("armor_stat"), ArmorStatModule.LOADER);
      ModifierModule.LOADER.register(getResource("inventory"), InventoryModule.LOADER);
      ModifierModule.LOADER.register(getResource("inventory_menu"), InventoryMenuModule.LOADER);
      ModifierModule.LOADER.register(getResource("inventory_slot_menu"), InventorySlotMenuModule.INSTANCE.getLoader());

      // special
      ModifierModule.LOADER.register(getResource("smelting"), SmeltingModule.LOADER);
      ModifierModule.LOADER.register(getResource("autosmelt"), AutosmeltModule.LOADER);
      ModifierModule.LOADER.register(getResource("melting"), MeltingModule.LOADER);
      ModifierModule.LOADER.register(getResource("glow_walker"), GlowWalkerModule.LOADER);
      ModifierModule.LOADER.register(getResource("fire_walker"), FireWalkerModule.LOADER);
      ModifierModule.LOADER.register(getResource("lightspeed_attribute"), LightspeedAttributeModule.LOADER);
      ModifierModule.LOADER.register(getResource("zoom"), ZoomModule.LOADER);
      ModifierModule.LOADER.register(getResource("fov"), FovModule.LOADER);
      ModifierModule.LOADER.register(getResource("damage_on_unequip"), DamageOnUnequipModule.LOADER);
      ModifierModule.LOADER.register(getResource("damage_on_shoot"), DamageOnShootModule.LOADER);
      ModifierModule.LOADER.register(getResource("reduce_effect_on_unequip"), ReduceEffectOnUnequipModule.LOADER);
      ModifierModule.LOADER.register(getResource("clear_effect_on_unequip"), ClearEffectOnUnequipModule.LOADER);
      ModifierModule.LOADER.register(getResource("share_durability"), ShareDurabilityModule.LOADER);
      ModifierModule.LOADER.register(getResource("craft_count"), CraftCountModule.LOADER);
      ModifierModule.LOADER.register(getResource("tipped"), TippedModule.LOADER);
      ModifierModule.LOADER.register(getResource("projectile_bounce"), ProjectileBounceModule.LOADER);
      ModifierModule.LOADER.register(getResource("block_item_provider"), BlockItemProviderModule.LOADER);
      ModifierModule.LOADER.register(getResource("tool_damage_range"), ToolDamageRangeModule.LOADER);
      // interaction
      ModifierModule.LOADER.register(getResource("brush"), BrushModule.LOADER);
      ModifierModule.LOADER.register(getResource("campfire_extinguish"), ExtinguishCampfireModule.LOADER);
      ModifierModule.LOADER.register(getResource("fishing"), FishingModule.LOADER);
      ModifierModule.LOADER.register(getResource("harvest"), HarvestModule.LOADER);
      ModifierModule.LOADER.register(getResource("place_glow"), PlaceGlowModule.LOADER);
      ModifierModule.LOADER.register(getResource("place_fire"), PlaceFireModule.LOADER);
      ModifierModule.LOADER.register(getResource("bucket"), BucketModule.LOADER);
      ModifierModule.LOADER.register(getResource("tank_interaction"), TankInteractionModule.LOADER);
      ModifierModule.LOADER.register(getResource("projectile_place_glow"), ProjectilePlaceGlowModule.LOADER);
      ModifierModule.LOADER.register(getResource("shears"), ShearsModule.LOADER);
      ModifierModule.LOADER.register(getResource("throwing"), ThrowingModule.LOADER);
      ModifierModule.LOADER.register(getResource("spitting"), SpittingModule.LOADER);
      ModifierModule.LOADER.register(getResource("splashing"), SplashingModule.LOADER);
      ModifierModule.LOADER.register(getResource("slurping"), SlurpingModule.LOADER);
      ModifierModule.LOADER.register(getResource("fireball"), FireballModule.LOADER);
      // sling
      ModifierModule.LOADER.register(getResource("sling_leap"), SlingLeapModule.LOADER);
      ModifierModule.LOADER.register(getResource("sling_knockback"), SlingKnockbackModule.LOADER);
      ModifierModule.LOADER.register(getResource("sling_teleport"), SlingTeleportModule.LOADER);
      // overslime
      ModifierModule.LOADER.register(getResource("overgrowth"), OvergrowthModule.LOADER);
      ModifierModule.LOADER.register(getResource("overburn"), OverburnModule.INSTANCE.getLoader());
      ModifierModule.LOADER.register(getResource("overshield"), OvershieldModule.LOADER);
      // combat
      ModifierModule.LOADER.register(getResource("fiery_attack"), FieryAttackModule.LOADER);
      ModifierModule.LOADER.register(getResource("fiery_armor_attack"), FieryArmorAttackModule.LOADER);
      ModifierModule.LOADER.register(getResource("freezing_attack"), FreezingAttackModule.LOADER);
      ModifierModule.LOADER.register(getResource("spilling"), SpillingModule.LOADER);
      ModifierModule.LOADER.register(getResource("channeling"), ChannelingModule.LOADER);
      ModifierModule.LOADER.register(getResource("smashing"), SmashingModule.LOADER);
      ModifierModule.LOADER.register(getResource("sweeping_edge"), SweepingEdgeModule.LOADER);
      ModifierModule.LOADER.register(getResource("severing"), SeveringModule.LOADER);
      ModifierModule.LOADER.register(getResource("blocking"), BlockingModule.LOADER);
      ModifierModule.LOADER.register(getResource("lifesteal"), LifestealModule.LOADER);
      // armor
      ModifierModule.LOADER.register(getResource("enderclearance"), EnderclearanceModule.LOADER);
      ModifierModule.LOADER.register(getResource("depth_protection"), DepthProtectionModule.LOADER);
      ModifierModule.LOADER.register(getResource("flame_barrier"), FlameBarrierModule.LOADER);
      ModifierModule.LOADER.register(getResource("kinetic"), KineticModule.LOADER);
      ModifierModule.LOADER.register(getResource("recurrent_protection"), RecurrentProtectionModule.LOADER);
      ModifierModule.LOADER.register(getResource("shield_strap"), ShieldStrapModule.LOADER);
      ModifierModule.LOADER.register(getResource("tool_belt"), ToolBeltModule.LOADER);
      ModifierModule.LOADER.register(getResource("minimap"), MinimapModule.LOADER);
      ModifierModule.LOADER.register(getResource("sleeves"), SleevesModule.LOADER);
      ModifierModule.LOADER.register(getResource("soulspeed"), SoulSpeedModule.LOADER);
      ModifierModule.LOADER.register(getResource("restore_lost_health"), RestoreLostHealthModule.LOADER);
      ModifierModule.LOADER.register(getResource("update_health"), UpdateHealthModule.LOADER);
      ModifierModule.LOADER.register(getResource("teleport_dodge"), TeleportDodgeModule.LOADER);
      // counterattack
      ModifierModule.LOADER.register(getResource("thorns"), ThornsModule.LOADER);
      ModifierModule.LOADER.register(getResource("fiery_counter"), FieryCounterModule.LOADER);
      ModifierModule.LOADER.register(getResource("freezing_counter"), FreezingCounterModule.LOADER);
      ModifierModule.LOADER.register(getResource("knockback_counter"), KnockbackCounterModule.LOADER);
      // ranged
      ModifierModule.LOADER.register(getResource("restrict_projectile_angle"), RestrictAngleModule.LOADER);
      ModifierModule.LOADER.register(getResource("bulk_quiver"), BulkQuiverModule.LOADER);
      ModifierModule.LOADER.register(getResource("trick_quiver"), TrickQuiverModule.LOADER);
      ModifierModule.LOADER.register(getResource("quiver_inventory"), QuiverInventoryModule.LOADER);
      ModifierModule.LOADER.register(getResource("infinity"), InfinityModule.LOADER);
      ModifierModule.LOADER.register(getResource("punch"), PunchModule.LOADER);
      ModifierModule.LOADER.register(getResource("reverse_punch"), ReversePunchModule.LOADER);
      ModifierModule.LOADER.register(getResource("arrow_pierce"), ArrowPierceModule.LOADER);
      ModifierModule.LOADER.register(getResource("projectile_gravity"), ProjectileGravityModule.LOADER);
      ModifierModule.LOADER.register(getResource("projectile_fuse"), ProjectileFuseModule.LOADER);
      ModifierModule.LOADER.register(getResource("projectile_attract_mobs"), ProjectileAttractMobsModule.LOADER);
      // cosmetic
      ModifierModule.LOADER.register(getResource("dye"), DyeModule.LOADER);
      ModifierModule.LOADER.register(getResource("embellishment"), EmbellishmentModule.LOADER);
      ModifierModule.LOADER.register(getResource("trim"), TrimModule.LOADER);
      ModifierModule.LOADER.register(getResource("banner"), BannerModule.LOADER);
      // compat
      ModifierModule.LOADER.register(getResource("the_one_probe"), TheOneProbeModule.INSTANCE.getLoader());
      ModifierModule.LOADER.register(getResource("headlight"), HeadlightModule.LOADER);

      // modifier predicates
      ModifierPredicate.LOADER.register(getResource("single"), SingleModifierPredicate.LOADER);
      ModifierPredicate.LOADER.register(getResource("tag"), TagModifierPredicate.LOADER);
      ModifierPredicate.LOADER.register(getResource("slot_type"), SlotTypeModifierPredicate.LOADER);


      // variables
      // block
      BlockVariable.LOADER.register(getResource("constant"), BlockVariable.Constant.LOADER);
      BlockVariable.LOADER.register(getResource("conditional"), ConditionalBlockVariable.LOADER);
      BlockVariable.LOADER.register(getResource("blast_resistance"), BlockVariable.BLAST_RESISTANCE.getLoader());
      BlockVariable.LOADER.register(getResource("hardness"), BlockVariable.HARDNESS.getLoader());
      BlockVariable.LOADER.register(getResource("state_property"), StatePropertyVariable.LOADER);
      // entity
      EntityVariable.LOADER.register(getResource("constant"), EntityVariable.Constant.LOADER);
      EntityVariable.LOADER.register(getResource("conditional"), ConditionalEntityVariable.LOADER);
      EntityVariable.LOADER.register(getResource("health"), EntityVariable.HEALTH.getLoader());
      EntityVariable.LOADER.register(getResource("height"), EntityVariable.HEIGHT.getLoader());
      EntityVariable.LOADER.register(getResource("attribute"), AttributeEntityVariable.LOADER);
      EntityVariable.LOADER.register(getResource("effect_level"), EntityEffectLevelVariable.LOADER);
      EntityVariable.LOADER.register(getResource("light"), EntityLightVariable.LOADER);
      EntityVariable.LOADER.register(getResource("equipment_count"), EquipmentCountEntityVariable.LOADER);
      EntityVariable.LOADER.register(getResource("biome_temperature"), EntityVariable.BIOME_TEMPERATURE.getLoader());
      EntityVariable.LOADER.register(getResource("water"), EntityVariable.WATER.getLoader());
      EntityVariable.LOADER.register(getResource("armor_coverage"), EntityVariable.ARMOR_COVERAGE.getLoader());
      EntityVariable.LOADER.register(getResource("player_stat"), PlayerStatVariable.LOADER);
      // tool
      ToolVariable.LOADER.register(getResource("constant"), ToolVariable.Constant.LOADER);
      ToolVariable.register(getResource("tool_conditional"), ConditionalToolVariable.LOADER);
      ToolVariable.register(getResource("tool_durability"), ToolVariable.CURRENT_DURABILITY.getLoader());
      ToolVariable.register(getResource("tool_lost_durability"), ToolVariable.CURRENT_DAMAGE.getLoader());
      ToolVariable.register(getResource("tool_stat"), ToolStatVariable.LOADER);
      ToolVariable.register(getResource("stat_multiplier"), StatMultiplierVariable.LOADER);
      ToolVariable.register(getResource("mod_data"), ModDataVariable.LOADER);
      ToolVariable.register(getResource("modifier_level"), ModifierLevelVariable.LOADER);
      // stat
      ConditionalStatVariable.LOADER.register(getResource("constant"), ConditionalStatVariable.Constant.LOADER);
      ConditionalStatVariable.register(getResource("entity"), EntityConditionalStatVariable.LOADER);
      // melee
      MeleeVariable.LOADER.register(getResource("constant"), MeleeVariable.Constant.LOADER);
      MeleeVariable.LOADER.register(getResource("entity"), EntityMeleeVariable.LOADER);
      // power
      PowerVariable.LOADER.register(getResource("constant"), PowerVariable.Constant.LOADER);
      PowerVariable.LOADER.register(getResource("entity"), EntityPowerVariable.LOADER);
      PowerVariable.LOADER.register(getResource("persistent_data"), PersistentDataPowerVariable.LOADER);
      // mining speed
      MiningSpeedVariable.LOADER.register(getResource("constant"), MiningSpeedVariable.Constant.LOADER);
      MiningSpeedVariable.LOADER.register(getResource("block"), BlockMiningSpeedVariable.LOADER);
      MiningSpeedVariable.LOADER.register(getResource("block_light"), BlockLightVariable.LOADER);
      MiningSpeedVariable.LOADER.register(getResource("biome_temperature"), BlockTemperatureVariable.LOADER);
      MiningSpeedVariable.LOADER.register(getResource("effective"), EffectiveMiningSpeedVariable.LOADER);
      // protection
      ProtectionVariable.LOADER.register(getResource("constant"), ProtectionVariable.Constant.LOADER);
      ProtectionVariable.LOADER.register(getResource("entity"), EntityProtectionVariable.LOADER);

      // tank helper
      ToolTankHelper.LOADABLE.register(getResource("tank"), ToolTankHelper.TANK_HELPER);
      ToolTankHelper.LOADABLE.register(getResource("smashing"), SmashingModule.TANK_HELPER);
    }
  }

  @SubscribeEvent
  void commonSetup(final FMLCommonSetupEvent event) {
    TinkerDataCapability.register();
    PersistentDataCapability.register();
    EntityModifierCapability.register();
    BlockItemProviderCapability.register();
    // by default, we support modifying projectiles (arrows or fireworks mainly, but maybe other stuff). other entities may come in the future
    EntityModifierCapability.registerEntityPredicate(entity -> entity instanceof Projectile);
  }

  /** Adds all relevant items to the creative tab, called by general */
  public static void addTabItems(ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
    output.accept(silkyCloth);
    // dragon scale is handled by world
    output.accept(emeraldReinforcement);
    output.accept(slimesteelReinforcement);
    output.accept(TinkerTables.pattern, TabVisibility.PARENT_TAB_ONLY); // extra listing of pattern, also in table as you need it for part builder usage
    output.accept(ironReinforcement);
    output.accept(searedReinforcement);
    output.accept(goldReinforcement);
    output.accept(cobaltReinforcement);
    output.accept(obsidianReinforcement);
    creativeSlotItem.get().addVariants(output::accept);
    // modifier crystal is handled by tool parts tab
  }
}
