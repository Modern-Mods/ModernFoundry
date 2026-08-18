package modernmods.modernfoundry.tools.modules.interaction;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import modernmods.mantle.data.loadable.primitive.IntLoadable;
import modernmods.mantle.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InteractionSource;
import modernmods.modernfoundry.library.modifiers.hook.mining.RemoveBlockModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.context.ToolHarvestContext;
import modernmods.modernfoundry.library.tools.definition.module.ToolHooks;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.shared.TinkerCommons;

import javax.annotation.Nullable;
import java.util.List;

/** Module to place a glow on right click */
public record PlaceGlowModule(int damage) implements ModifierModule, BlockInteractionModifierHook, RemoveBlockModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<PlaceGlowModule>defaultHooks(ModifierHooks.BLOCK_INTERACT, ModifierHooks.REMOVE_BLOCK);
  public static final RecordLoadable<PlaceGlowModule> LOADER = RecordLoadable.create(
    IntLoadable.FROM_ZERO.requiredField("tool_damage", PlaceGlowModule::damage),
    PlaceGlowModule::new);

  @Override
  public RecordLoadable<PlaceGlowModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context, InteractionSource source) {
    if (!tool.isBroken() && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, modifier.getId(), source)) {
      Player player = context.getPlayer();
      if (!context.getLevel().isClientSide()) {
        Level world = context.getLevel();
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(face);
        if (TinkerCommons.glowBlock.get().addGlow(world, pos, face.getOpposite())) {
          // damage the tool, showing animation if relevant
          if (damage > 0 && ToolDamageUtil.damage(tool, damage, player, context.getItemInHand(), modifier.getId()) && player != null) {
            player.onEquippedItemBroken(context.getItemInHand().getItem(), source.getSlot(context.getHand()));
          }
          world.playSound(null, pos, world.getBlockState(pos).getSoundType(world, pos, player).getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
      }
      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }

  @Nullable
  @Override
  public Boolean removeBlock(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
    // if we have left click modifiers active, ensure we don't break the block on left click
    // otherwise our newly placed block is immediately removed
    if (context.getState().is(TinkerCommons.glowBlock.get()) && tool.hasTag(TinkerTags.Items.INTERACTABLE_LEFT) && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, modifier.getId(), InteractionSource.LEFT_CLICK)) {
      return false;
    }
    return null;
  }
}
