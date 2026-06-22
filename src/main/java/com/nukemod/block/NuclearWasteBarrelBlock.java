package com.nukemod.block;

import com.nukemod.NukeMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class NuclearWasteBarrelBlock extends Block {

	public NuclearWasteBarrelBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		Box box = new Box(pos).expand(4);
		world.getEntitiesByClass(LivingEntity.class, box, entity ->
				entity.isAlive() && !entity.isSpectator() && !hasHazmatSuit(entity))
				.forEach(entity -> {
					entity.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 100, 0));
					entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 1));
				});

		if (random.nextFloat() < 0.3f) {
			world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
					pos.getY() + 1.0,
					pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8,
					2, 0, 0.1, 0, 0.02);
		}
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!world.isClient && entity instanceof LivingEntity living && !hasHazmatSuit(living)) {
			living.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 160, 1));
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 200, 2));
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
