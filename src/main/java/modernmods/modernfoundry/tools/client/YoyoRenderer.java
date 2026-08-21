package modernmods.modernfoundry.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import modernmods.modernfoundry.tools.yoyo.YoyoEntity;
import modernmods.modernfoundry.tools.yoyo.api.IYoyo;
import modernmods.modernfoundry.tools.yoyo.api.RenderOrientation;
import modernmods.modernfoundry.tools.TinkerTools;import java.util.Random;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class YoyoRenderer extends EntityRenderer<YoyoEntity> {
   private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
   private final Random random = new Random();

   public YoyoRenderer(Context ctx) {
      super(ctx);
   }

   public boolean shouldRender(YoyoEntity p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
      return true;
   }

   public ResourceLocation getTextureLocation(YoyoEntity yoyo) {
      return TextureAtlas.LOCATION_BLOCKS;
   }

   public void render(YoyoEntity entity, float p_114486_, float pt, PoseStack stack, MultiBufferSource p_114489_, int p_114490_) {
      Minecraft.getInstance().getProfiler().push("renderYoyo");
      stack.pushPose();
      stack.scale(0.5F, 0.5F, 0.5F);
      Vec3 pointTo = entity.getPlayerHandPos(pt).subtract(entity.getX(), entity.getY(), entity.getZ()).normalize();
      stack.pushPose();
      if (entity.hasYoyo() && entity.getYoyo().getRenderOrientation(entity.getYoyoStack()) == RenderOrientation.Horizontal) {
         stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
      } else {
         stack.mulPose(Axis.YP.rotationDegrees(270.0F - Minecraft.getInstance().cameraEntity.getYRot()));
      }

      stack.mulPose(Axis.ZP.rotation((float)entity.getRemainingTime() / entity.getMaxTime() * 2.0F * 360.0F));
      if (entity.getYoyoStack().getItem() == TinkerTools.creativeYoyo.get()) {
         stack.mulPose(Axis.ZP.rotation((float)entity.tickCount / entity.getMaxTime() * 4.0F * 360.0F));
      }

      this.itemRenderer.renderStatic(entity.getYoyoStack(), ItemDisplayContext.NONE, p_114490_, 0, stack, p_114489_, Minecraft.getInstance().level, p_114490_);
      stack.popPose();
      if (entity.isCollecting() && !entity.getCollectedDrops().isEmpty()) {
         this.renderCollectedItems(entity, pt, stack, p_114489_, p_114490_);
      }

      stack.popPose();
      this.renderChord(entity, pt, stack, p_114489_, p_114490_);
      Minecraft.getInstance().getProfiler().pop();
   }

   public void renderChord(YoyoEntity entity, float partialTicks, PoseStack stack, MultiBufferSource source, int packedLight) {
      UUID uuid = YoyoEntity.CASTERS.keySet().stream().filter(k -> YoyoEntity.CASTERS.get(k).equals(entity)).findFirst().orElse(null);
      if (uuid != null) {
         Player player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
         if (player != null) {
            stack.pushPose();
            int armDirection = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            if (player.getOffhandItem().equals(entity.getYoyoStack())) {
               armDirection = -armDirection;
            }

            float attackAnimation = player.getAttackAnim(partialTicks);
            float interpolatedAttackSwing = Mth.sin(Mth.sqrt(attackAnimation) * (float) Math.PI);
            float bodyYawRadians = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * (float) (Math.PI / 180.0);
            double bodyYawSin = Mth.sin(bodyYawRadians);
            double bodyYawCos = Mth.cos(bodyYawRadians);
            double offsetX = armDirection * 0.35;
            double hookX;
            double hookY;
            double hookZ;
            float crouchOffset;
            if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
               double fovFactor = 960.0 / ((Integer)this.entityRenderDispatcher.options.fov().get()).intValue();
               Vec3 cameraOffset = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane(armDirection * 0.525F, -0.1F);
               cameraOffset = cameraOffset.scale(fovFactor);
               cameraOffset = cameraOffset.yRot(interpolatedAttackSwing * 0.5F);
               cameraOffset = cameraOffset.xRot(-interpolatedAttackSwing * 0.7F);
               hookX = Mth.lerp(partialTicks, player.xo, player.getX()) + cameraOffset.x;
               hookY = Mth.lerp(partialTicks, player.yo, player.getY()) + cameraOffset.y;
               hookZ = Mth.lerp(partialTicks, player.zo, player.getZ()) + cameraOffset.z;
               crouchOffset = player.getEyeHeight();
            } else {
               hookX = Mth.lerp(partialTicks, player.xo, player.getX()) - bodyYawCos * offsetX - bodyYawSin * 0.8;
               hookY = player.yo + player.getEyeHeight() + (player.getY() - player.yo) * partialTicks - 0.45;
               hookZ = Mth.lerp(partialTicks, player.zo, player.getZ()) - bodyYawSin * offsetX + bodyYawCos * 0.8;
               crouchOffset = player.isCrouching() ? -0.1875F : 0.0F;
            }

            double entityX = Mth.lerp(partialTicks, entity.xo, entity.getX());
            double entityY = Mth.lerp(partialTicks, entity.yo, entity.getY()) + 0.25;
            double entityZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
            float deltaX = (float)(hookX - entityX);
            float deltaY = (float)(hookY - entityY) + crouchOffset;
            float deltaZ = (float)(hookZ - entityZ);
            VertexConsumer lineBuffer = source.getBuffer(RenderType.lineStrip());
            Pose poseStack = stack.last();
            int segments = 24;
            int color = 14540253;
            if (entity.getYoyoStack().getItem() instanceof IYoyo yoyo) {
               color = yoyo.getCordColor(entity.getYoyoStack(), entity.tickCount + partialTicks);
            }

            for (int segment = 0; segment <= segments; segment++) {
               stringVertex(deltaX, deltaY, deltaZ, lineBuffer, poseStack, fraction(segment, 24), fraction(segment + 1, 24), segment, color);
            }

            stack.popPose();
         }
      }
   }

   private static float fraction(int p_114691_, int p_114692_) {
      return (float)p_114691_ / p_114692_;
   }

   private static void stringVertex(
      float deltaX,
      float deltaY,
      float deltaZ,
      VertexConsumer vertexBuffer,
      Pose pose,
      float currentSegmentFraction,
      float nextSegmentFraction,
      int currentSegment,
      int color
   ) {
      float currentX = deltaX * currentSegmentFraction;
      float currentY = deltaY * (currentSegmentFraction * currentSegmentFraction + currentSegmentFraction) * 0.5F + 0.25F;
      float currentZ = deltaZ * currentSegmentFraction;
      float nextX = deltaX * nextSegmentFraction - currentX;
      float nextY = deltaY * (nextSegmentFraction * nextSegmentFraction + nextSegmentFraction) * 0.5F + 0.25F - currentY;
      float nextZ = deltaZ * nextSegmentFraction - currentZ;
      float segmentLength = Mth.sqrt(nextX * nextX + nextY * nextY + nextZ * nextZ);
      nextX /= segmentLength;
      nextY /= segmentLength;
      nextZ /= segmentLength;
      float stringR = (color >> 16 & 0xFF) / 255.0F;
      float stringG = (color >> 8 & 0xFF) / 255.0F;
      float stringB = (color & 0xFF) / 255.0F;
      float r = stringR;
      float g = stringG;
      float b = stringB;
      if (currentSegment % 2 == 0) {
         r *= 0.7F;
         g *= 0.7F;
         b *= 0.7F;
      }

      vertexBuffer.addVertex(pose.pose(), currentX, currentY, currentZ).setColor(r, g, b, 1.0F).setNormal(pose.copy(), nextX, nextY, nextZ);
   }

   private void renderCollectedItems(YoyoEntity entity, float pt, PoseStack poseStack, MultiBufferSource source, int light) {
      boolean boundTexture = false;

      for (int i = 0; i < entity.getCollectedDrops().size(); i++) {
         ItemStack stack = entity.getCollectedDrops().get(i);
         int count = stack.getCount();

         for (int maxCount = stack.getMaxStackSize(); count > 0; count -= maxCount) {
            this.renderItem(i, entity, stack, pt, poseStack, source, light);
         }
      }
   }

   private void renderItem(int i, YoyoEntity yoyo, ItemStack itemStack, float partialTicks, PoseStack poseStack, MultiBufferSource source, int light) {
      poseStack.pushPose();
      long seed = (Item.getId(itemStack.getItem()) * 31L + i) * 31L + itemStack.getCount();
      this.random.setSeed(seed);
      BakedModel bakedModel = this.itemRenderer.getItemModelShaper().getItemModel(itemStack);
      int modelCount = this.transformModelCount(yoyo, itemStack, partialTicks, bakedModel, poseStack);
      boolean gui3d = bakedModel.isGui3d();
      if (!gui3d) {
         float f3 = -0.0F * (modelCount - 1) * 0.5F;
         float f4 = -0.0F * (modelCount - 1) * 0.5F;
         float f5 = -0.09375F * (modelCount - 1) * 0.5F;
         poseStack.translate(f3, f4, f5);
      }

      for (int k = 0; k < modelCount; k++) {
         poseStack.pushPose();
         if (gui3d) {
            if (k > 0) {
               float f7 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f9 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f6 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               poseStack.translate(f7, f9, f6);
            }

            this.itemRenderer
               .renderStatic(itemStack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, source, Minecraft.getInstance().level, 0);
            poseStack.popPose();
         } else {
            if (k > 0) {
               float f8 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               poseStack.translate(f8, f10, 0.0);
            }

            this.itemRenderer
               .renderStatic(itemStack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY, poseStack, source, Minecraft.getInstance().level, 0);
            poseStack.popPose();
            poseStack.translate(0.0, 0.0, 0.09375);
         }
      }

      poseStack.popPose();
   }

   private double interpolateValue(double start, double end, double pct) {
      return start + (end - start) * pct;
   }

   private float interpolateValue(float start, float end, float pct) {
      return start + (end - start) * pct;
   }

   private int transformModelCount(YoyoEntity yoyo, ItemStack itemStack, float partialTicks, BakedModel model, PoseStack stack) {
      boolean gui3d = model.isGui3d();
      int count = this.getModelCount(itemStack);
      double bob = Math.sin((this.random.nextDouble() + yoyo.tickCount + partialTicks) / 10.0 + this.random.nextDouble() * Math.PI * 2.0) * 0.1 + 0.1;
      double scale = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y;
      stack.translate(0.0, bob + 0.25 * scale, 0.0);
      if (gui3d) {
         double angle = ((this.random.nextDouble() + yoyo.tickCount + partialTicks) / 20.0 + this.random.nextDouble() * Math.PI * 2.0) * (180.0 / Math.PI);
         stack.mulPose(Axis.YP.rotationDegrees((float)angle));
      }

      return count;
   }

   private int getModelCount(ItemStack stack) {
      if (stack.getCount() > 48) {
         return 5;
      } else if (stack.getCount() > 32) {
         return 4;
      } else if (stack.getCount() > 16) {
         return 3;
      } else {
         return stack.getCount() > 1 ? 2 : 1;
      }
   }
}
