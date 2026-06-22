package com.nukemod.block;

import com.nukemod.NukeMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class IrradiatedDirtBlock extends Block {

	public IrradiatedDirtBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (random.nextFloat() < 0.3f) {
			world.spawnParticles(ParticleTypes.INSTANT_EFFECT,
					pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(),
					1, 0, 0, 0, 0);
		}
		for (int i = 0; i < 4; i++) {
			BlockPos target = pos.add(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
			BlockState ts = world.getBlockState(target);
			if (ts.getBlock() == net.minecraft.block.Blocks.GRASS_BLOCK ||
				ts.getBlock() == net.minecraft.block.Blocks.DIRT ||
				ts.getBlock() == net.minecraft.block.Blocks.PODZOL) {
				world.setBlockState(target, state);
			}
		}
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!world.isClient && entity instanceof LivingEntity living && !hasHazmatSuit(living)) {
			living.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 80, 0));
		}
		super.onSteppedOn(world, pos, state, entity);
	}

	private boolean hasHazmatSuit(LivingEntity entity) {
		int count = 0;
		for (net.minecraft.item.ItemStack armor : entity.getArmorItems()) {
			Identifier id = Registries.ITEM.getId(armor.getItem());
			if (id.getNamespace().equals("nukemod") && id.getPath().startsWith("hazmat_")) count++;
		}
		return count >= 4;
	}
}
