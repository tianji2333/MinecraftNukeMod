package com.nukemod.item;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * 防辐射服材质 - 物理防御极低但免疫辐射
 */
public class HazmatArmorMaterial implements ArmorMaterial {

	private static final int[] DURABILITY = { 130, 150, 160, 110 }; // boots, legs, chest, head
	private static final int[] PROTECTION = { 1, 2, 2, 1 };          // 物理防御≈皮革

	@Override
	public int getDurability(ArmorItem.Type type) {
		return DURABILITY[type.ordinal()];
	}

	@Override
	public int getProtection(ArmorItem.Type type) {
		return PROTECTION[type.ordinal()];
	}

	@Override
	public int getEnchantability() {
		return 5;
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.ofItems(net.minecraft.item.Items.LEATHER);
	}

	@Override
	public String getName() {
		return "hazmat";
	}

	@Override
	public float getToughness() {
		return 0;
	}

	@Override
	public float getKnockbackResistance() {
		return 0;
	}
}
