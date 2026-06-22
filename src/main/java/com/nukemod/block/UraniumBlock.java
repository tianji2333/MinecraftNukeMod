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

public class UraniumBlock extends Block {

	private static final int RADIUS = 6;

	public UraniumBlock(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		Box box = new Box(pos).expand(RADIUS);
		world.getEntitiesByClass(LivingEntity.class, box, entity ->
				entity.isAlive() && !entity.isSpectator() && !entity.isInvulnerable() && !hasHazmatSuit(entity))
				.forEach(entity -> {
					double dist = entity.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
					int amp = Math.max(0, (int)(2 * (1 - dist / (RADIUS * RADIUS))));
					entity.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 100, amp));
				});

		for (int i = 0; i < 3; i++) {
			world.spawnParticles(ParticleTypes.INSTANT_EFFECT,
					pos.getX() + random.nextDouble(), pos.getY() + 1.0 + random.nextDouble() * 0.5, pos.getZ() + random.nextDouble(),
					1, 0, 0, 0, 0);
		}
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!world.isClient && entity instanceof LivingEntity living && !hasHazmatSuit(living)) {
			living.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 120, 1));
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
