package modernmods.modernfoundry.library.json.variable.protection;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.mantle.data.registry.GenericLoaderRegistry;
import modernmods.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import modernmods.modernfoundry.library.json.variable.VariableLoaderRegistry;
import modernmods.modernfoundry.library.tools.context.EquipmentContext;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;

/** Variable for use in {@link modernmods.modernfoundry.library.modifiers.modules.armor.ProtectionModule} */
public interface ProtectionVariable extends IHaveLoader {
  GenericLoaderRegistry<ProtectionVariable> LOADER = new VariableLoaderRegistry<>("Protection Variable", Constant::new);

  /**
   * Gets the value of the variable
   * @param tool     Tool instance
   * @param context  Equipment context, will be null in tooltips
   * @param target   Entity using the tool, may be null conditionally in tooltips
   * @param source   Damage source, will be null in tooltips
   * @param slotType Slot type, will be null in tooltips
   * @return  Value of this variable
   */
  float getValue(IToolStackView tool, @Nullable EquipmentContext context, @Nullable LivingEntity target, @Nullable DamageSource source, @Nullable EquipmentSlot slotType);

  /** Constant value instance for this object */
  record Constant(float value) implements VariableLoaderRegistry.ConstantFloat, ProtectionVariable {
    public static final RecordLoadable<Constant> LOADER = VariableLoaderRegistry.constantLoader(Constant::new);

    @Override
    public float getValue(IToolStackView tool, @Nullable EquipmentContext context, @Nullable LivingEntity target, @Nullable DamageSource source, @Nullable EquipmentSlot slotType) {
      return value;
    }

    @Override
    public RecordLoadable<Constant> getLoader() {
      return LOADER;
    }
  }
}
