package modernmods.modernfoundry.integrations.items.modifiers.armor;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.armor.ArmorWalkModifierHook;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.integrations.util.OptionalIntegrationHelper;

public class MosquitoModifier extends Modifier implements ArmorWalkModifierHook {

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.BOOT_WALK);
    }

    @Override
    public Component getDisplayName(int level) {
        return super.getDisplayName();
    }

    public void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos prevPos, BlockPos newPos) {
        if ((living.tickCount & 1) == 0 && !tool.isBroken()) {
            float level = modifier.getEffectiveLevel();

            if (level > 0) {
                applyAnnoyance(living, level);
            }
        }
    }

    public static void applyAnnoyance(LivingEntity living, float level) {
        double x = living.getX();
        double y = living.getY();
        double z = living.getZ();
        float range = 3F + level;

        List<LivingEntity> entities = living.level().getEntitiesOfClass(LivingEntity.class, new AABB(x - range, y - range, z - range, x + range, y + range, z + range));

        if (!entities.isEmpty() && living.tickCount % 100 == 0) {
            var sound = OptionalIntegrationHelper.sound("alexsmobs:mosquito_loop");
            if (sound != null) living.playSound(sound, 1.0F, 1.0F);
        }

        for (LivingEntity livingEntity : entities) {
            if (!livingEntity.equals(living) && living.tickCount % 20 == 0) {
                for (int i = 0; i < 3; i++) {
                    livingEntity.level().addParticle(ParticleTypes.CRIMSON_SPORE, livingEntity.getRandomX(1.0), livingEntity.getRandomY(), livingEntity.getRandomZ(1.0), 0, 0, 0);
                }
            }
        }
    }

}

