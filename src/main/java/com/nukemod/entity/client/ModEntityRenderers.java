package com.nukemod.entity.client;

import com.nukemod.NukeMod;
import com.nukemod.entity.ModEntities;
import com.nukemod.entity.NuclearCreeperEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.IronGolemEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Identifier;

public class ModEntityRenderers {

    public static void register() {
        EntityRendererRegistry.register(ModEntities.RADIATION_GIANT, ctx ->
            new ZombieEntityRenderer(ctx) {
                @Override
                public Identifier getTexture(ZombieEntity entity) {
                    return new Identifier(NukeMod.MOD_ID, "textures/entity/radiation_giant.png");
                }
            }
        );

        EntityRendererRegistry.register(ModEntities.URANIUM_GOLEM, ctx ->
            new IronGolemEntityRenderer(ctx) {
                @Override
                public Identifier getTexture(IronGolemEntity entity) {
                    return new Identifier(NukeMod.MOD_ID, "textures/entity/uranium_golem.png");
                }
            }
        );

        EntityRendererRegistry.register(ModEntities.NUCLEAR_CREEPER, ctx -> {
            var model = new CreeperEntityModel<NuclearCreeperEntity>(ctx.getPart(EntityModelLayers.CREEPER));
            return new MobEntityRenderer<NuclearCreeperEntity, CreeperEntityModel<NuclearCreeperEntity>>(ctx, model, 0.5F) {
                @Override
                public Identifier getTexture(NuclearCreeperEntity entity) {
                    return new Identifier(NukeMod.MOD_ID, "textures/entity/nuclear_creeper.png");
                }
            };
        });
    }
}
