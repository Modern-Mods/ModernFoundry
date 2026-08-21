package modernmods.modernfoundry.tools.client;

import modernmods.modernfoundry.common.network.TinkerNetwork;
import modernmods.modernfoundry.tools.network.YoyoToggleAttackPacket;
import modernmods.modernfoundry.tools.network.YoyoToggleEnchantmentPacket;
import modernmods.modernfoundry.tools.yoyo.YoyoEnchantments;
import modernmods.modernfoundry.tools.yoyo.YoyoItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

public final class YoyoConfigScreen extends Screen {
  protected YoyoConfigScreen() { super(Component.literal("")); }

  @Override
  protected void init() {
    super.init();
    Provider provider = this.minecraft.player.registryAccess();
    RegistryLookup<Enchantment> enchantments = provider.lookupOrThrow(Registries.ENCHANTMENT);
    this.addRenderableWidget(new ExpandingWidget(this.width / 2 - 1, this.height / 2 - 88, 40, 40, 2,
      widget -> TinkerNetwork.getInstance().sendToServer(new YoyoToggleAttackPacket(InteractionHand.MAIN_HAND)),
      () -> YoyoItem.isAttackEnable(this.minecraft.player.getMainHandItem()), Items.DIAMOND_SWORD,
      Component.translatable("tooltip.modernfoundry.yoyo.attack"), () -> true));
    this.addRenderableWidget(new ExpandingWidget(this.width / 2 - 1, this.height / 2 - 44, 40, 40, 2,
      widget -> TinkerNetwork.getInstance().sendToServer(new YoyoToggleEnchantmentPacket(InteractionHand.MAIN_HAND, YoyoEnchantments.COLLECTING.location())),
      () -> YoyoItem.isEnchantmentEnable(this.minecraft.player.getMainHandItem(), YoyoEnchantments.COLLECTING, provider), Items.HOPPER,
      enchantments.getOrThrow(YoyoEnchantments.COLLECTING).value().description().copy(),
      () -> this.minecraft.player.getMainHandItem().getItem() instanceof YoyoItem yoyo
        && yoyo.getMaxCollectedDrops(this.minecraft.player.getMainHandItem(), provider) > 0));
    this.addRenderableWidget(new ExpandingWidget(this.width / 2 - 1, this.height / 2 + 1, 40, 40, 2,
      widget -> TinkerNetwork.getInstance().sendToServer(new YoyoToggleEnchantmentPacket(InteractionHand.MAIN_HAND, YoyoEnchantments.BREAKING.location())),
      () -> YoyoItem.isEnchantmentEnable(this.minecraft.player.getMainHandItem(), YoyoEnchantments.BREAKING, provider), Items.DIAMOND_PICKAXE,
      Enchantment.getFullname(enchantments.getOrThrow(YoyoEnchantments.BREAKING), 1),
      () -> this.minecraft.player.getMainHandItem().getEnchantmentLevel(enchantments.getOrThrow(YoyoEnchantments.BREAKING)) > 0
        && !YoyoItem.isEnchantmentEnable(this.minecraft.player.getMainHandItem(), YoyoEnchantments.CRAFTING, provider)));
    this.addRenderableWidget(new ExpandingWidget(this.width / 2 - 1, this.height / 2 + 46, 40, 40, 2,
      widget -> TinkerNetwork.getInstance().sendToServer(new YoyoToggleEnchantmentPacket(InteractionHand.MAIN_HAND, YoyoEnchantments.CRAFTING.location())),
      () -> YoyoItem.isEnchantmentEnable(this.minecraft.player.getMainHandItem(), YoyoEnchantments.CRAFTING, provider), Items.CRAFTING_TABLE,
      Enchantment.getFullname(enchantments.getOrThrow(YoyoEnchantments.CRAFTING), 1),
      () -> this.minecraft.player.getMainHandItem().getEnchantmentLevel(enchantments.getOrThrow(YoyoEnchantments.CRAFTING)) > 0
        && !YoyoItem.isEnchantmentEnable(this.minecraft.player.getMainHandItem(), YoyoEnchantments.BREAKING, provider)));
  }

  @Override public void tick() { super.tick(); children().stream().filter(w -> w instanceof ExpandingWidget).map(w -> (ExpandingWidget) w).forEach(ExpandingWidget::tick); }
  @Override public void mouseMoved(double x, double y) { children().stream().filter(w -> w instanceof ExpandingWidget).map(w -> (ExpandingWidget) w).forEach(w -> w.mouseMoved(x, y)); super.mouseMoved(x, y); }
  @Override public boolean isPauseScreen() { return false; }
  @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
    if (YoyosKeybindings.OPEN_CONFIG.getKey().getValue() == key) { onClose(); return true; }
    return super.keyPressed(key, scanCode, modifiers);
  }
}

