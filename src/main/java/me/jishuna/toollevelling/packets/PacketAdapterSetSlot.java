package me.jishuna.toollevelling.packets;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import me.jishuna.toollevelling.PluginKeys;
import me.jishuna.toollevelling.ToolLevelling;
import me.jishuna.toollevelling.api.tools.ToolType;
import net.md_5.bungee.api.ChatColor;

public class PacketAdapterSetSlot extends PacketAdapter {

	private ToolLevelling plugin;
	private final DecimalFormat decimalFormat = new DecimalFormat("##.#");
	private final List<String> loreCache = new ArrayList<>();

	public PacketAdapterSetSlot(ToolLevelling plugin, ListenerPriority listenerPriority) {
		super(plugin, listenerPriority, PacketType.Play.Server.SET_SLOT);
		this.plugin = plugin;
	}

	public void cacheLore(ConfigurationSection section) {
		this.loreCache.clear();

		for (String line : section.getStringList("item-display")) {
			this.loreCache.add(ChatColor.translateAlternateColorCodes('&', line));
		}
	}

	@Override
	public void onPacketSending(PacketEvent event) {
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

		for (int i = 0; i < this.loreCache.size(); i++) {
			String line = this.loreCache.get(i);
			line = line.replace("%level%", Integer.toString(level));
			line = line.replace("%points%", Integer.toString(points));
			line = line.replace("%experience%", decimalFormat.format(experience));
			line = line.replace("%nextlevel%", decimalFormat.format(nextLevel));
			line = line.replace("%percentage%", decimalFormat.format((experience / nextLevel) * 100));

			if (hasLore) {
				lore.set(i, line);
			} else {
				lore.add(line);
			}
		}

		meta.setLore(lore);
		item.setItemMeta(meta);

		packet.getItemModifier().write(0, item);
		event.setPacket(packet);
	}

}
