package me.jishuna.toollevelling.api.inventory;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.jishuna.commonlib.CustomInventory;
import me.jishuna.commonlib.ItemBuilder;
import me.jishuna.commonlib.MessageConfig;
import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.upgrades.Upgrade;
import me.jishuna.toollevelling.api.utils.ItemUpdater;

public class ToolInventory extends CustomInventory {
	private final ToolLevelling plugin;
	private final ItemStack filler;
	private final UpgradeMenu upgradeMenu;

	private ItemStack item;
	private int points;

	public ToolInventory(ToolLevelling plugin) {
		super(null, 27, "Test");

		this.plugin = plugin;
		this.filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).withName(" ").build();
		this.upgradeMenu = new UpgradeMenu(this);

		addClickConsumer(event -> event.setCancelled(true));

		for (int i = 0; i < 27; i++) {
			setItem(i, this.filler);
		}

		addButton(18,
				new ItemBuilder(Material.BARRIER).withName(plugin.getMessageConfig().getString("close-item.name"))
						.withLore(plugin.getMessageConfig().getStringList("close-item.lore")).build(),
				event -> event.getWhoClicked().closeInventory());
	}

	public void show(HumanEntity player) {
		this.plugin.getInventoryManager().openGui(player, this);
	}

	private void showUpgradeMenu(InventoryClickEvent event) {
		if (this.points > 0)
			this.upgradeMenu.show(event.getWhoClicked());
	}

	public void onUpgradeComplete(HumanEntity player) {
		refreshInventory();

		show(player);
	}

	private void refreshInventory() {
		Map<Upgrade, Integer> upgradeMap = this.plugin.getUpgradeManager().getAllUpgrades(this.item);

		if (!upgradeMap.isEmpty()) {
			addButton(11, new ItemBuilder(Material.BOOK).withName(plugin.getMessageConfig().getString("upgrades-item.name"))
					.withLore(plugin.getMessageConfig().getStringList("upgrades-item.lore")).build(),
					event -> this.plugin.getInventoryManager().openGui(event.getWhoClicked(),
							new UpgradesMenu(this, this.plugin.getUpgradeManager().getAllUpgrades(this.item))));
		} else {
			removeButton(11);
			setItem(11, this.filler);
		}

		setItem(13, this.item.clone());

		if (this.points > 0) {
			MessageConfig config = this.plugin.getMessageConfig();
			addButton(15, new ItemBuilder(Material.PAPER).withName(config.getString("purchase-upgrades-item.name"))
					.withLore(config.getStringList("purchase-upgrades-item.lore")).build(), this::showUpgradeMenu);
		} else {
			removeButton(15);
			setItem(15, this.filler);
		}
	}

	public ItemStack getFiller() {
		return filler;
	}

	public ToolLevelling getPlugin() {
		return plugin;
	}

	public ItemStack getItem() {
		return this.item;
	}

	public void setItem(ItemStack item) {

		if (item == null)
			return;

		this.item = item;

		PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
		this.points = container.getOrDefault(PluginKeys.POINTS.getKey(), PersistentDataType.INTEGER, 0);

		refreshInventory();
		this.upgradeMenu.refreshInventory();
	}

	public int getPoints() {
		return points;
	}

	public void removePoint() {
		this.points--;

		ItemMeta meta = this.item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		container.set(PluginKeys.POINTS.getKey(), PersistentDataType.INTEGER, this.points);

		this.item.setItemMeta(meta);

		ItemUpdater.updateItem(this.plugin, item, true);
	}

}
