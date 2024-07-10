package fr.kolala.slimemap.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import fr.kolala.slimemap.item.FilledSlimeMapItem;
import fr.kolala.slimemap.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value = EnvType.CLIENT)
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    // TODO: Make custom texture for both of them with slime around the map
    @Unique
    private static final RenderLayer SLIME_MAP_BACKGROUND = RenderLayer.getText(new Identifier("slimemap", "textures/map/slime_map_background.png"));
    @Unique
    private static final RenderLayer SLIME_MAP_BACKGROUND_CHECKERBOARD = RenderLayer.getText(new Identifier("slimemap", "textures/map/slime_map_background_checkerboard.png"));

    @Shadow private ItemStack offHand;

    @Shadow protected abstract float getMapAngle(float tickDelta);

    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void renderArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, Arm arm);

    @Shadow private ItemStack mainHand;

    @Shadow protected abstract void renderArmHoldingItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, float swingProgress, Arm arm);

    @Unique
    private void renderSlimeMapInOneHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float equipProgress, Arm arm, float swingProgress, ItemStack stack) {
        float f = arm == Arm.RIGHT ? 1.0f : -1.0f;
        matrices.translate(f * 0.125f, -0.125f, 0.0f);
        assert this.client.player != null;
        if (!this.client.player.isInvisible()) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 10.0f));
            this.renderArmHoldingItem(matrices, vertexConsumers, light, equipProgress, swingProgress, arm);
            matrices.pop();
        }
        matrices.push();
        matrices.translate(f * 0.51f, -0.08f + equipProgress * -1.2f, -0.75f);
        float g = MathHelper.sqrt(swingProgress);
        float h = MathHelper.sin(g * (float)Math.PI);
        float i = -0.5f * h;
        float j = 0.4f * MathHelper.sin(g * ((float)Math.PI * 2));
        float k = -0.3f * MathHelper.sin(swingProgress * (float)Math.PI);
        matrices.translate(f * i, j - 0.3f * h, k);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * -45.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * h * -30.0f));
        this.renderFirstPersonSlimeMap(matrices, vertexConsumers, light, stack);
        matrices.pop();
    }

    @Unique
    private void renderSlimeMapInBothHands(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float pitch, float equipProgress, float swingProgress) {
        float f = MathHelper.sqrt(swingProgress);
        float g = -0.2f * MathHelper.sin(swingProgress * (float) Math.PI);
        float h = -0.4f * MathHelper.sin(f * (float) Math.PI);
        matrices.translate(0.0f, -g / 2.0f, h);
        float i = this.getMapAngle(pitch);
        matrices.translate(0.0f, 0.04f + equipProgress * -1.2f + i * -0.5f, -0.72f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(i * -85.0f));
        assert this.client.player != null;
        if (!this.client.player.isInvisible()) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            this.renderArm(matrices, vertexConsumers, light, Arm.RIGHT);
            this.renderArm(matrices, vertexConsumers, light, Arm.LEFT);
            matrices.pop();
        }
        float j = MathHelper.sin(f * (float)Math.PI);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(j * 20.0f));
        matrices.scale(2.0f, 2.0f, 2.0f);
        this.renderFirstPersonSlimeMap(matrices, vertexConsumers, light, this.mainHand);
    }

    @Unique
    private void renderFirstPersonSlimeMap(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int swingProgress, ItemStack stack) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.scale(0.38f, 0.38f, 0.38f);
        matrices.translate(-0.5f, -0.5f, 0.0f);
        matrices.scale(0.0078125f, 0.0078125f, 0.0078125f);
        Integer integer = FilledSlimeMapItem.getMapId(stack);
        MapState mapState = FilledSlimeMapItem.getMapState(integer, this.client.world);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(mapState == null ? SLIME_MAP_BACKGROUND : SLIME_MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        vertexConsumer.vertex(matrix4f, -7.0f, 135.0f, 0.0f).color(255, 255, 255, 255).texture(0.0f, 1.0f).light(swingProgress).next();
        vertexConsumer.vertex(matrix4f, 135.0f, 135.0f, 0.0f).color(255, 255, 255, 255).texture(1.0f, 1.0f).light(swingProgress).next();
        vertexConsumer.vertex(matrix4f, 135.0f, -7.0f, 0.0f).color(255, 255, 255, 255).texture(1.0f, 0.0f).light(swingProgress).next();
        vertexConsumer.vertex(matrix4f, -7.0f, -7.0f, 0.0f).color(255, 255, 255, 255).texture(0.0f, 0.0f).light(swingProgress).next();
        if (mapState != null) {
            this.client.gameRenderer.getMapRenderer().draw(matrices, vertexConsumers, integer, mapState, false, swingProgress);
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
    private void renderFirstPersonItemInject(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Local(ordinal = 0) boolean bl, @Local(ordinal = 0) Arm arm) {
        if (item.isOf(ModItems.FILLED_SLIME_MAP)) {
            if (bl && this.offHand.isEmpty()) {
                this.renderSlimeMapInBothHands(matrices, vertexConsumers, light, pitch, equipProgress, swingProgress);
            } else {
                this.renderSlimeMapInOneHand(matrices, vertexConsumers, light, equipProgress, arm, swingProgress, item);
            }
        }
    }
}