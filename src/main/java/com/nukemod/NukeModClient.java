package com.nukemod;

import com.nukemod.client.renderer.BulletRenderer;
import com.nukemod.client.renderer.NukeEntityRenderer;
import com.nukemod.client.renderer.ThrowableNukeRenderer;
import com.nukemod.entity.ModEntities;
import com.nukemod.entity.client.ModEntityRenderers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class NukeModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.NUCLEAR_BOMB, NukeEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.THROWABLE_NUKE, ThrowableNukeRenderer::new);
        EntityRendererRegistry.register(ModEntities.BULLET, BulletRenderer::new);
        ModEntityRenderers.register();
    }
}
