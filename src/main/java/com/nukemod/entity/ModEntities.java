package com.nukemod.entity;

import com.nukemod.NukeMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<PrimedNuclearBombEntity> NUCLEAR_BOMB = FabricEntityTypeBuilder
            .<PrimedNuclearBombEntity>create(SpawnGroup.MISC, PrimedNuclearBombEntity::new)
            .dimensions(EntityDimensions.fixed(0.98F, 0.98F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(10)
            .build();

    public static final EntityType<ThrowableNukeEntity> THROWABLE_NUKE = FabricEntityTypeBuilder
            .<ThrowableNukeEntity>create(SpawnGroup.MISC, ThrowableNukeEntity::new)
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(10)
            .build();

    public static final EntityType<BulletEntity> BULLET = FabricEntityTypeBuilder
            .<BulletEntity>create(SpawnGroup.MISC, BulletEntity::new)
            .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
            .trackRangeBlocks(10)
            .trackedUpdateRate(5)
            .build();

    // Bosses
    public static final EntityType<RadiationGiantEntity> RADIATION_GIANT = FabricEntityTypeBuilder
            .<RadiationGiantEntity>create(SpawnGroup.MONSTER, RadiationGiantEntity::new)
            .dimensions(EntityDimensions.fixed(1.8F, 4.5F))
            .trackRangeBlocks(16)
            .trackedUpdateRate(3)
            .build();

    public static final EntityType<UraniumGolemEntity> URANIUM_GOLEM = FabricEntityTypeBuilder
            .<UraniumGolemEntity>create(SpawnGroup.MONSTER, UraniumGolemEntity::new)
            .dimensions(EntityDimensions.fixed(1.6F, 3.2F))
            .trackRangeBlocks(16)
            .trackedUpdateRate(3)
            .build();

    public static final EntityType<NuclearCreeperEntity> NUCLEAR_CREEPER = FabricEntityTypeBuilder
            .<NuclearCreeperEntity>create(SpawnGroup.MONSTER, NuclearCreeperEntity::new)
            .dimensions(EntityDimensions.fixed(0.9F, 2.2F))
            .trackRangeBlocks(16)
            .trackedUpdateRate(3)
            .build();

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, id("nuclear_bomb"), NUCLEAR_BOMB);
        Registry.register(Registries.ENTITY_TYPE, id("throwable_nuke"), THROWABLE_NUKE);
        Registry.register(Registries.ENTITY_TYPE, id("bullet"), BULLET);
        Registry.register(Registries.ENTITY_TYPE, id("radiation_giant"), RADIATION_GIANT);
        Registry.register(Registries.ENTITY_TYPE, id("uranium_golem"), URANIUM_GOLEM);
        Registry.register(Registries.ENTITY_TYPE, id("nuclear_creeper"), NUCLEAR_CREEPER);
    }

    private static Identifier id(String path) {
        return new Identifier(NukeMod.MOD_ID, path);
    }
}
