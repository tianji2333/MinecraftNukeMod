package com.nukemod.client.renderer;

import com.nukemod.entity.BulletEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class BulletRenderer extends EntityRenderer<BulletEntity> {

    private final ItemRenderer itemRenderer;

    public BulletRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.shadowRadius = 0.1F;
    }

    @Override
    public void render(BulletEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.push();
        matrices.scale(0.3F, 0.3F, 0.3F);
        int color = entity.isOnFire() ? 0xFF5500 : 0xFFAA00;
        ItemStack stack = new ItemStack(entity.isOnFire() ? Items.FIRE_CHARGE : Items.GLOWSTONE_DUST);
        itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
    }

    @Override
    public Identifier getTexture(BulletEntity entity) {
        return null;
    }
}
