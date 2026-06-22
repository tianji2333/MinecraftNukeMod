package com.nukemod.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * 辐射效果 - 绿色粒子，持续伤害，随时间衰减
 */
public class RadiationEffect extends StatusEffect {

	private static final int COLOR = 0x55FF55; // 荧光绿

	public RadiationEffect() {
		super(StatusEffectCategory.HARMFUL, COLOR);
	}

	@Override
	public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		// 每 tick 伤害 = (等级+1) × 0.5
		float damage = 0.5f * (amplifier + 1);
		entity.damage(entity.getDamageSources().magic(), damage);
	}

	@Override
	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		// 等级越高扣血越快
		int interval = Math.max(10, 40 - amplifier * 5);
		return duration % interval == 0;
	}
}
