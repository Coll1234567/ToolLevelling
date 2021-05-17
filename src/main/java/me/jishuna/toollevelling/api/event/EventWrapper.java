package me.jishuna.toollevelling.api.event;

import java.util.function.BiConsumer;

import org.bukkit.event.Event;

import me.jishuna.toollevelling.api.upgrades.UpgradeData;

public class EventWrapper<T extends Event> {

	private BiConsumer<T, UpgradeData> handler;
	private Class<T> eventClass;

	public EventWrapper(Class<T> eventClass, BiConsumer<T, UpgradeData> handler) {
		this.handler = handler;
		this.eventClass = eventClass;
	}

	public void consume(Event event, UpgradeData data) {
		if (this.eventClass.isAssignableFrom(event.getClass())) {
			handler.accept(this.eventClass.cast(event), data);
		}
	}
}
