package com.nukemod.item;

import com.nukemod.NukeMod;
import com.nukemod.block.ModBlocks;
import com.nukemod.entity.ModEntities;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    // Nuclear bombs
    public static final Item NUCLEAR_BOMB = new BlockItem(ModBlocks.NUCLEAR_BOMB,
            new FabricItemSettings().maxCount(16));
    public static final Item MINI_NUKE = new BlockItem(ModBlocks.MINI_NUKE,
            new FabricItemSettings().maxCount(16));
    public static final Item NUCLEAR_MINE = new BlockItem(ModBlocks.NUCLEAR_MINE,
            new FabricItemSettings().maxCount(16));

    // Uranium
    public static final Item URANIUM_ORE = new BlockItem(ModBlocks.URANIUM_ORE, new FabricItemSettings());
    public static final Item DEEPSLATE_URANIUM_ORE = new BlockItem(ModBlocks.DEEPSLATE_URANIUM_ORE, new FabricItemSettings());
    public static final Item URANIUM_BLOCK = new BlockItem(ModBlocks.URANIUM_BLOCK, new FabricItemSettings());
    public static final Item NUCLEAR_WASTE_BARREL = new BlockItem(ModBlocks.NUCLEAR_WASTE_BARREL, new FabricItemSettings());
    public static final Item RAW_URANIUM = new Item(new FabricItemSettings());
    public static final Item URANIUM_INGOT = new Item(new FabricItemSettings());

    // Building blocks
    public static final Item BLAST_CONCRETE = new BlockItem(ModBlocks.BLAST_CONCRETE, new FabricItemSettings());
    public static final Item URANIUM_GLASS = new BlockItem(ModBlocks.URANIUM_GLASS, new FabricItemSettings());
    public static final Item URANIUM_BRICKS = new BlockItem(ModBlocks.URANIUM_BRICKS, new FabricItemSettings());
    public static final Item IRRADIATED_DIRT = new BlockItem(ModBlocks.IRRADIATED_DIRT, new FabricItemSettings());

    // Tactical weapons
    public static final Item BRIEFCASE_NUKE = new BriefcaseNukeItem(new FabricItemSettings().maxCount(8));
    public static final Item NUCLEAR_TORPEDO = new NuclearTorpedoItem(new FabricItemSettings().maxCount(8));
    public static final Item DIRTY_BOMB = new BlockItem(ModBlocks.DIRTY_BOMB, new FabricItemSettings().maxCount(16));

    // Guns
    public static final Item NUCLEAR_RIFLE = new NuclearRifleItem(new FabricItemSettings());
    public static final Item URANIUM_PISTOL = new UraniumPistolItem(new FabricItemSettings());
    public static final Item RADIATION_SHOTGUN = new RadiationShotgunItem(new FabricItemSettings());
    public static final Item GAUSS_CANNON = new GaussCannonItem(new FabricItemSettings());
    public static final Item URANIUM_BULLET = new UraniumBulletItem(new FabricItemSettings().maxCount(64));

    // Spawn eggs
    public static final Item RADIATION_GIANT_SPAWN_EGG = new SpawnEggItem(ModEntities.RADIATION_GIANT,
            0x44AA44, 0x88FF88, new FabricItemSettings());
    public static final Item URANIUM_GOLEM_SPAWN_EGG = new SpawnEggItem(ModEntities.URANIUM_GOLEM,
            0x336633, 0x55FF55, new FabricItemSettings());
    public static final Item NUCLEAR_CREEPER_SPAWN_EGG = new SpawnEggItem(ModEntities.NUCLEAR_CREEPER,
            0x334433, 0x66FF66, new FabricItemSettings());

    // Geiger counter
    public static final Item GEIGER_COUNTER = new GeigerCounterItem(new FabricItemSettings().maxCount(1));

    // Hazmat armor
    private static final ArmorMaterial HAZMAT_MATERIAL = new HazmatArmorMaterial();
    public static final Item HAZMAT_HELMET = new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.HELMET, new FabricItemSettings());
    public static final Item HAZMAT_CHESTPLATE = new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.CHESTPLATE, new FabricItemSettings());
    public static final Item HAZMAT_LEGGINGS = new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.LEGGINGS, new FabricItemSettings());
    public static final Item HAZMAT_BOOTS = new ArmorItem(HAZMAT_MATERIAL, ArmorItem.Type.BOOTS, new FabricItemSettings());

    public static void register() {
        Registry.register(Registries.ITEM, id("nuclear_bomb"), NUCLEAR_BOMB);
        Registry.register(Registries.ITEM, id("mini_nuke"), MINI_NUKE);
        Registry.register(Registries.ITEM, id("nuclear_mine"), NUCLEAR_MINE);
        Registry.register(Registries.ITEM, id("uranium_ore"), URANIUM_ORE);
        Registry.register(Registries.ITEM, id("deepslate_uranium_ore"), DEEPSLATE_URANIUM_ORE);
        Registry.register(Registries.ITEM, id("uranium_block"), URANIUM_BLOCK);
        Registry.register(Registries.ITEM, id("nuclear_waste_barrel"), NUCLEAR_WASTE_BARREL);
        Registry.register(Registries.ITEM, id("raw_uranium"), RAW_URANIUM);
        Registry.register(Registries.ITEM, id("uranium_ingot"), URANIUM_INGOT);
        Registry.register(Registries.ITEM, id("blast_concrete"), BLAST_CONCRETE);
        Registry.register(Registries.ITEM, id("uranium_glass"), URANIUM_GLASS);
        Registry.register(Registries.ITEM, id("uranium_bricks"), URANIUM_BRICKS);
        Registry.register(Registries.ITEM, id("irradiated_dirt"), IRRADIATED_DIRT);
        Registry.register(Registries.ITEM, id("uranium_lamp"), new BlockItem(ModBlocks.URANIUM_LAMP, new FabricItemSettings()));
        Registry.register(Registries.ITEM, id("briefcase_nuke"), BRIEFCASE_NUKE);
        Registry.register(Registries.ITEM, id("nuclear_torpedo"), NUCLEAR_TORPEDO);
        Registry.register(Registries.ITEM, id("dirty_bomb"), DIRTY_BOMB);
        Registry.register(Registries.ITEM, id("nuclear_rifle"), NUCLEAR_RIFLE);
        Registry.register(Registries.ITEM, id("uranium_pistol"), URANIUM_PISTOL);
        Registry.register(Registries.ITEM, id("radiation_shotgun"), RADIATION_SHOTGUN);
        Registry.register(Registries.ITEM, id("gauss_cannon"), GAUSS_CANNON);
        Registry.register(Registries.ITEM, id("uranium_bullet"), URANIUM_BULLET);
        Registry.register(Registries.ITEM, id("radiation_giant_spawn_egg"), RADIATION_GIANT_SPAWN_EGG);
        Registry.register(Registries.ITEM, id("uranium_golem_spawn_egg"), URANIUM_GOLEM_SPAWN_EGG);
        Registry.register(Registries.ITEM, id("nuclear_creeper_spawn_egg"), NUCLEAR_CREEPER_SPAWN_EGG);
        Registry.register(Registries.ITEM, id("geiger_counter"), GEIGER_COUNTER);
        Registry.register(Registries.ITEM, id("hazmat_helmet"), HAZMAT_HELMET);
        Registry.register(Registries.ITEM, id("hazmat_chestplate"), HAZMAT_CHESTPLATE);
        Registry.register(Registries.ITEM, id("hazmat_leggings"), HAZMAT_LEGGINGS);
        Registry.register(Registries.ITEM, id("hazmat_boots"), HAZMAT_BOOTS);
    }

    private static Identifier id(String path) {
        return new Identifier(NukeMod.MOD_ID, path);
    }
}
