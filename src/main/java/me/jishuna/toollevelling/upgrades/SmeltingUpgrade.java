package me.jishuna.toollevelling.upgrades;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.plugin.Plugin;

import me.jishuna.toollevelling.api.upgrades.CustomUpgrade;
import me.jishuna.toollevelling.api.upgrades.UpgradeData;

public class SmeltingUpgrade extends CustomUpgrade {
	private static final String KEY = "auto_smelt";

	private final Map<Material, Material> smeltingMap = new EnumMap<>(Material.class);
	private Set<Material> blacklist;

	public SmeltingUpgrade(Plugin owner) {
		super(owner, KEY, loadConfig(owner, KEY));

		Iterator<Recipe> iterator = Bukkit.recipeIterator();

		while (iterator.hasNext()) {
			Recipe recipe = iterator.next();

			if (recipe instanceof CookingRecipe) {
				CookingRecipe<?> furnaceRecipe = (CookingRecipe<?>) recipe;
				if (furnaceRecipe.getInputChoice() instanceof MaterialChoice) {
					for (Material input : ((MaterialChoice) furnaceRecipe.getInputChoice()).getChoices()) {
						this.smeltingMap.put(input, furnaceRecipe.getResult().getType());
					}
				} else {
					this.smeltingMap.put(furnaceRecipe.getInput().getType(), furnaceRecipe.getResult().getType());
				}
			}
		}

		addEventHandler(BlockDropItemEvent.class, this::onBlockBreak);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);
		
		this.blacklist = new HashSet<>();

		for (String mat : upgradeConfig.getStringList("blacklisted-materials")) {
			Material mateiral = Material.matchMaterial(mat.toUpperCase());

			if (mateiral != null) {
				this.blacklist.add(mateiral);
			}
		}
	}

	private void onBlockBreak(BlockDropItemEvent event, UpgradeData data) {
		for (Item item : event.getItems()) {
			ItemStack itemstack = item.getItemStack();
			Material material = itemstack.getType();

			if (this.blacklist.contains(material))
				continue;

			Material smelted = this.smeltingMap.get(material);

			if (smelted != null) {
				itemstack.setType(smelted);
				item.setItemStack(itemstack);

				data.getPlayer().spawnParticle(Particle.FLAME, event.getBlock().getLocation().add(0.5, 0.5, 0.5), 10,
						0.25, 0.25, 0.25, 0);
			}
		}
	}

}
