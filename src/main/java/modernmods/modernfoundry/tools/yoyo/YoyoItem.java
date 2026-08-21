package modernmods.modernfoundry.tools.yoyo;

import modernmods.modernfoundry.tools.yoyo.api.BlockInteraction;
import modernmods.modernfoundry.tools.yoyo.api.EntityInteraction;
import modernmods.modernfoundry.tools.yoyo.api.IYoyo;
import modernmods.modernfoundry.tools.yoyo.api.RenderOrientation;
import modernmods.modernfoundry.tools.yoyo.api.YoyoFactory;
import modernmods.modernfoundry.tools.yoyo.YoyoDataComponents;
import modernmods.modernfoundry.tools.yoyo.YoyoEnchantments;
import modernmods.modernfoundry.common.Sounds;
import modernmods.modernfoundry.tools.TinkerTools;
import modernmods.modernfoundry.tools.network.YoyoHandSyncPacket;
import modernmods.modernfoundry.common.network.TinkerNetwork;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Tool.Rule;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YoyoItem extends TieredItem implements IYoyo {
   private final Float defaultAttackDamage;
   protected final YoyoFactory factory;
   protected RenderOrientation renderOrientation = RenderOrientation.Vertical;
   protected List<EntityInteraction> entityInteractions = new ArrayList<>();
   protected List<BlockInteraction> blockInteractions = new ArrayList<>();
   protected YoyoTier yoyoTier;

   public YoyoItem(Properties properties, YoyoTier tier, YoyoFactory factory) {
      super(
         tier.getTier(),
         properties.component(
            DataComponents.TOOL,
            new Tool(
               List.of(
                  Rule.deniesDrops(tier.getTier().getIncorrectBlocksForDrops()),
                  Rule.minesAndDrops(BlockTags.MINEABLE_WITH_PICKAXE, tier.getTier().getSpeed()),
                  Rule.minesAndDrops(BlockTags.MINEABLE_WITH_AXE, tier.getTier().getSpeed()),
                  Rule.minesAndDrops(BlockTags.MINEABLE_WITH_HOE, tier.getTier().getSpeed()),
                  Rule.minesAndDrops(BlockTags.MINEABLE_WITH_SHOVEL, tier.getTier().getSpeed())
               ),
               1.0F,
               1
            )
         )
      );
      this.yoyoTier = tier;
      this.defaultAttackDamage = tier.getTier().getAttackDamageBonus() + 3.0F;
      this.factory = factory;
      this.entityInteractions.addAll(tier.getEntityInteractions());
      this.blockInteractions.addAll(tier.getBlockInteractions());
   }

   public YoyoItem(Properties properties, YoyoTier tier) {
      this(properties, tier, YoyoEntity::new);
   }

   public YoyoItem(YoyoTier tier) {
      this(new Properties().stacksTo(1), tier);
   }

   public YoyoItem addBlockInteraction(BlockInteraction... interaction) {
      Collections.addAll(this.blockInteractions, interaction);
      return this;
   }

   public YoyoItem addEntityInteraction(EntityInteraction... interaction) {
      Collections.addAll(this.entityInteractions, interaction);
      return this;
   }

   public YoyoItem setRenderOrientation(RenderOrientation renderOrientation) {
      this.renderOrientation = renderOrientation;
      return this;
   }

   public void appendHoverText(ItemStack stack, @Nullable TooltipContext p_41422_, List<Component> tooltips, TooltipFlag p_41424_) {
      super.appendHoverText(stack, p_41422_, tooltips, p_41424_);
      tooltips.add(Component.translatable("tooltip.modernfoundry.yoyo.weight", new Object[]{this.getWeight(stack)}).withStyle(ChatFormatting.GRAY));
      tooltips.add(Component.translatable("tooltip.modernfoundry.yoyo.length", new Object[]{this.getLength(stack)}).withStyle(ChatFormatting.GRAY));
      int duration = this.getDuration(stack);
      if (duration < 0) {
         tooltips.add(Component.translatable("tooltip.modernfoundry.yoyo.duration.infinite").withStyle(ChatFormatting.GRAY));
      } else {
         tooltips.add(Component.translatable("tooltip.modernfoundry.yoyo.duration", new Object[]{duration / 20.0F}).withStyle(ChatFormatting.GRAY));
      }

      if (p_41422_ != null && this.getMaxCollectedDrops(stack, p_41422_.registries()) > 0) {
         tooltips.add(
            Component.translatable(
                  "tooltip.modernfoundry.yoyo.max_collected_items",
                  new Object[]{this.getMaxCollectedDrops(stack, p_41422_.registries()), this.getMaxCollectedDrops(stack, p_41422_.registries()) / 64}
               )
               .withStyle(ChatFormatting.GRAY)
         );
      }

      tooltips.add(
         Component.translatable("tooltip.modernfoundry.yoyo.open_menu", new Object[]{Component.translatable("key.modernfoundry.open_yoyo_config")}).withStyle(ChatFormatting.GRAY)
      );
      tooltips.add(Component.literal(""));
      MutableComponent attackComp = Component.translatable("tooltip.modernfoundry.yoyo.attack")
         .withStyle(ChatFormatting.GRAY)
         .append(Component.literal(": "))
         .append(Component.literal(isAttackEnable(stack) ? "✔" : "❌").withStyle(isAttackEnable(stack) ? ChatFormatting.GREEN : ChatFormatting.RED));
      tooltips.add(attackComp);
      if (p_41422_ != null) {
         addEnchantmentToTooltip(stack, YoyoEnchantments.COLLECTING, tooltips, p_41422_);
         addEnchantmentToTooltip(stack, YoyoEnchantments.BREAKING, tooltips, p_41422_);
         addEnchantmentToTooltip(stack, YoyoEnchantments.CRAFTING, tooltips, p_41422_);
      }
      if (stack.isEnchanted()) {
         tooltips.add(Component.literal(""));
      }
   }

   private static void addEnchantmentToTooltip(ItemStack stack, ResourceKey<Enchantment> ench, List<Component> tooltips, TooltipContext ctx) {
      addEnchantmentToTooltip(stack, ctx.registries().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ench), tooltips, ctx);
   }

   private static void addEnchantmentToTooltip(ItemStack stack, Holder<Enchantment> ench, List<Component> tooltips, TooltipContext ctx) {
      int level = stack.getEnchantmentLevel(ench);
      MutableComponent comp = ((Enchantment)ench.value())
         .description()
         .copy()
         .withStyle(ChatFormatting.GRAY)
         .append(Component.literal(": "))
         .append(
            Component.literal(isEnchantmentEnable(stack, ench, ctx.registries()) ? "✔" : "❌")
               .withStyle(isEnchantmentEnable(stack, ench.getKey(), ctx.registries()) ? ChatFormatting.GREEN : ChatFormatting.RED)
         );
      if (((Enchantment)ench.value()).getMaxLevel() > 1 && level > 0) {
         comp = comp.append(Component.literal(" (")).append(Component.translatable("enchantment.level." + level)).append(Component.literal(")"));
      }

      if (ench.equals(YoyoEnchantments.CRAFTING)) {
         comp.append(Component.literal(" (WIP, no usages)").withStyle(ChatFormatting.RED));
      }

      tooltips.add(comp);
   }

   public static boolean isAttackEnable(ItemStack stack) {
      return (Boolean)stack.getOrDefault(YoyoDataComponents.ATTACK, true);
   }

   public static void toggleAttack(ItemStack stack) {
      stack.set(YoyoDataComponents.ATTACK, !isAttackEnable(stack));
   }

   public static boolean isEnchantmentEnable(ItemStack stack, Holder<Enchantment> enchantment, Provider provider) {
      return isEnchantmentEnable(stack, enchantment.getKey(), provider);
   }

   public static boolean isEnchantmentEnable(ItemStack stack, ResourceKey<Enchantment> enchantment, Provider provider) {
      return ((EnchantmentsState)stack.getOrDefault(YoyoDataComponents.ENCHANTMENTS, EnchantmentsState.EMPTY))
         .isEnchantmentActivate(stack, enchantment, provider);
   }

   public static void toggleEnchant(ItemStack stack, ResourceKey<Enchantment> enchant, Provider provider) {
      EnchantmentsState state = ((EnchantmentsState)stack.getOrDefault(YoyoDataComponents.ENCHANTMENTS, EnchantmentsState.EMPTY)).copy();
      state.toggleEnchantment(enchant, stack, provider);
      stack.set(YoyoDataComponents.ENCHANTMENTS, state);
   }

   public static void toggleEnchant(ItemStack stack, ResourceKey<Enchantment> enchant, Provider provider, ServerPlayer player) {
      EnchantmentsState state = ((EnchantmentsState)stack.getOrDefault(YoyoDataComponents.ENCHANTMENTS, EnchantmentsState.EMPTY)).copy();
      InteractionHand hand = player.getItemInHand(InteractionHand.MAIN_HAND).equals(stack) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
      state.toggleEnchantment(enchant, stack, provider);
      stack.set(YoyoDataComponents.ENCHANTMENTS, state);
      TinkerNetwork.getInstance().sendTo(new YoyoHandSyncPacket(hand, stack), player);
   }

   public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
      return enchantment.equals(Enchantments.SWEEPING_EDGE) ? false : super.supportsEnchantment(stack, enchantment);
   }

   public boolean isFoil(ItemStack stack) {
      return stack.getItem() == TinkerTools.creativeYoyo.get() || super.isFoil(stack);
   }

   public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
      if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
         stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
      }

      return true;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      if (!level.isClientSide && stack.getDamageValue() <= stack.getMaxDamage()) {
         YoyoEntity yoyoEntity = YoyoEntity.CASTERS.get(player.getUUID());
         if (yoyoEntity == null) {
            yoyoEntity = this.factory.create(level, player, hand);
            level.addFreshEntity(yoyoEntity);
            level.playSound(
               null,
               yoyoEntity.getX(),
               yoyoEntity.getY(),
               yoyoEntity.getZ(),
               (SoundEvent)Sounds.YOYO_THROW.getSound(),
               SoundSource.NEUTRAL,
               0.5F,
               0.4F / (level.random.nextFloat() * 0.4F + 0.8F)
            );
            player.causeFoodExhaustion(0.05F);
         } else {
            yoyoEntity.setRetracting(!yoyoEntity.isRetracting());
         }
      }

      return InteractionResultHolder.success(stack);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
      return true;
   }

   public double getAttackDamage(ItemStack yoyo) {
      return this.yoyoTier.getDamage();
   }

   @NotNull
   public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
      return ItemAttributeModifiers.builder()
         .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, this.getAttackDamage(stack), Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
         .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, -2.4F, Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
         .build();
   }

   @Override
   public double getWeight(ItemStack yoyo) {
      return this.yoyoTier.getWeight();
   }

   @Override
   public double getLength(ItemStack yoyo) {
      return this.yoyoTier.getLength();
   }

   @Override
   public int getDuration(ItemStack yoyo) {
      return this.yoyoTier.getDuration();
   }

   @Override
   public int getAttackInterval(ItemStack yoyo) {
      return 10;
   }

   @Override
   public int getMaxCollectedDrops(ItemStack yoyo, Provider provider) {
      return this.calculateMaxCollectedDrops(yoyo.getEnchantmentLevel(provider.holderOrThrow(YoyoEnchantments.COLLECTING)));
   }

   @Override
   public <T extends LivingEntity> void damageItem(ItemStack yoyo, InteractionHand hand, int amount, T entity) {
      yoyo.hurtAndBreak(amount, entity, hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
   }

   @Override
   public void entityInteraction(ItemStack yoyoStack, Player player, InteractionHand hand, YoyoEntity yoyo, Entity target) {
      if (!target.level().isClientSide) {
         this.entityInteractions.forEach(i -> i.apply(yoyoStack, player, hand, yoyo, target));
      }
   }

   @Override
   public boolean interactsWithBlocks(ItemStack yoyo) {
      return !this.blockInteractions.isEmpty();
   }

   @Override
   public void blockInteraction(ItemStack yoyoStack, Player player, Level world, BlockPos pos, BlockState state, Block block, YoyoEntity yoyo) {
      if (!world.isClientSide) {
         this.blockInteractions.forEach(i -> i.apply(yoyoStack, player, pos, state, block, yoyo));
      }
   }

   @Override
   public RenderOrientation getRenderOrientation(ItemStack yoyo) {
      return this.renderOrientation;
   }

   private int calculateMaxCollectedDrops(int level) {
      if (level == 0) {
         return 0;
      }

      int mult = 1;

      for (int i = 0; i < level; i++) {
         mult *= 2;
      }

      return 64 * mult;
   }
}
