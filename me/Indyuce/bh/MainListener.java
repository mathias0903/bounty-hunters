package me.Indyuce.bh;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
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

import me.Indyuce.bh.api.Bounty;
import me.Indyuce.bh.api.BountyClaimEvent;
import me.Indyuce.bh.api.BountyCreateEvent;
import me.Indyuce.bh.ressource.Items;
import me.Indyuce.bh.ressource.UserdataParams;

public class MainListener implements Listener {
	static HashMap<UUID, Long> last_bounty = new HashMap<UUID, Long>();

	public static Main plugin;

	public MainListener(Main ins) {
		plugin = ins;
	}

	@EventHandler
	public void a(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		ConfigData.setupCD(plugin, "/userdata", p.getUniqueId().toString());
		FileConfiguration config = ConfigData.getCD(plugin, "/userdata", p.getUniqueId().toString());
		for (UserdataParams pa : UserdataParams.values()) {
			String path = pa.name().toLowerCase().replace("_", "-");
			if (!config.getKeys(false).contains(path)) {
				config.set(path, pa.value);
			}
		}
		ConfigData.saveCD(plugin, config, "/userdata", p.getUniqueId().toString());
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void c(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (p.getKiller() == null) {
			return;
		}
		if (!(p.getKiller() instanceof Player)) {
			return;
		}
		if (p == p.getKiller()) {
			return;
		}
		
		// world blacklist
		if (plugin.getConfig().getStringList("world-blacklist").contains(p.getWorld().getName())) {
			return;
		}
		Player t = p.getKiller();
		FileConfiguration config = ConfigData.getCD(plugin, "", "data");
		if (config.getKeys(false).contains(p.getName())) {
			if (!t.hasPermission("bountyhunters.claim")) {
				return;
			}

			OfflinePlayer creator = null;
			double reward = config.getInt(p.getName() + ".reward");
			if (config.getConfigurationSection(p.getName()).contains("creator")) {
				creator = Bukkit.getOfflinePlayer(config.getString(p.getName() + ".creator"));
			}

			// API
			BountyClaimEvent e1 = new BountyClaimEvent(new Bounty(creator, p, reward), t);
			Bukkit.getPluginManager().callEvent(e1);
			if (e1.isCancelled()) {
				return;
			}
			reward = e1.getBounty().getReward();

			claimedBountyDrop(p);
			if (plugin.setupEco()) {
				plugin.getEco().depositPlayer(t, reward);
			}

			FileConfiguration t_config = ConfigData.getCD(plugin, "/userdata", t.getUniqueId().toString());
			t_config.set("claimed-bounties", t_config.getInt("claimed-bounties") + 1);
			ConfigData.saveCD(plugin, t_config, "/userdata", t.getUniqueId().toString());
			Utils.updateLvl(t);

			if (creator != null) {
				FileConfiguration t1_config = ConfigData.getCD(plugin, "/userdata", creator.getUniqueId().toString());
				t1_config.set("successful-bounties", t1_config.getInt("successful-bounties") + 1);

				ConfigData.saveCD(plugin, t1_config, "/userdata", creator.getUniqueId().toString());
			}

			Utils.bountyClaimedAlert(p.getKiller(), p);
			// death quote
			String deathQuote = t_config.getString("current-quote");
			if (!deathQuote.equals("")) {
				boolean bool = plugin.getConfig().getBoolean("display-death-quote-on-title");
				for (Player t2 : Bukkit.getOnlinePlayers()) {
					t2.sendMessage("§7§o" + t.getName() + "> " + deathQuote);
					if (bool) {
						plugin.title.sendTitle(t2, "§6§l" + t.getName().toUpperCase(), "§o" + deathQuote, 60);
					}
				}
			}

			config.set(p.getName(), null);
			ConfigData.saveCD(plugin, config, "", "data");
			if (plugin.getConfig().getBoolean("drop-head")) {
				ItemStack head = Items.PLAYER_HEAD.a().clone();
				SkullMeta head_meta = (SkullMeta) head.getItemMeta();
				head_meta.setDisplayName(head_meta.getDisplayName().replace("%name%", p.getName()));
				head_meta.setOwner(p.getName());
				head.setItemMeta(head_meta);

				p.getWorld().dropItemNaturally(p.getLocation(), head);
			}
			return;
		} else if (plugin.getConfig().getBoolean("auto-bounty")) {
			if (config.getConfigurationSection("").contains(t.getName())) {
				int reward = plugin.getConfig().getInt("auto-bounty-reward");

				// API
				BountyCreateEvent e1 = new BountyCreateEvent(new Bounty(null, t, reward));
				Bukkit.getPluginManager().callEvent(e1);
				if (e1.isCancelled()) {
					return;
				}

				config.set(t.getName() + ".reward", reward);
				ConfigData.saveCD(plugin, config, "", "data");
				Utils.autoBountyAlert(t);
			}
		}
	}

	@EventHandler
	public void d(PlayerPickupItemEvent e) {
		Item i = e.getItem();
		if (i.hasMetadata("BOUNTYHUNTERS:no_pickup")) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void e(InventoryPickupItemEvent e) {
		Item i = e.getItem();
		if (e.getInventory().getType() == InventoryType.HOPPER && i.hasMetadata("BOUNTYHUNTERS:no_pickup")) {
			e.setCancelled(true);
		}
	}

	void claimedBountyDrop(Player p) {
		// effect
		if (plugin.getConfig().getBoolean("bounty-effect.enabled")) {
			String material = plugin.getConfig().getString("bounty-effect.material").toUpperCase().replace("-", "_")
					.replace(" ", "_");
			try {
				for (int j = 0; j < 8; j++) {
					ItemStack stack = new ItemStack(Material.valueOf(material));
					ItemMeta stack_meta = stack.getItemMeta();
					stack_meta.setDisplayName("BOUNTYHUNTERS:chest " + p.getUniqueId().toString() + " " + j);
					stack.setItemMeta(stack_meta);

					Item item = p.getWorld().dropItemNaturally(p.getLocation(), stack);
					item.setMetadata("BOUNTYHUNTERS:no_pickup", new FixedMetadataValue(plugin, true));
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
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
		if (plugin.getConfig().getBoolean("physical-rewards.enabled")) {
			for (String s : plugin.getConfig().getConfigurationSection("physical-rewards.list").getKeys(false)) {
				try {
					String[] split = plugin.getConfig().getString("physical-rewards.list." + s)
							.split(Pattern.quote(" "));
					ItemStack i = new ItemStack(Material.valueOf(s.toUpperCase().replace("-", "_").replace(" ", "_")),
							(int) Double.parseDouble(split[0]), (split.length > 1 ? (short) Double.parseDouble(split[1]) : (short) 0));
					p.getWorld().dropItem(p.getLocation(), i);
				} catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("§4[Bounty Hunters] Wrong item format: " + s + ":"
							+ plugin.getConfig().getString("physical-rewards.list." + s));
				}
			}
		}
	}
}