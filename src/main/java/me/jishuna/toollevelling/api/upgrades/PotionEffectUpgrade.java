package me.jishuna.toollevelling.api.upgrades;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

public class PotionEffectUpgrade extends CustomUpgrade {
	private final PotionEffectType type;

	private CompiledExpression chancePerLevel;
	private CompiledExpression duration;
	private int amplifier;

	public PotionEffectUpgrade(Plugin owner, String key, PotionEffectType type) {
		super(owner, key, loadConfig(owner, key));
		this.type = type;

		addEventHandler(EntityDamageByEntityEvent.class, this::onDamage);
	}

	@Override
	protected void loadData(YamlConfiguration upgradeConfig) {
		super.loadData(upgradeConfig);

		this.chancePerLevel = Crunch.compileExpression(upgradeConfig.getString("chance-formula", "level * 5"),
				getEvalenvironment());
		this.duration = Crunch.compileExpression(upgradeConfig.getString("duration-formula", "level * 20"),
				getEvalenvironment());
	}

	private void onDamage(EntityDamageByEntityEvent event, UpgradeData data) {
		Entity target = event.getEntity();

		if (!(target instanceof LivingEntity))
			return;

		int level = data.getLevel();
		double chance = getRandom().nextDouble() * 100;

		if (chance > this.chancePerLevel.evaluate(level))
			return;

		((LivingEntity) target).addPotionEffect(
				new PotionEffect(this.type, (int) this.duration.evaluate(level), this.amplifier, true));
	}
}
