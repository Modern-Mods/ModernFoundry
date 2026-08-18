package modernmods.modernfoundry.library.tools.item.armor;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.item.component.TooltipDisplay;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbility;
import modernmods.modernfoundry.compat.neoforged.neoforge.capabilities.ICapabilityProvider;
import modernmods.mantle.client.SafeClientAccess;
import modernmods.mantle.client.TooltipKey;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.behavior.EnchantmentModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import modernmods.modernfoundry.library.modifiers.hook.interaction.SlotStackModifierHook;
import modernmods.modernfoundry.library.modifiers.modules.build.RarityModule;
import modernmods.modernfoundry.library.tools.IndestructibleItemEntity;
import modernmods.modernfoundry.library.tools.capability.ToolCapabilityProvider;
import modernmods.modernfoundry.library.tools.capability.inventory.ToolInventoryCapability;
import modernmods.modernfoundry.library.tools.definition.ModifiableArmorMaterial;
import modernmods.modernfoundry.library.tools.definition.ToolDefinition;
import modernmods.modernfoundry.library.tools.definition.module.display.ToolNameHook;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.helper.ToolBuildHandler;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.helper.TooltipUtil;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;
import modernmods.modernfoundry.library.tools.nbt.StatsNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.library.utils.TagUtil;
import modernmods.modernfoundry.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ModifiableArmorItem extends Item implements IModifiableDisplay {
  /** Volatile modifier tag to make piglins neutal when worn */
  public static final Identifier PIGLIN_NEUTRAL = TConstruct.getResource("piglin_neutral");
  /** Volatile modifier tag to make this item an elytra */
  public static final Identifier ELYTRA = TConstruct.getResource("elyta");
  /** Volatile flag for a boot item to walk on powdered snow. Cold immunity is handled through a tag */
  public static final Identifier SNOW_BOOTS = TConstruct.getResource("snow_boots");
  /** Volatile flag for an item to act as an enderman mask, stopping them from getting angry. */
  public static final Identifier ENDERMASK = TConstruct.getResource("endermask");

  @Getter
  private final ToolDefinition toolDefinition;
  /** Armor slot type this item occupies */
  private final ArmorType armorType;
  /** Cache of the tool built for rendering */
  private ItemStack toolForRendering = null;
  public ModifiableArmorItem(ArmorMaterial materialIn, ArmorType type, Properties builderIn, ToolDefinition toolDefinition) {
    super(armorProperties(builderIn, materialIn, type));
    this.armorType = type;
    this.toolDefinition = toolDefinition;
  }

  /**
   * Applies the humanoid armor properties. Mirrors {@link Properties#humanoidArmor(ArmorMaterial, ArmorType)} but skips
   * the enchantable component when the material's enchantment value is zero: Tinkers armor is enchanted through its own
   * modifier system, and 26.1's {@code Enchantable} rejects a non-positive value (which would crash registration).
   */
  private static Properties armorProperties(Properties props, ArmorMaterial material, ArmorType type) {
    props.durability(type.getDurability(material.durability()))
         .attributes(material.createAttributes(type))
         .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()).setEquipSound(material.equipSound()).setAsset(material.assetId()).build())
         .repairable(material.repairIngredient());
    if (material.enchantmentValue() > 0) {
      props.enchantable(material.enchantmentValue());
    }
    return props;
  }

  public ModifiableArmorItem(ModifiableArmorMaterial material, ArmorType type, Properties properties) {
    this(material.getArmorMaterial(), type, properties, Objects.requireNonNull(material.getArmorDefinition(type), "Missing tool definition for " + type.getName()));
  }

  /** Gets the equipment slot this armor occupies */
  public EquipmentSlot getEquipmentSlot() {
    return armorType.getSlot();
  }

  /* Basic properties */

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
    return ModifierUtil.checkVolatileFlag(stack, PIGLIN_NEUTRAL);
  }

  @Override
  public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
    return armorType == ArmorType.BOOTS && ModifierUtil.checkVolatileFlag(stack, SNOW_BOOTS);
  }

  // Note: NeoForge's isEnderMask hook was removed in 26.1; endermen aggro suppression is now driven by the
  // equippable data component (allowed_entities / camera_overlay). The ENDERMASK modifier flag applies that component.

  @Override
  public boolean canPerformAction(ItemInstance stack, ItemAbility toolAction) {
    return stack instanceof ItemStack itemStack && ModifierUtil.canPerformAction(ToolStack.from(itemStack), toolAction);
  }

  @Override
  public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
    return true;
  }


  /* Enchantments */

  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    return enchantment.is(EnchantmentTags.CURSE) && super.supportsEnchantment(stack, enchantment);
  }

  @Override
  public int getEnchantmentLevel(ItemInstance stack, Holder<Enchantment> enchantment) {
    return stack instanceof ItemStack itemStack ? EnchantmentModifierHook.getEnchantmentLevel(itemStack, enchantment) : 0;
  }

  @Override
  public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
    return EnchantmentModifierHook.getAllEnchantments(stack, lookup);
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

  @Override
  public InteractionResult use(Level levelIn, Player playerIn, InteractionHand handIn) {
    if (playerIn.isCrouching()) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      InteractionResult result = ToolInventoryCapability.tryOpenContainer(stack, null, getToolDefinition(), playerIn, Util.getSlotType(handIn));
      if (result.consumesAction()) {
        return result;
      }
    }
    return super.use(levelIn, playerIn, handIn);
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
  public Entity createEntity(Level level, Entity original, ItemStack stack) {
    return IndestructibleItemEntity.createFrom(level, original, stack);
  }


  /* Damage/Durability */

  @Override
  public boolean isCombineRepairable(ItemStack stack) {
    // handle in the tinker station, not the anvil or grindstone
    return false;
  }

  public boolean canBeDepleted() {
    return true;
  }

  @Override
  public boolean isDamageable(ItemStack stack) {
    return true;
  }

  @Override
  public int getMaxDamage(ItemStack stack) {
    return ToolDamageUtil.getFakeMaxDamage(stack);
  }

  @Override
  public int getDamage(ItemStack stack) {
    if (!canBeDepleted()) {
      return 0;
    }
    return ToolStack.from(stack).getDamage();
  }

  @Override
  public void setDamage(ItemStack stack, int damage) {
    if (canBeDepleted()) {
      ToolStack.from(stack).setDamage(damage);
    }
  }

  @Override
  public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T damager, Consumer<Item> onBroken) {
    // We basically emulate Itemstack.damageItem here. We always return 0 to skip the handling in ItemStack.
    // If we don't tools ignore our damage logic
    if (canBeDepleted() && ToolDamageUtil.damage(ToolStack.from(stack), amount, damager, stack)) {
      onBroken.accept(stack.getItem());
    }

    return 0;
  }


  /* Durability display */

  @Override
  public boolean isBarVisible(ItemStack pStack) {
    return DurabilityDisplayModifierHook.showDurabilityBar(pStack);
  }

  @Override
  public int getBarColor(ItemStack pStack) {
    return DurabilityDisplayModifierHook.getDurabilityRGB(pStack);
  }

  @Override
  public int getBarWidth(ItemStack pStack) {
    return DurabilityDisplayModifierHook.getDurabilityWidth(pStack);
  }


  /* Armor properties */

  @Override
  public Multimap<Attribute,AttributeModifier> getAttributeModifiers(IToolStackView tool, EquipmentSlot slot) {
    if (slot != getEquipmentSlot()) {
      return ImmutableMultimap.of();
    }

    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    if (!tool.isBroken()) {
      // base stats
      StatsNBT statsNBT = tool.getStats();
      float armor = statsNBT.get(ToolStats.ARMOR);
      if (armor > 0) {
        builder.put(Attributes.ARMOR.value(), new AttributeModifier(TConstruct.getResource("armor/" + armorType.getName() + "/armor"), armor, AttributeModifier.Operation.ADD_VALUE));
      }
      float toughness = statsNBT.get(ToolStats.ARMOR_TOUGHNESS);
      if (toughness > 0) {
        builder.put(Attributes.ARMOR_TOUGHNESS.value(), new AttributeModifier(TConstruct.getResource("armor/" + armorType.getName() + "/toughness"), toughness, AttributeModifier.Operation.ADD_VALUE));
      }
      double knockbackResistance = statsNBT.get(ToolStats.KNOCKBACK_RESISTANCE);
      if (knockbackResistance > 0) {
        builder.put(Attributes.KNOCKBACK_RESISTANCE.value(), new AttributeModifier(TConstruct.getResource("armor/" + armorType.getName() + "/knockback_resistance"), knockbackResistance, AttributeModifier.Operation.ADD_VALUE));
      }
      // grab attributes from modifiers
      BiConsumer<Attribute,AttributeModifier> attributeConsumer = builder::put;
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.ATTRIBUTES).addAttributes(tool, entry, slot, attributeConsumer);
      }
    }

    return builder.build();
  }

  public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
    if (slot != getEquipmentSlot() || !TagUtil.hasTag(stack)) {
      return ImmutableMultimap.of();
    }
    return getAttributeModifiers(ToolStack.from(stack), slot);
  }

  @Override
  public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
    ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
    EquipmentSlot slot = getEquipmentSlot();
    getAttributeModifiers(slot, stack).forEach((attribute, modifier) -> builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier, EquipmentSlotGroup.bySlot(slot)));
    return builder.build();
  }


  /* Elytra */

  // Note: NeoForge's canElytraFly / elytraFlightTick item hooks were removed in 26.1; gliding is now driven by the
  // glider data component (DataComponents.GLIDER) applied by the ELYTRA modifier, with equipment asset controlling wings.
  // The per-tick durability drain and ELYTRA_FLIGHT modifier hook handling needs reattaching to the glider tick pipeline;
  // deferred to the armor/equipment component pass and flagged for in-game validation.


  /* Ticking */

  @Override
  public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel levelIn, Entity entityIn, @javax.annotation.Nullable EquipmentSlot equipmentSlot) {
    // don't care about non-living, they skip most tool context
    if (entityIn instanceof LivingEntity living) {
      ToolStack tool = ToolStack.from(stack);
      // 26.1 inventoryTick is server-only
      tool.ensureHasData();
      List<ModifierEntry> modifiers = tool.getModifierList();
      if (!modifiers.isEmpty()) {
        boolean isCorrectSlot = living.getItemBySlot(getEquipmentSlot()) == stack;
        // 26.1 gives an EquipmentSlot instead of index/selected; derive the legacy flags for the modifier hook
        boolean isSelected = equipmentSlot == EquipmentSlot.MAINHAND;
        int itemSlot = equipmentSlot != null ? equipmentSlot.getIndex() : 0;
        // we pass in the stack for most custom context, but for the sake of armor its easier to tell them that this is the correct slot for effects
        for (ModifierEntry entry : modifiers) {
          entry.getHook(ModifierHooks.INVENTORY_TICK).onInventoryTick(tool, entry, levelIn, living, itemSlot, isSelected, isCorrectSlot, stack);
        }
      }
    }
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
    return SlotStackModifierHook.overrideStackedOnOther(held, slot, action, player) || super.overrideStackedOnOther(held, slot, action, player);
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack slotStack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    return SlotStackModifierHook.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access) || super.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access);
  }


  /* Tooltips */

  @Override
  public Component getName(ItemStack stack) {
    return ToolNameHook.getName(getToolDefinition(), stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipConsumer, TooltipFlag flag) {
    List<Component> tooltip = new java.util.ArrayList<>();
    Level level = context.registries() == null ? null : SafeClientAccess.getLevel();
    TooltipUtil.addInformation(this, stack, level, tooltip, SafeClientAccess.getTooltipKey(), flag);
  
    tooltip.forEach(tooltipConsumer);
  }

  @Override
  public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
    tooltips = TooltipUtil.getArmorStats(tool, player, tooltips, key, tooltipFlag);
    TooltipUtil.addAttributes(this, tool, player, tooltips, TooltipUtil.SHOW_ARMOR_ATTRIBUTES, getEquipmentSlot());
    return tooltips;
  }

  public int getDefaultTooltipHideFlags(ItemStack stack) {
    return TooltipUtil.getModifierHideFlags(getToolDefinition());
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
