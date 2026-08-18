package modernmods.modernfoundry.tools.modules.armor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.json.LevelingValue;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.item.IModifiable;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/** Module that places fire as you walk */
public record FireWalkerModule(LevelingValue radius) implements ModifierModule, ArmorWalkRadiusModule<Void> {
  public static final RecordLoadable<FireWalkerModule> LOADER = RecordLoadable.create(LevelingValue.LOADABLE.requiredField("radius", FireWalkerModule::radius), FireWalkerModule::new);

  @Override
  public RecordLoadable<FireWalkerModule> getLoader() {
    return LOADER;
  }

  @Override
  public float getRadius(IToolStackView tool, ModifierEntry modifier) {
    return radius.compute(modifier.getEffectiveLevel() + tool.getVolatileData().getInt(IModifiable.EXPANDED));
  }

  @Override
  public boolean walkOn(IToolStackView tool, ModifierEntry entry, LivingEntity living, Level world, BlockPos target, MutableBlockPos mutable, Void context) {
    if (BaseFireBlock.canBePlacedAt(world, target, living.getDirection())) {
      world.playSound(null, target, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, living.level().getRandom().nextFloat() * 0.4F + 0.8F);
      world.setBlock(target, BaseFireBlock.getState(world, target), Block.UPDATE_ALL_IMMEDIATE);
      ToolDamageUtil.damageAnimated(tool, 1, living, EquipmentSlot.FEET, entry.getId());
    }
    return tool.isBroken();
  }
}
