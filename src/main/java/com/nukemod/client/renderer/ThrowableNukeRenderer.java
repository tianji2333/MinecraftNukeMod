package com.nukemod.client.renderer;

import com.nukemod.entity.ThrowableNukeEntity;
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

public class ThrowableNukeRenderer extends EntityRenderer<ThrowableNukeEntity> {

    private final ItemRenderer itemRenderer;

    public ThrowableNukeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(ThrowableNukeEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
        matrices.push();
        matrices.translate(0, 0.15, 0);
        matrices.scale(0.6F, 0.6F, 0.6F);
        itemRenderer.renderItem(new ItemStack(Items.IRON_INGOT), ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), entity.getId());
        matrices.pop();
    }

    @Override
    public Identifier getTexture(ThrowableNukeEntity entity) {
        return null;
    }
}
