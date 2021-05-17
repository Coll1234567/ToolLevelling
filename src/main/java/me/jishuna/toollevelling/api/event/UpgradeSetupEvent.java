package me.jishuna.toollevelling.api.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.jishuna.toollevelling.api.upgrades.Upgrade;

public class UpgradeSetupEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final List<Upgrade> upgradesToAdd = new ArrayList<>();

	public List<Upgrade> getUpgradesToAdd() {
		return upgradesToAdd;
	}

	@Override
	public HandlerList getHandlers() {
		return getHandlerList();
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
