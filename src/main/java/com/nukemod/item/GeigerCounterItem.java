package com.nukemod.item;

import com.nukemod.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/**
 * 盖革计数器 - 手持时探测周围辐射，发出"咔嗒"声
 * 辐射越近/越强 → 音调越高
 */
public class GeigerCounterItem extends Item {

	private static final List<Block> RADIOACTIVE_BLOCKS = List.of(
			ModBlocks.URANIUM_BLOCK,
			ModBlocks.URANIUM_ORE,
			ModBlocks.DEEPSLATE_URANIUM_ORE,
			ModBlocks.IRRADIATED_DIRT,
			ModBlocks.NUCLEAR_WASTE_BARREL,
			ModBlocks.URANIUM_BRICKS
	);

	private static final int SCAN_RADIUS = 16;
	private static final int SCAN_STEP = 2;

	public GeigerCounterItem(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient) return;
		if (!selected && !(entity instanceof PlayerEntity player && player.getOffHandStack() == stack)) return;
		if (world.getTime() % 8 != 0) return;

		BlockPos center = entity.getBlockPos();
		double totalSignal = 0;

		for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx += SCAN_STEP) {
			for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy += SCAN_STEP) {
				for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz += SCAN_STEP) {
					double distSq = dx * dx + dy * dy + dz * dz;
					if (distSq > SCAN_RADIUS * SCAN_RADIUS) continue;

					Block block = world.getBlockState(center.add(dx, dy, dz)).getBlock();
					if (RADIOACTIVE_BLOCKS.contains(block)) {
						double dist = Math.max(1.0, Math.sqrt(distSq));
						totalSignal += getBlockDanger(block) / (dist * dist);
					}
				}
			}
		}

		if (totalSignal > 0.001) {
			float volume = (float) MathHelper.clamp(totalSignal * 2, 0.3, 1.5);
			float pitch = (float) MathHelper.clamp(0.3 + totalSignal * 3, 0.3, 2.0);
			world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
					SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.PLAYERS,
					volume, pitch);
		}
	}

	private double getBlockDanger(Block block) {
		if (block == ModBlocks.URANIUM_BLOCK)  return 4.0;
		if (block == ModBlocks.URANIUM_ORE || block == ModBlocks.DEEPSLATE_URANIUM_ORE) return 2.0;
		if (block == ModBlocks.IRRADIATED_DIRT) return 1.0;
		if (block == ModBlocks.NUCLEAR_WASTE_BARREL) return 3.0;
		if (block == ModBlocks.URANIUM_BRICKS) return 0.5;
		return 0;
	}
}
