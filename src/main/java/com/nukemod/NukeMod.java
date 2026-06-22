package com.nukemod;

import com.nukemod.block.ModBlocks;
import com.nukemod.effect.RadiationEffect;
import com.nukemod.entity.ModEntities;
import com.nukemod.entity.NuclearCreeperEntity;
import com.nukemod.entity.RadiationGiantEntity;
import com.nukemod.entity.UraniumGolemEntity;
import com.nukemod.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NukeMod implements ModInitializer {
	public static final String MOD_ID = "nukemod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final StatusEffect RADIATION = new RadiationEffect();

	@Override
	public void onInitialize() {
		LOGGER.info("核弹 Mod 加载中... ☢️");

		ModBlocks.register();
		ModItems.register();
		ModEntities.register();

		Registry.register(Registries.STATUS_EFFECT, id("radiation"), RADIATION);

		// Register boss attributes
		FabricDefaultAttributeRegistry.register(ModEntities.RADIATION_GIANT, RadiationGiantEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.URANIUM_GOLEM, UraniumGolemEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.NUCLEAR_CREEPER, NuclearCreeperEntity.createAttributes());

		// Creative inventory tabs
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
			content.add(ModItems.NUCLEAR_RIFLE);
			content.add(ModItems.URANIUM_PISTOL);
			content.add(ModItems.RADIATION_SHOTGUN);
			content.add(ModItems.GAUSS_CANNON);
			content.add(ModItems.URANIUM_BULLET);
			content.add(ModItems.NUCLEAR_BOMB);
			content.add(ModItems.MINI_NUKE);
			content.add(ModItems.NUCLEAR_MINE);
			content.add(ModItems.BRIEFCASE_NUKE);
			content.add(ModItems.NUCLEAR_TORPEDO);
			content.add(ModItems.DIRTY_BOMB);
			content.add(ModItems.HAZMAT_HELMET);
			content.add(ModItems.HAZMAT_CHESTPLATE);
			content.add(ModItems.HAZMAT_LEGGINGS);
			content.add(ModItems.HAZMAT_BOOTS);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(content -> {
			content.add(ModItems.RADIATION_GIANT_SPAWN_EGG);
			content.add(ModItems.URANIUM_GOLEM_SPAWN_EGG);
			content.add(ModItems.NUCLEAR_CREEPER_SPAWN_EGG);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.add(ModItems.NUCLEAR_BOMB);
			content.add(ModItems.MINI_NUKE);
			content.add(ModItems.NUCLEAR_MINE);
			content.add(ModItems.BRIEFCASE_NUKE);
			content.add(ModItems.NUCLEAR_TORPEDO);
			content.add(ModItems.DIRTY_BOMB);
			content.add(ModItems.NUCLEAR_WASTE_BARREL);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(content -> {
			content.add(ModItems.URANIUM_BLOCK);
			content.add(ModItems.URANIUM_BRICKS);
			content.add(ModItems.BLAST_CONCRETE);
			content.add(ModItems.NUCLEAR_WASTE_BARREL);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(content -> {
			content.add(ModItems.URANIUM_ORE);
			content.add(ModItems.DEEPSLATE_URANIUM_ORE);
			content.add(ModItems.RAW_URANIUM);
			content.add(ModItems.IRRADIATED_DIRT);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.add(ModItems.URANIUM_GLASS);
			content.add(ModBlocks.URANIUM_LAMP.asItem());
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(content -> {
			content.add(ModItems.RAW_URANIUM);
			content.add(ModItems.URANIUM_INGOT);
			content.add(ModItems.URANIUM_BULLET);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.add(ModItems.GEIGER_COUNTER);
		});

		// Ore generation
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.UNDERGROUND_ORES,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("uranium_ore_placed"))
		);

		LOGGER.info("核弹 Mod 加载完成！⚡🔥");
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
