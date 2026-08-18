package modernmods.modernfoundry.library.tools.item;

import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ICapabilityProvider;
import modernmods.mantle.client.SafeClientAccess;
import modernmods.mantle.client.TooltipKey;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.hook.build.ConditionalStatModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.InventoryTickModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.SlotStackModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.build.RarityModule;
import modernmods.modernfoundry.library.tools.IndestructibleItemEntity;
import modernmods.modernfoundry.library.tools.capability.ToolCapabilityProvider;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.display.ToolNameHook;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolBuildHandler;
import modernmods.modernfoundry.library.tools.helper.TooltipUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tools.entity.ModifiableArrow;

import javax.annotation.Nullable;
import java.util.List;

/** Modifiable item that is usable as arrows in a bow */
public class ModifiableArrowItem extends ArrowItem implements IModifiableDisplay {
  /** Tool definition for the given tool */
  @Getter
  private final ToolDefinition toolDefinition;
  /** Cached tool for rendering on UIs */
  private ItemStack toolForRendering;

  public ModifiableArrowItem(Properties props, ToolDefinition toolDefinition) {
    super(props);
    this.toolDefinition = toolDefinition;
  }


  /* Arrowing */

  @Override
  public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter, @Nullable ItemStack weapon) {
    ModifiableArrow arrow = new ModifiableArrow(level, shooter);
    arrow.onCreate(stack, shooter);
    return arrow;
  }

  @Override
  public boolean isInfinite(ItemStack stack, ItemStack bow, LivingEntity player) {
    return false;
  }


  /* Shurikening */

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    // only throw arrows if they have the throwable tool action. Useful for the other style of projectile in addons, or a really weird arrow modifier.
    if (stack.is(TinkerTags.Items.THROWN_AMMO)) {
      level.playSound(null, player.getX(), player.getY(), player.getZ(), Sounds.SHURIKEN_THROW.getSound(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
      player.getCooldowns().addCooldown(stack, 10);
      if (!level.isClientSide()) {
        ModifiableArrow arrow = new ModifiableArrow(level, player);
        IToolStackView tool = arrow.onCreate(stack, player);
        float velocity = ConditionalStatModifierHook.getModifiedStat(tool, player, ToolStats.VELOCITY);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);
        level.addFreshEntity(arrow);
      }

      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }

      return InteractionResult.SUCCESS;
    }
    return InteractionResult.PASS;
  }


  /* Loading */

  @Nullable
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new ToolCapabilityProvider(stack);
  }

  public void verifyTagAfterLoad(CompoundTag nbt) {
    ToolStack.verifyTag(this, nbt, getToolDefinition());
  }

  @Override
  public void onCraftedBy(ItemStack stack, Player playerIn) {
    ToolStack.ensureInitialized(stack, getToolDefinition());
  }


  /* Display */

  @Override
  public boolean isFoil(ItemStack stack) {
    // we use enchantments to handle some modifiers, so don't glow from them
    // however, if a modifier wants to glow let them
    return ModifierUtil.checkVolatileFlag(stack, SHINY);
  }

  public Rarity getRarity(ItemStack stack) {
    return RarityModule.getRarity(stack);
  }


  /* Indestructible items */

  @Override
  public boolean hasCustomEntity(ItemStack stack) {
    return IndestructibleItemEntity.hasCustomEntity(stack);
  }

  @Nullable
  @Override
  public Entity createEntity(Level world, Entity original, ItemStack stack) {
    return IndestructibleItemEntity.createFrom(world, original, stack);
  }


  /* Modifier interactions */

  @Override
  public void inventoryTick(ItemStack stack, ServerLevel worldIn, Entity entityIn, @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slot) {
    InventoryTickModifierHook.heldInventoryTick(stack, worldIn, entityIn, slot != null ? slot.getIndex() : 0, slot == net.minecraft.world.entity.EquipmentSlot.MAINHAND);
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
    return SlotStackModifierHook.overrideStackedOnOther(held, slot, action, player);
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack slotStack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    return SlotStackModifierHook.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access);
  }


  /* Tooltips */

  @Override
  public Component getName(ItemStack stack) {
    return ToolNameHook.getName(getToolDefinition(), stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    TooltipUtil.addInformation(this, stack, context.level(), tooltip, SafeClientAccess.getTooltipKey(), flag);
  
    tooltip.forEach(tooltipConsumer);
  }

  public int getDefaultTooltipHideFlags(ItemStack stack) {
    return TooltipUtil.getModifierHideFlags(getToolDefinition());
  }

  @Override
  public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
    return TooltipUtil.getAmmoStats(tool, player, tooltips, key, tooltipFlag);
  }

  /* Display items */

  @Override
  public ItemStack getRenderTool() {
    if (toolForRendering == null) {
      toolForRendering = ToolBuildHandler.buildToolForRendering(this, this.getToolDefinition());
    }
    return toolForRendering;
  }
}
