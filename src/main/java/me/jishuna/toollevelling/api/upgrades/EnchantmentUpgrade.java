package me.jishuna.toollevelling.api.upgrades;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class EnchantmentUpgrade extends Upgrade {
	private final Enchantment enchantment;

	public EnchantmentUpgrade(Enchantment enchantment, YamlConfiguration upgradeConfig) {
		super(enchantment.getKey().getKey().toLowerCase(), upgradeConfig);

		this.enchantment = enchantment;
	}

	@Override
	public void onLevelup(ItemStack item) {
		int level = getLevel(item);
		item.removeEnchantment(this.enchantment);
		item.addUnsafeEnchantment(this.enchantment, level + 1);
	}

	@Override
	public int getLevel(ItemStack item) {
		return item.getEnchantmentLevel(this.enchantment);
	}

}
