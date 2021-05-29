package me.jishuna.toollevelling.api.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.jishuna.commonlib.CustomInventory;
import me.jishuna.commonlib.ItemBuilder;
import me.jishuna.commonlib.MessageConfig;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.tools.ToolType;
import me.jishuna.toollevelling.api.upgrades.Upgrade;

public class UpgradeMenu extends CustomInventory {
	private final ToolInventory inventory;

	private final Map<Material, Map<Integer, Upgrade>> upgradeCache = new HashMap<>();

	public UpgradeMenu(ToolInventory inventory) {
		super(null, 27, "Test");

		this.inventory = inventory;

		addClickConsumer(event -> event.setCancelled(true));

		for (int i = 0; i < 27; i++) {
			setItem(i, this.inventory.getFiller());
		}

		ToolLevelling plugin = inventory.getPlugin();
		
		addButton(18, new ItemBuilder(Material.ARROW).withName(plugin.getMessageConfig().getString("back-item.name"))
				.withLore(plugin.getMessageConfig().getStringList("back-item.lore")).build(),
				event -> this.inventory.show(event.getWhoClicked()));
	}

	public void show(HumanEntity player) {
		this.inventory.getPlugin().getInventoryManager().openGui(player, this);
	}
	
	public void refreshInventory() {
		ItemStack item = this.inventory.getItem();

		populateOptions(this.upgradeCache.get(item.getType()));
	}

	private void populateOptions(Map<Integer, Upgrade> slotMap) {
		if (slotMap != null && !slotMap.isEmpty()) {
			for (Entry<Integer, Upgrade> entry : slotMap.entrySet()) {
				int slot = entry.getKey();
				Upgrade upgrade = entry.getValue();

				ItemStack upgradeItem = new ItemBuilder(Material.BOOK).withName(upgrade.getName())
						.withLore(upgrade.getDescription()).build();

				addButton(slot, upgradeItem, this::onUpgradeClick);
			}
		} else {
			ItemStack item = this.inventory.getItem();
			ToolLevelling plugin = this.inventory.getPlugin();

			ToolType type = plugin.getToolTypeManager().getToolType(item.getType());

			for (int i = 11; i < 17; i += 2) {
				Optional<Upgrade> upgradeOptional = plugin.getUpgradeManager().getRandomUpgradeSmart(type, item, 10);

				if (upgradeOptional.isPresent()) {
					Upgrade upgrade = upgradeOptional.get();
					this.upgradeCache.computeIfAbsent(item.getType(), key -> new HashMap<>()).put(i, upgrade);

					ItemStack upgradeItem = upgrade.asItem(plugin, Material.BOOK, null);

					addButton(i, upgradeItem, this::onUpgradeClick);
				} else {
					// TODO handle this better
					removeButton(i);
					setItem(i, this.inventory.getFiller());
					this.upgradeCache.computeIfAbsent(item.getType(), key -> new HashMap<>()).remove(i);
				}
			}
		}

		if (this.inventory.getPoints() > 1) {
			MessageConfig config = this.inventory.getPlugin().getMessageConfig();
			ItemStack upgradeItem = new ItemBuilder(Material.BARREL).withName(config.getString("refresh-item.name"))
					.withLore(config.getStringList("refresh-item.lore")).build();

			addButton(22, upgradeItem, this::onRefreshClick);
		} else {
			removeButton(22);
			setItem(22, this.inventory.getFiller());
		}
	}

	private void onRefreshClick(InventoryClickEvent event) {
		if (this.inventory.getPoints() > 1) {
			this.inventory.removePoint();
			populateOptions(null);
		}
	}

	private void onUpgradeClick(InventoryClickEvent event) {
		if (this.inventory.getPoints() > 0) {
			Material material = this.inventory.getItem().getType();
			Upgrade upgrade = this.upgradeCache.get(material).get(event.getRawSlot());

			if (upgrade != null && upgrade.getLevel(this.inventory.getItem()) < upgrade.getMaxLevel()) {
				upgrade.onLevelup(this.inventory.getItem());
				this.inventory.removePoint();
			}

			this.inventory.onUpgradeComplete(event.getWhoClicked());
			populateOptions(null);
		}
	}
}