package modernmods.modernfoundry.integrations.data.tcon.fluid;

import com.hollingsworth.arsnouveau.setup.registry.ModPotions;

import com.sammy.malum.registry.common.MobEffectRegistry;

import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;

import net.neoforged.neoforge.common.crafting.conditions.ModLoadedCondition;

import quek.undergarden.registry.UGEffects;

import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.integrations.common.TinkerDamageTypes;
import modernmods.modernfoundry.library.data.tinkering.AbstractFluidEffectProvider;
import modernmods.modernfoundry.library.modifiers.fluid.FluidEffect;
import modernmods.modernfoundry.library.modifiers.fluid.FluidMobEffect;
import modernmods.modernfoundry.library.modifiers.fluid.TimeAction;

import modernmods.modernfoundry.integrations.data.integration.ModIntegration;
import modernmods.modernfoundry.integrations.items.TciItems;

public class FluidEffectProvider extends AbstractFluidEffectProvider {

    public FluidEffectProvider(PackOutput packOutput, String modId) {
        super(packOutput, modId);
    }

    @Override
    public String getName() {
        return "TciIntegration - TCon Fluid Effect Provider";
    }

    @Override
    protected void addFluids() {
        addMetal(TciItems.MOLTEN_MANASTEEL)
            .addCondition(new ModLoadedCondition(ModIntegration.BOTANIA_MODID))
            .magicDamage(2F)
            .addEffect(FluidMobEffect.builder().effect(ModPotions.RECOVERY_EFFECT.get(), 20 * 6, 2), TimeAction.SET);
        addMetal(TciItems.MOLTEN_NEPTUNIUM)
            .addCondition(new ModLoadedCondition(ModIntegration.AQUACULTURE_MODID))
            .fireDamage(2F)
            .addDamage(LivingEntityPredicate.WATER_SENSITIVE, 2F, TinkerDamageTypes.WATER)
            .addEntityEffect(FluidEffect.EXTINGUISH_FIRE);
        addGem(TciItems.MOLTEN_SOURCE_GEM)
            .addCondition(new ModLoadedCondition(ModIntegration.ARS_MODID))
            .addEffect(FluidMobEffect.builder().effect(ModPotions.RECOVERY_EFFECT.get(), 20 * 6, 2), TimeAction.SET);
        addMetal(TciItems.MOLTEN_SOUL_STAINED_STEEL)
            .addCondition(new ModLoadedCondition(ModIntegration.MALUM_MODID))
            .magicDamage(2F)
            .addEffect(FluidMobEffect.builder().effect(MobEffectRegistry.GLUTTONY.get(), 20 * 10, 2), TimeAction.SET);
        addMetal(TciItems.MOLTEN_CLOGGRUM)
            .addCondition(new ModLoadedCondition(ModIntegration.UNDERGARDEN_MODID))
            .addEffect(FluidMobEffect.builder().effect(UGEffects.GOOEY.get(), 20 * 5, 2), TimeAction.SET);
        addMetal(TciItems.MOLTEN_FROSTSTEEL)
            .addCondition(new ModLoadedCondition(ModIntegration.UNDERGARDEN_MODID))
            .addEffect(FluidMobEffect.builder().effect(UGEffects.CHILLY.get(), 20 * 4, 2), TimeAction.SET);
        addMetal(TciItems.MOLTEN_FORGOTTEN_METAL)
            .addCondition(new ModLoadedCondition(ModIntegration.UNDERGARDEN_MODID))
            .magicDamage(2F)
            .addEffect(FluidMobEffect.builder().effect(MobEffects.ABSORPTION, 20 * 20, 2), TimeAction.SET);
    }

}
