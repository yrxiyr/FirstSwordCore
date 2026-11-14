package com.firstsword.core.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import com.firstsword.core.GravityField;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;

/**
 * 引力场：贴地的一圈暗紫色法阵，反向旋转，越到边缘越偏猩红。
 *
 * 和流星法阵不同，这一圈是躺在地面上的（XZ 平面），所以不用做面向镜头的变换。
 */
public class GravityFieldRenderer extends EntityRenderer<GravityField>
{
    private static final float TWO_PI = (float) (Math.PI * 2.0D);

    private static final int SEGMENTS = 96;
    private static final int SPOKES = 16;

    /** 地面之上的高度，避免和方块面 z-fighting */
    private static final float Y = 0.06F;

    private static final float FADE_IN = 6.0F;
    private static final float FADE_OUT = 20.0F;

    private static final RenderType FIELD = RenderType.create(
            "firstsword_core_gravity_field",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false));

    public GravityFieldRenderer(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(GravityField entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight)
    {
        float age = entity.tickCount + partialTicks;
        float alpha = fade(age);

        if (alpha <= 0.01F)
        {
            return;
        }

        float radius = entity.radius();
        float spin = -age * 0.02F;

        VertexConsumer consumer = buffer.getBuffer(FIELD);
        Matrix4f matrix = poseStack.last().pose();

        ring(consumer, matrix, radius, 0.955F, 1.0F, spin, alpha);
        ring(consumer, matrix, radius, 0.575F, 0.61F, -spin * 1.7F, alpha * 0.8F);
        spokes(consumer, matrix, radius, 0.61F, 0.955F, -spin, alpha * 0.55F);
    }

    private static void ring(VertexConsumer consumer, Matrix4f matrix, float radius,
                             float inner, float outer, float spin, float alpha)
    {
        for (int i = 0; i < SEGMENTS; i++)
        {
            float a0 = spin + TWO_PI * i / SEGMENTS;
            float a1 = spin + TWO_PI * (i + 1) / SEGMENTS;

            vertex(consumer, matrix, radius, inner, a0, alpha);
            vertex(consumer, matrix, radius, outer, a0, alpha);
            vertex(consumer, matrix, radius, outer, a1, alpha);
            vertex(consumer, matrix, radius, inner, a1, alpha);
        }
    }

    private static void spokes(VertexConsumer consumer, Matrix4f matrix, float radius,
                               float from, float to, float spin, float alpha)
    {
        // 0.35 格宽的辐条换算成弧度
        float halfAngle = 0.35F / (to * radius);

        for (int i = 0; i < SPOKES; i++)
        {
            float angle = spin + TWO_PI * i / SPOKES;

            vertex(consumer, matrix, radius, from, angle - halfAngle, alpha);
            vertex(consumer, matrix, radius, to, angle - halfAngle, alpha);
            vertex(consumer, matrix, radius, to, angle + halfAngle, alpha);
            vertex(consumer, matrix, radius, from, angle + halfAngle, alpha);
        }
    }

    /** 极坐标 → 世界坐标，颜色沿圆周从紫渐变到猩红 */
    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float radius, float fraction, float angle, float alpha)
    {
        double r = fraction * radius;

        float t = ((angle / TWO_PI) % 1.0F + 1.0F) % 1.0F;

        float red = 0.58F + 0.37F * t;
        float green = 0.16F + 0.02F * t;
        float blue = 0.92F - 0.66F * t;

        consumer.addVertex(matrix,
                        (float) (Math.cos(angle) * r), Y, (float) (Math.sin(angle) * r))
                .setColor(red, green, blue, alpha);
    }

    private static float fade(float age)
    {
        float fadeIn = Math.min(age / FADE_IN, 1.0F);
        float fadeOut = Math.min((GravityField.LIFETIME - age) / FADE_OUT, 1.0F);

        return Math.max(0.0F, Math.min(fadeIn, fadeOut)) * 0.85F;
    }

    @Override
    public ResourceLocation getTextureLocation(GravityField entity)
    {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
