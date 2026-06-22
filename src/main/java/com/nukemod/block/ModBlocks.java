package com.nukemod.block;

import com.nukemod.NukeMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block NUCLEAR_BOMB = new NuclearBombBlock(
            AbstractBlock.Settings.copy(Blocks.TNT).strength(0.5F, 0.5F),
            100.0F
    );

    public static final Block MINI_NUKE = new NuclearBombBlock(
            AbstractBlock.Settings.copy(Blocks.TNT).strength(0.3F, 0.3F),
            30.0F
    );

    public static final Block NUCLEAR_MINE = new NuclearMineBlock(
            AbstractBlock.Settings.copy(Blocks.TNT).strength(0.3F, 0.3F),
            30.0F
    );

    public static final Block URANIUM_ORE = new Block(
            AbstractBlock.Settings.copy(Blocks.STONE)
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(3.0F, 3.0F)
                    .luminance(state -> 4)
                    .requiresTool()
    );

    public static final Block DEEPSLATE_URANIUM_ORE = new Block(
            AbstractBlock.Settings.copy(Blocks.DEEPSLATE)
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(4.5F, 3.0F)
                    .luminance(state -> 4)
                    .requiresTool()
                    .sounds(BlockSoundGroup.DEEPSLATE)
    );

    public static final Block URANIUM_BLOCK = new UraniumBlock(
            AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(5.0F, 6.0F)
                    .luminance(state -> 7)
                    .requiresTool()
    );

    public static final Block NUCLEAR_WASTE_BARREL = new NuclearWasteBarrelBlock(
            AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)
                    .mapColor(MapColor.GREEN)
                    .strength(2.0F, 3.0F)
                    .luminance(state -> 3)
    );

    public static final Block BLAST_CONCRETE = new Block(
            AbstractBlock.Settings.copy(Blocks.STONE)
                    .mapColor(MapColor.GRAY)
                    .strength(6.0F, 600.0F)
                    .requiresTool()
    );

    public static final Block URANIUM_GLASS = new Block(
            AbstractBlock.Settings.copy(Blocks.GREEN_STAINED_GLASS)
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(2.0F, 3.0F)
                    .luminance(state -> 7)
                    .nonOpaque()
    );

    public static final Block URANIUM_BRICKS = new Block(
            AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(4.0F, 8.0F)
                    .luminance(state -> 3)
                    .requiresTool()
    );

    public static final Block IRRADIATED_DIRT = new IrradiatedDirtBlock(
            AbstractBlock.Settings.copy(Blocks.DIRT)
                    .mapColor(MapColor.GREEN)
                    .strength(0.6F, 0.6F)
                    .luminance(state -> 2)
    );

    public static final Block URANIUM_LAMP = new Block(
            AbstractBlock.Settings.copy(Blocks.GLOWSTONE)
                    .mapColor(MapColor.EMERALD_GREEN)
                    .strength(1.5F, 3.0F)
                    .luminance(state -> 15)
                    .nonOpaque()
    );

    public static final Block DIRTY_BOMB = new NuclearBombBlock(
            AbstractBlock.Settings.copy(Blocks.TNT).strength(0.3F, 0.3F),
            15.0F
    );

    public static void register() {
        Registry.register(Registries.BLOCK, id("nuclear_bomb"), NUCLEAR_BOMB);
        Registry.register(Registries.BLOCK, id("mini_nuke"), MINI_NUKE);
        Registry.register(Registries.BLOCK, id("nuclear_mine"), NUCLEAR_MINE);
        Registry.register(Registries.BLOCK, id("uranium_ore"), URANIUM_ORE);
        Registry.register(Registries.BLOCK, id("deepslate_uranium_ore"), DEEPSLATE_URANIUM_ORE);
        Registry.register(Registries.BLOCK, id("uranium_block"), URANIUM_BLOCK);
        Registry.register(Registries.BLOCK, id("nuclear_waste_barrel"), NUCLEAR_WASTE_BARREL);
        Registry.register(Registries.BLOCK, id("blast_concrete"), BLAST_CONCRETE);
        Registry.register(Registries.BLOCK, id("uranium_glass"), URANIUM_GLASS);
        Registry.register(Registries.BLOCK, id("uranium_bricks"), URANIUM_BRICKS);
        Registry.register(Registries.BLOCK, id("irradiated_dirt"), IRRADIATED_DIRT);
        Registry.register(Registries.BLOCK, id("uranium_lamp"), URANIUM_LAMP);
        Registry.register(Registries.BLOCK, id("dirty_bomb"), DIRTY_BOMB);
    }

    private static Identifier id(String path) {
        return new Identifier(NukeMod.MOD_ID, path);
    }
}
