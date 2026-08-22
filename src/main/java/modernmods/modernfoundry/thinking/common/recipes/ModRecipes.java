package modernmods.modernfoundry.thinking.common.recipes;

import modernmods.hilt.registration.deferred.SynchronizedDeferredRegister;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.thinking.common.register.ModModule;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Native recipe registrations for Tinkers' Thinking content. */
public final class ModRecipes extends ModModule {
    private static final SynchronizedDeferredRegister<RecipeType<?>> TYPES =
            SynchronizedDeferredRegister.create(Registries.RECIPE_TYPE, TConstruct.MOD_ID);

    public static final DeferredHolder<? super RecipeType<DryingRackRecipes>, RecipeType<DryingRackRecipes>> DRYING_RACK_TYPE =
            TYPES.register("drying_rack", () -> DryingRackRecipes.Type.INSTANCE);
    public static final DeferredHolder<? super RecipeSerializer<DryingRackRecipes>, RecipeSerializer<DryingRackRecipes>> DRYING_RACK =
            RECIPE_SERIALIZERS.register("drying_rack", () -> DryingRackRecipes.Serializer.INSTANCE);

    public static void init(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
