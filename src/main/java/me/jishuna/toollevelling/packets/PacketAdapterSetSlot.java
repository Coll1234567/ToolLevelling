package me.jishuna.toollevelling.packets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import me.jishuna.commonlib.MessageConfig;
import me.jishuna.commonlib.StringUtils;
import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.tools.ToolType;
import me.jishuna.toollevelling.api.upgrades.Upgrade;
import net.md_5.bungee.api.ChatColor;

public class PacketAdapterSetSlot extends PacketAdapter {

	private ToolLevelling plugin;
	private final DecimalFormat decimalFormat = new DecimalFormat("##.#");
	private final List<String> loreCache = new ArrayList<>();

	public PacketAdapterSetSlot(ToolLevelling plugin, ListenerPriority listenerPriority) {
		super(plugin, listenerPriority, PacketType.Play.Server.SET_SLOT);
		this.plugin = plugin;
	}

	public void cacheLore(MessageConfig config) {
		this.loreCache.clear();

		for (String line : config.getStringList("item-display")) {
			this.loreCache.add(ChatColor.translateAlternateColorCodes('&', line));
		}
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		PacketContainer packet = event.getPacket();
		ItemStack item = packet.getItemModifier().read(0).clone();

		if (item.getType().isAir())
			return;

		if (plugin.getToolTypeManager().getToolType(item.getType()) == ToolType.NONE)
			return;

		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer container = meta.getPersistentDataContainer();

		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		boolean hasLore = !lore.isEmpty();

		int level = container.getOrDefault(PluginKeys.LEVEL.getKey(), PersistentDataType.INTEGER, 0);
		int points = container.getOrDefault(PluginKeys.POINTS.getKey(), PersistentDataType.INTEGER, 0);
		double experience = container.getOrDefault(PluginKeys.EXPERIENCE.getKey(), PersistentDataType.DOUBLE, 0d);
		double nextLevel = container.getOrDefault(PluginKeys.NEXT_LEVEL.getKey(), PersistentDataType.DOUBLE, 0d);

		if (experience == 0 && level == 0)
			return;

		PersistentDataContainer upgradeContainer = container.get(PluginKeys.UPGRADE_COMPOUND.getKey(),
				PersistentDataType.TAG_CONTAINER);

		if (upgradeContainer != null) {
			
			for (NamespacedKey upgradeKey : upgradeContainer.getKeys()) {
				Optional<Upgrade> upgradeOptional = this.plugin.getUpgradeManager().getUpgrade(upgradeKey.getKey());

				if (!upgradeOptional.isPresent())
					continue;

				Upgrade upgrade = upgradeOptional.get();
				String line = upgrade.getName() + " " + StringUtils
						.toRomanNumeral(upgradeContainer.getOrDefault(upgradeKey, PersistentDataType.INTEGER, 1));

				addLore(line, lore, hasLore);
			}
			addLore(" ", lore, hasLore);
		}

		for (int i = 0; i < this.loreCache.size(); i++) {
			String line = this.loreCache.get(i);

			// TODO Ugly, replace it
			line = line.replace("%level%", Integer.toString(level));
			line = line.replace("%points%", Integer.toString(points));
			line = line.replace("%experience%", decimalFormat.format(experience));
			line = line.replace("%nextlevel%", decimalFormat.format(nextLevel));
			line = line.replace("%percentage%", decimalFormat.format((experience / nextLevel) * 100));

			addLore(line, lore, hasLore);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);

		packet.getItemModifier().write(0, item);
		event.setPacket(packet);
	}

	private void addLore(String line, List<String> lore, boolean hasLore) {
		if (hasLore) {
			lore.add(0, line);
		} else {
			lore.add(line);
		}
	}

}
