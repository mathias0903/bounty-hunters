package me.Indyuce.bh;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.Indyuce.bh.reflect.Json;
import me.Indyuce.bh.resource.Items;
import me.Indyuce.bh.resource.SpecialChar;

public class Utils implements Listener {
	public Utils() {
		if (Main.plugin.getConfig().getBoolean("compass.enabled")) {
			new BukkitRunnable() {
				public void run() {
					for (Player p : Bukkit.getOnlinePlayers())
						loop(p);
				}
			}.runTaskTimer(Main.plugin, 0, 10);
		}
	}

	public static boolean isCompass(ItemStack i) {
		if (!isPluginItem(i, true))
			return false;
		List<String> lore = i.getItemMeta().getLore();
		List<String> lore1 = Items.BOUNTY_COMPASS.a().getItemMeta().getLore();
		for (int j = 0; j < lore1.size(); j++)
			if (!lore.get(j).equals(lore1.get(j)))
				return false;
		return true;
	}

	public static String applySpecialChars(String s) {
		return s.replace("%star%", SpecialChar.star).replace("%square%", SpecialChar.square);
	}

	public static String format(double n) {
		if (!Main.plugin.getConfig().getBoolean("formatted-numbers"))
			return "" + n;
		String[] prefixes = new String[] { "M", "B", "Tril", "Quad", "Quin", "Sext", "Sept", "Octi", "Noni", "Deci" };
		for (int j = 9; j >= 0; j--) {
			double b = Math.pow(10, 6 + 3 * j);
			if (n > b)
				return tronc(n / b, 3) + prefixes[j];
		}
		return "" + n;
	}

	public static double tronc(double x, int n) {
		double pow = Math.pow(10.0, n);
		return Math.floor(x * pow) / pow;
	}

	void loop(Player p) {
		ItemStack i = VersionUtils.getMainItem(p);
		if (!isCompass(i))
			return;
		Player t = null;

		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		for (String s : config.getKeys(false))
			if (config.getConfigurationSection(s).contains("hunters")) {
				List<String> hunters = config.getStringList(s + ".hunters");
				if (hunters.contains(p.getName())) {
					t = Bukkit.getPlayer(s);
					break;
				}
			}

		if (t == null)
			return;

		String format = msg("in-another-world");
		if (p.getWorld().getName().equals(t.getWorld().getName())) {
			format = (Main.plugin.getConfig().getBoolean("round-distance") ? (int) (t.getLocation().distance(p.getLocation())) : tronc(t.getLocation().distance(p.getLocation()), 3)) + " blocks";
			p.setCompassTarget(t.getLocation().clone().add(.5, 0, .5));
		}

		ItemMeta i_meta = i.getItemMeta();
		i_meta.setDisplayName("§7§l[ §6§l" + format + " §7§l]");
		i.setItemMeta(i_meta);
	}

	public static boolean isPluginItem(ItemStack i, boolean lore) {
		if (i == null)
			return false;
		if (i.getItemMeta() == null)
			return false;
		if (i.getItemMeta().getDisplayName() == null)
			return false;
		if (lore && i.getItemMeta().getLore() == null)
			return false;
		return true;
	}

	public static String updateName(String folder_path, String file, String path, String main_name, String name) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, folder_path, file);
		if (config.getConfigurationSection("").getKeys(false).contains(main_name))
			return config.getString(main_name + "." + path);
		return name;
	}

	public static String[] updateList(String folder_path, String file, String path, String name, String[] list) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, folder_path, file);
		if (config.getConfigurationSection("").getKeys(false).contains(name))
			return config.getStringList(name + "." + path).toArray(new String[0]);
		return list;
	}

	public static String msg(String path) {
		FileConfiguration messages = ConfigData.getCD(Main.plugin, "/language", "messages");
		String msg = messages.getString(path);
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack getProfile(Player p) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
		ItemStack profile = Items.PROFILE.a().clone();
		SkullMeta profile_meta = (SkullMeta) profile.getItemMeta();
		profile_meta.setDisplayName(profile_meta.getDisplayName().replace("%name%", p.getName()));
		profile_meta.setOwner(p.getName());
		List<String> profile_lore = profile_meta.getLore();
		for (int j = 0; j < profile_lore.size(); j++) {
			String s = profile_lore.get(j);
			s = s.replace("%claimed-bounties%", "" + config.getInt("claimed-bounties")).replace("%successful-bounties%", "" + config.getInt("successful-bounties")).replace("%current-title%", (config.getString("current-title").equals("") ? "§c" + msg("no-title") : applySpecialChars(config.getString("current-title")))).replace("%level%", "" + config.getInt("level"));
			profile_lore.set(j, s);
		}
		profile_meta.setLore(profile_lore);
		profile.setItemMeta(profile_meta);
		return profile;
	}

	public static void updateLvl(Player p) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
		FileConfiguration levels = ConfigData.getCD(Main.plugin, "", "levels");

		for (int j = 30; j > 0; j--) {
			int ntlu = j * levels.getInt("bounties-needed-to-lvl-up");
			if (config.getInt("claimed-bounties") >= ntlu && config.getInt("level") < j) {
				p.sendMessage(Main.plugin.chatWindow);
				p.sendMessage("§e" + msg("level-up").replace("%level%", "" + j));
				p.sendMessage("§e" + msg("level-up-2").replace("%bounties%", "" + levels.getInt("bounties-needed-to-lvl-up")));

				double money = levels.getDouble("reward.money.base") + (j * levels.getDouble("reward.money.per-lvl"));
				List<String> unlocked = config.getStringList("unlocked");
				List<String> rewards = new ArrayList<String>();

				// title rewards
				for (String title : levels.getConfigurationSection("reward.title").getKeys(false)) {
					int reward_level = 0;
					try {
						reward_level = Integer.parseInt(title);
					} catch (Exception e) {
						continue;
					}
					String reward = levels.getString("reward.title." + title);
					if (j >= reward_level && !unlocked.contains(reward))
						rewards.add(reward);
				}

				// quote rewards
				for (String quote : levels.getConfigurationSection("reward.quote").getKeys(false)) {
					int reward_level = 0;
					try {
						reward_level = Integer.parseInt(quote);
					} catch (Exception e) {
						continue;
					}
					String reward = levels.getString("reward.quote." + quote);
					if (j >= reward_level && !unlocked.contains(reward))
						rewards.add(reward);
				}
				unlocked.addAll(rewards);

				String rewards_format = "";
				if (money > 0)
					rewards_format += "\n§e" + msg("level-up-reward") + money + "§e money";
				for (String s : rewards)
					rewards_format += "\n§e" + msg("level-up-reward") + applySpecialChars(s);
				rewards_format = rewards_format.substring(1);

				Json.json(p, "{\"text\":\"§e" + msg("level-up-rewards") + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + rewards_format + "\",\"color\":\"white\"}]}}}");

				Main.plugin.economy.depositPlayer(p, money);
				config.set("level", j);
				config.set("unlocked", unlocked);
				ConfigData.saveCD(Main.plugin, config, "/userdata", p.getUniqueId().toString());
				break;
			}
		}
	}

	public static ItemStack getLvlAdvancement(Player p) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
		FileConfiguration levels = ConfigData.getCD(Main.plugin, "", "levels");

		ItemStack item = Items.LVL_ADVANCEMENT.a().clone();
		ItemMeta item_meta = item.getItemMeta();
		item_meta.setDisplayName(item_meta.getDisplayName().replace("%name%", p.getName()).replace("%level%", "" + config.getInt("level")));

		String lvl_adv = "§f";
		int ntlu = levels.getInt("bounties-needed-to-lvl-up");
		for (int j = 0; j < ntlu; j++)
			lvl_adv += (config.getInt("claimed-bounties") % ntlu > j ? "§a" + SpecialChar.square : "§f" + SpecialChar.square);

		List<String> item_lore = item_meta.getLore();
		for (int j = 0; j < item_lore.size(); j++) {
			String s = item_lore.get(j);
			s = s.replace("%level%", "" + config.getInt("level")).replace("%lvl-advancement%", lvl_adv);
			item_lore.set(j, s);
		}
		item_meta.setLore(item_lore);
		item.setItemMeta(item_meta);
		return item;
	}

	// ALERTS
	public static void bountyClaimedAlert(Player p, Player t) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		p.sendMessage(Main.plugin.chatWindow);
		p.sendMessage("§e" + msg("bounty-claimed-by-you").replace("%target%", t.getName()).replace("%reward%", format(config.getDouble(t.getName() + ".reward"))));
		for (String h_format : config.getStringList(t.getName() + ".hunters")) {
			Player h = Bukkit.getPlayer(h_format);
			if (h == null)
				continue;
			h.setCompassTarget(h.getWorld().getSpawnLocation());
		}
		FileConfiguration p_config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
		for (Player t1 : Bukkit.getOnlinePlayers()) {
			VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
			if (t1 != p)
				t1.sendMessage("§e" + msg("bounty-claimed").replace("%reward%", format(config.getDouble(t.getName() + ".reward"))).replace("%killer%", (p_config.getString("current-title").equals("") ? "" : "§5[" + applySpecialChars(p_config.getString("current-title")) + "] ") + ChatColor.getLastColors(msg("bounty-claimed").split(Pattern.quote("%killer%"))[0]) + p.getName()).replace("%target%", t.getName()));
		}
	}

	public static void newBountyAlert(Player p, Player t) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		double reward = config.getDouble(t.getName() + ".reward");
		VersionUtils.sound(t, "ENTITY_ENDERMEN_HURT", 1, 0);
		if (p != t)
			t.sendMessage("§c" + msg("new-bounty-on-you").replace("%creator%", p.getName()));
		p.sendMessage(Main.plugin.chatWindow);
		p.sendMessage("§e" + msg("bounty-created").replace("%target%", t.getName()));
		p.sendMessage("§e" + msg("bounty-explain").replace("%reward%", "" + reward));
		for (Player t1 : Bukkit.getOnlinePlayers()) {
			if (t1 != t)
				VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
			if (t1 != t && t1 != p)
				t1.sendMessage("§e" + msg("new-bounty-on-player").replace("%creator%", p.getName()).replace("%target%", t.getName()).replace("%reward%", format(reward)));
		}
	}

	public static void newHunterAlert(Player t, Player h) {
		t.sendMessage(ChatColor.RED + msg("new-hunter-alert").replace("%hunter%", h.getName()));
		VersionUtils.sound(t.getLocation(), "ENTITY_ENDERMEN_HURT", 1, 1);
	}

	public static void uppedBountyAlert(String name, double newReward) {
		for (Player t : Bukkit.getOnlinePlayers())
			t.sendMessage(ChatColor.YELLOW + msg("upped-bounty").replace("%player%", name).replace("%reward%", format(newReward)));
	}

	public static void autoBountyAlert(Player t) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		double reward = config.getDouble(t.getName() + ".reward");
		t.sendMessage("§c" + msg("illegal-kill"));
		VersionUtils.sound(t, "ENTITY_ENDERMEN_HURT", 1, 0);
		for (Player t1 : Bukkit.getOnlinePlayers()) {
			if (t1 != t)
				VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
			if (t1 != t)
				t1.sendMessage("§e" + msg("auto-bounty").replace("%target%", t.getName()).replace("%reward%", format(reward)));
		}
	}

	public static void bountyExpired(String name) {
		for (Player t1 : Bukkit.getOnlinePlayers()) {
			VersionUtils.sound(t1, "ENTITY_VILLAGER_NO", 1, 2);
			t1.sendMessage("§e" + msg("bounty-expired").replace("%target%", name));
		}
	}
}