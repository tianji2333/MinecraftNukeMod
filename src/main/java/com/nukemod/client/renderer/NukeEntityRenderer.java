package com.nukemod.client.renderer;

import com.nukemod.block.ModBlocks;
import com.nukemod.entity.PrimedNuclearBombEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class NukeEntityRenderer extends EntityRenderer<PrimedNuclearBombEntity> {
    private final BlockRenderManager blockRenderManager;

    public NukeEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.5F;
        this.blockRenderManager = ctx.getBlockRenderManager();
    }

    @Override
    public void render(PrimedNuclearBombEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // 悬浮效果 + 旋转（类似TNT的渲染）
        matrices.translate(0.0F, 0.5F, 0.0F);

        // 缓慢旋转（随时间自转）
        int age = entity.getFuse();
        float rotation = (age + tickDelta) * 12.0F;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));

        // 闪烁效果（快爆炸时闪烁）
        if (age > 0 && age < 40 && (age / 3) % 2 == 0) {
            // 接近爆炸时微小的抖动
            float scale = 1.0F + (40 - age) * 0.002F;
            matrices.scale(scale, scale, scale);
        }

        // 渲染核弹方块
        BlockState blockState = ModBlocks.NUCLEAR_BOMB.getDefaultState();
        this.blockRenderManager.renderBlockAsEntity(blockState, matrices, vertexConsumers, light, 0);

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(PrimedNuclearBombEntity entity) {
        return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    }
}
