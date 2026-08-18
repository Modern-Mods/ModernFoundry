package modernmods.modernfoundry.tools.modifiers.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import modernmods.modernfoundry.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerEffects;
import modernmods.modernfoundry.tools.TinkerModifiers;

import java.util.function.Consumer;

/** Effect for rendering the charge up when you start using a helmet */
public class HelmetChargingEffect extends MobEffect {
  public HelmetChargingEffect() {
    super(MobEffectCategory.NEUTRAL, -1);
  }

  // initializeClient removed from Item/MobEffect/FluidType in 26.1; registered via RegisterClientExtensionsEvent
  public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
    consumer.accept(new IClientMobEffectExtensions() {
      @Override
      public boolean isVisibleInInventory(MobEffectInstance instance) {
        return false;
      }
      // Minimal placeholder: the custom charge-bar GUI icon overlay (GuiGraphics#blit(sprite) / innerBlit and
      // MobEffectTextureManager sprite lookup) is part of the removed 26.1.2 GUI/render pipeline. Deferring to the
      // default vanilla effect-icon rendering pending the render-system rewrite.
    });
  }


  /* Helpers */

  /** Starts using the helmet with the charge time rendering */
  public static int startUsingHelmet(IToolStackView tool, LivingEntity living, float speedFactor) {
    int time = GeneralInteractionModifierHook.startDrawing(tool, living, speedFactor);
    living.addEffect(new MobEffectInstance(TinkerEffects.holder(TinkerModifiers.helmetCharging), time + 20, 0, true, false, true));
    return time;
  }
}
