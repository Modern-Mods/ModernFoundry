package modernmods.modernfoundry.integrations.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public class IfdHelper {

    public static void shootGhostSword(Player player, float damage) {
        EntityType<?> type = OptionalIntegrationHelper.registry(BuiltInRegistries.ENTITY_TYPE, "iceandfire:ghost_sword");
        Object created = OptionalIntegrationHelper.newInstance(
                "com.github.alexthe666.iceandfire.entity.EntityGhostSword", type, player.level(), player, damage * 0.5F);
        Entity ghostSword = created instanceof Entity entity ? entity : OptionalIntegrationHelper.create(type, player.level());
        if (ghostSword == null) return;

        OptionalIntegrationHelper.invoke(ghostSword, "shootFromRotation", player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 0.5F);
        player.level().addFreshEntity(ghostSword);
    }

}
