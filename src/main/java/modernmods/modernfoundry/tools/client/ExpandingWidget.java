package modernmods.modernfoundry.tools.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ExpandingWidget extends AbstractWidget {
   private final int expandedWidth;
   private final int expandedHeight;
   private final int animationDurationTicks;
   private int ticksSinceOpen;
   private int closingTicks;
   private boolean collapsing;
   private final ExpandingWidget.OnClick onClick;
   private final Supplier<Boolean> toggled;
   private final Supplier<Boolean> isActivate;
   private final Item item;

   public ExpandingWidget(
      int x,
      int y,
      int width,
      int height,
      int animationDurationTicks,
      ExpandingWidget.OnClick onClick,
      Supplier<Boolean> toggled,
      Item item,
      Component message,
      Supplier<Boolean> isActivate
   ) {
      super(x, y, 0, 0, message);
      this.onClick = onClick;
      this.expandedWidth = width;
      this.expandedHeight = height;
      this.animationDurationTicks = animationDurationTicks;
      this.ticksSinceOpen = 0;
      this.collapsing = false;
      this.toggled = toggled;
      this.item = item;
      this.isActivate = isActivate;
   }

   public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.active = this.isActivate.get();
      this.isHovered = mouseX >= this.getX() - this.width / 2
         && mouseX <= this.getX() + this.width / 2
         && mouseY >= this.getY() - this.height / 2
         && mouseY <= this.getY() + this.height / 2;
      if (this.ticksSinceOpen <= this.animationDurationTicks || this.collapsing) {
         this.updateSize();
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.fill(
         this.getX() - this.width / 2,
         this.getY() - this.height / 2,
         this.getX() + this.width / 2,
         this.getY() + this.height / 2,
         this.toggled.get() ? new Color(114, 114, 114, 100).getRGB() : new Color(0, 0, 0, 100).getRGB()
      );
      if (this.ticksSinceOpen >= this.animationDurationTicks) {
         graphics.pose().pushPose();
         graphics.pose().scale(0.75F, 0.75F, 0.75F);
         graphics.drawCenteredString(
            Minecraft.getInstance().font,
            this.getMessage().copy().withStyle(this.toggled.get() ? ChatFormatting.GREEN : ChatFormatting.RED),
            (int)(this.getX() / 0.75F),
            (int)((this.getY() + 6) / 0.75F),
            this.toggled.get() ? ChatFormatting.GREEN.getColor() : ChatFormatting.RED.getColor()
         );
         graphics.pose().popPose();
         if (this.ticksSinceOpen > this.animationDurationTicks && !this.collapsing) {
            graphics.pose().pushPose();
            graphics.pose().scale(1.5F, 1.5F, 1.5F);
            graphics.renderItem(new ItemStack(this.item), (int)(this.getX() / 1.5 - 7.5), (int)((this.getY() - this.height / 2.0F + 1.0F) / 1.5F));
            graphics.pose().popPose();
         }

         graphics.pose().pushPose();
         graphics.pose().scale(3.0F, 3.0F, 3.0F);
         graphics.pose().translate(0.5, 0.0, 80.0);
         String check = "❌";
         int widthCheck = Minecraft.getInstance().font.width(check);
         if (!this.toggled.get()) {
            graphics.drawString(
               Minecraft.getInstance().font,
               check,
               this.getX() / 3 - widthCheck / 2,
               (this.getY() - this.height / 2 + 2) / 3,
               ChatFormatting.RED.getColor(),
               false
            );
         }

         graphics.pose().popPose();
         if (this.isHovered && this.isActive()) {
            graphics.fill(
               this.getX() - this.width / 2,
               this.getY() - this.height / 2,
               this.getX() + this.width / 2,
               this.getY() - this.height / 2 + 1,
               new Color(255, 255, 255).getRGB()
            );
            graphics.fill(
               this.getX() - this.width / 2,
               this.getY() + this.height / 2,
               this.getX() + this.width / 2,
               this.getY() + this.height / 2 - 1,
               new Color(255, 255, 255).getRGB()
            );
            graphics.fill(
               this.getX() - this.width / 2,
               this.getY() - this.height / 2 + 1,
               this.getX() - this.width / 2 + 1,
               this.getY() + this.height / 2 - 1,
               new Color(255, 255, 255).getRGB()
            );
            graphics.fill(
               this.getX() + this.width / 2,
               this.getY() - this.height / 2 + 1,
               this.getX() + this.width / 2 - 1,
               this.getY() + this.height / 2 - 1,
               new Color(255, 255, 255).getRGB()
            );
         }

         if (!this.isActive()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 2000.0F);
            graphics.fill(
               this.getX() - this.width / 2,
               this.getY() - this.height / 2,
               this.getX() + this.width / 2,
               this.getY() + this.height / 2,
               new Color(0, 0, 0, 155).getRGB()
            );
            graphics.pose().popPose();
         }
      }
   }

   private void updateSize() {
      if (this.ticksSinceOpen >= this.animationDurationTicks && !this.collapsing) {
         this.width = this.expandedWidth;
         this.height = this.expandedHeight;
      } else {
         if (this.ticksSinceOpen <= this.animationDurationTicks) {
            int pixelPerTick = this.expandedWidth / this.animationDurationTicks;
            int progress = this.animationDurationTicks + (this.ticksSinceOpen - this.animationDurationTicks);
            this.width = pixelPerTick * progress;
            this.height = pixelPerTick * progress;
         }

         if (this.collapsing) {
            if (this.closingTicks >= this.animationDurationTicks) {
               this.width = 0;
               this.height = 0;
               this.collapsing = false;
               return;
            }

            int pixelPerTick = this.expandedWidth / this.animationDurationTicks;
            int progress = this.animationDurationTicks - this.closingTicks;
            this.width = pixelPerTick * progress;
            this.height = pixelPerTick * progress;
         }
      }
   }

   public void tick() {
      this.ticksSinceOpen++;
      if (this.collapsing) {
         this.closingTicks++;
      }
   }

   public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
   }

   public void mouseMoved(double mouseX, double mouseY) {
   }

   public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
      if (this.isHovered && this.isActive()) {
         this.onClick.onClick(this);
      }

      return this.isHovered;
   }

   @OnlyIn(Dist.CLIENT)
   public interface OnClick {
      void onClick(ExpandingWidget var1);
   }
}


