package me.jishuna.toollevelling.api.upgrades;

import org.bukkit.entity.Player;

public class UpgradeData {
	private final Player player;
	private final int level;
	
	
	public UpgradeData(Player player, int level) {
		this.player = player;
		this.level = level;
	}

	public Player getPlayer() {
		return player;
	}


	public int getLevel() {
		return level;
	}

}
