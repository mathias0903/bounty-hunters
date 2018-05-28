package me.Indyuce.bh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import me.Indyuce.bh.api.Bounty;
import me.Indyuce.bh.api.BountyClaimEvent;
import me.Indyuce.bh.api.BountyCreateEvent;
import me.Indyuce.bh.reflect.Title;
import me.Indyuce.bh.resource.BountyCause;
import me.Indyuce.bh.resource.Items;
import me.Indyuce.bh.resource.UserdataParams;

@SuppressWarnings("deprecation")
public class MainListener implements Listener {
	public MainListener() {

		// target particles
		if (Main.plugin.getConfig().getBoolean("target-particles.enabled"))
			new BukkitRunnable() {
				final String permNode = Main.plugin.getConfig().getString("target-particles.permission");
				final boolean permBool = permNode.equals("");

				public void run() {
					FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
					for (String s : config.getKeys(false)) {
						Player p = Bukkit.getPlayer(s);
						if (p == null)
							continue;

						if (!config.getConfigurationSection(s).contains("hunters"))
							continue;

						List<Player> players = new ArrayList<Player>();
						for (String s1 : config.getStringList(s + ".hunters")) {
							Player t = Bukkit.getPlayer(s1);
							if (t != null)
								if (permBool || t.hasPermission(permNode))
									players.add(t);
						}

						if (players.size() > 0)
							new BukkitRunnable() {
								int ti = 0;
								Location loc = p.getLocation().clone().add(0, .1, 0);

								public void run() {
									ti++;
									if (ti > 2)
										cancel();

									for (double j = 0; j < Math.PI * 2; j += Math.PI / 16)
										Eff.REDSTONE.display(0, 0, 0, 0, 1, loc.clone().add(Math.cos(j) * .8, 0, Math.sin(j) * .8), players);
								}
							}.runTaskTimer(Main.plugin, 0, 7);
					}
				}
			}.runTaskTimer(Main.plugin, 0, 100);
	}

	@EventHandler
	public void a(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		ConfigData.setupCD(Main.plugin, "/userdata", p.getUniqueId().toString());
		FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
		for (UserdataParams pa : UserdataParams.values()) {
			String path = pa.name().toLowerCase().replace("_", "-");
			if (!config.getKeys(false).contains(path))
				config.set(path, pa.value);
		}
		ConfigData.saveCD(Main.plugin, config, "/userdata", p.getUniqueId().toString());
	}

	@EventHandler
	public void b(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (p.getKiller() == null)
			return;
		if (!(p.getKiller() instanceof Player))
			return;
		if (p == p.getKiller())
			return;

		// world blacklist
		if (Main.plugin.getConfig().getStringList("world-blacklist").contains(p.getWorld().getName()))
			return;

		Player t = p.getKiller();
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		if (config.getKeys(false).contains(p.getName())) {
			if (!t.hasPermission("bountyhunters.claim"))
				return;

			OfflinePlayer creator = null;
			double reward = config.getDouble(p.getName() + ".reward");
			if (config.getConfigurationSection(p.getName()).contains("creator"))
				creator = Bukkit.getOfflinePlayer(config.getString(p.getName() + ".creator"));
			if (creator != null)
				if (!Main.plugin.getConfig().getBoolean("own-bounty-claiming") && creator.getUniqueId() == t.getUniqueId())
					return;

			// API
			BountyClaimEvent bountyEvent = new BountyClaimEvent(new Bounty(creator, p, reward), t);
			Bukkit.getPluginManager().callEvent(bountyEvent);
			if (bountyEvent.isCancelled())
				return;
			reward = bountyEvent.getBounty().getReward();

			// bounty listener
			claimedBountyDrop(p);
			Main.plugin.economy.depositPlayer(t, reward);

			FileConfiguration t_config = ConfigData.getCD(Main.plugin, "/userdata", t.getUniqueId().toString());
			t_config.set("claimed-bounties", t_config.getInt("claimed-bounties") + 1);
			ConfigData.saveCD(Main.plugin, t_config, "/userdata", t.getUniqueId().toString());
			if (Main.plugin.getConfig().getBoolean("enable-quotes-levels-titles"))
				Utils.updateLvl(t);

			if (creator != null) {
				FileConfiguration t1_config = ConfigData.getCD(Main.plugin, "/userdata", creator.getUniqueId().toString());
				t1_config.set("successful-bounties", t1_config.getInt("successful-bounties") + 1);

				ConfigData.saveCD(Main.plugin, t1_config, "/userdata", creator.getUniqueId().toString());
			}

			Utils.bountyClaimedAlert(p.getKiller(), p);

			// death quote
			if (Main.plugin.getConfig().getBoolean("enable-quotes-levels-titles")) {
				String deathQuote = t_config.getString("current-quote");
				if (!deathQuote.equals("")) {
					boolean bool = Main.plugin.getConfig().getBoolean("display-death-quote-on-title");
					for (Player t2 : Bukkit.getOnlinePlayers()) {
						t2.sendMessage("§7§o" + t.getName() + "> " + deathQuote);
						if (bool)
							Title.title(t2, "§6§l" + t.getName().toUpperCase(), "§o" + deathQuote, 5, 60);
					}
				}
			}

			// save config
			config.set(p.getName(), null);
			ConfigData.saveCD(Main.plugin, config, "", "data");

			// drop head
			if (Main.plugin.getConfig().getBoolean("drop-head.enabled") && new Random().nextDouble() <= Main.plugin.getConfig().getDouble("drop-head.chance") / 100) {
				ItemStack head = Items.PLAYER_HEAD.a().clone();
				SkullMeta head_meta = (SkullMeta) head.getItemMeta();
				head_meta.setDisplayName(head_meta.getDisplayName().replace("%name%", p.getName()));
				head_meta.setOwner(p.getName());
				head.setItemMeta(head_meta);

				p.getWorld().dropItemNaturally(p.getLocation(), head);
			}
			return;
		} else if (Main.plugin.getConfig().getBoolean("auto-bounty.enabled")) {
			if (config.getConfigurationSection("").contains(t.getName())) {
				double reward = Main.plugin.getConfig().getDouble("auto-bounty.reward");

				// API
				BountyCreateEvent e1 = new BountyCreateEvent(new Bounty(null, t, reward), BountyCause.AUTO_BOUNTY);
				Bukkit.getPluginManager().callEvent(e1);
				if (e1.isCancelled())
					return;

				config.set(t.getName() + ".reward", reward);
				ConfigData.saveCD(Main.plugin, config, "", "data");
				Utils.newBountyAlert(e1);
			}
		}
	}

	@EventHandler
	public void c(PlayerPickupItemEvent e) {
		Item i = e.getItem();
		if (i.hasMetadata("BOUNTYHUNTERS:no_pickup"))
			e.setCancelled(true);
	}

	@EventHandler
	public void d(InventoryPickupItemEvent e) {
		Item i = e.getItem();
		if (e.getInventory().getType() == InventoryType.HOPPER && i.hasMetadata("BOUNTYHUNTERS:no_pickup"))
			e.setCancelled(true);
	}

	private void claimedBountyDrop(Player p) {
		// effect
		if (Main.plugin.getConfig().getBoolean("bounty-effect.enabled")) {
			String material = Main.plugin.getConfig().getString("bounty-effect.material").toUpperCase().replace("-", "_").replace(" ", "_");
			try {
				for (int j = 0; j < 8; j++) {
					ItemStack stack = new ItemStack(Material.valueOf(material));
					ItemMeta stack_meta = stack.getItemMeta();
					stack_meta.setDisplayName("BOUNTYHUNTERS:chest " + p.getUniqueId().toString() + " " + j);
					stack.setItemMeta(stack_meta);

					Item item = p.getWorld().dropItemNaturally(p.getLocation(), stack);
					item.setMetadata("BOUNTYHUNTERS:no_pickup", new FixedMetadataValue(Main.plugin, true));
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
						public void run() {
							item.remove();
						}
					}, 40 + new Random().nextInt(30));
				}
			} catch (Exception e) {
				Bukkit.getConsoleSender().sendMessage("§4[Bounty Hunters] No such material found: " + material + ".");
			}
		}

		// physical reward
		if (Main.plugin.getConfig().getBoolean("physical-rewards.enabled"))
			for (String s : Main.plugin.getConfig().getConfigurationSection("physical-rewards.list").getKeys(false)) {
				try {
					String[] split = Main.plugin.getConfig().getString("physical-rewards.list." + s).split(Pattern.quote(" "));
					ItemStack i = new ItemStack(Material.valueOf(s.toUpperCase().replace("-", "_").replace(" ", "_")), (int) Double.parseDouble(split[0]), (split.length > 1 ? (short) Double.parseDouble(split[1]) : (short) 0));
					p.getWorld().dropItem(p.getLocation(), i);
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("§4[Bounty Hunters] Wrong item format: " + s + ":" + Main.plugin.getConfig().getString("physical-rewards.list." + s));
				}
			}
	}
}