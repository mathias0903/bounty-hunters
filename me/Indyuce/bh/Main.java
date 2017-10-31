package me.Indyuce.bh;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.Indyuce.bh.api.Bounty;
import me.Indyuce.bh.api.BountyCreateEvent;
import me.Indyuce.bh.gui.BountiesGUI;
import me.Indyuce.bh.gui.LeaderboardGUI;
import me.Indyuce.bh.json.Json;
import me.Indyuce.bh.json.Json_1_10_R1;
import me.Indyuce.bh.json.Json_1_11_R1;
import me.Indyuce.bh.json.Json_1_12_R1;
import me.Indyuce.bh.json.Json_1_8_R3;
import me.Indyuce.bh.json.Json_1_9_R1;
import me.Indyuce.bh.json.Json_1_9_R2;
import me.Indyuce.bh.ressource.Items;
import me.Indyuce.bh.ressource.MessagesParams;
import me.Indyuce.bh.ressource.QuoteReward;
import me.Indyuce.bh.ressource.TitleReward;
import me.Indyuce.bh.ressource.UserdataParams;
import me.Indyuce.bh.title.Title;
import me.Indyuce.bh.title.Title_1_10_R1;
import me.Indyuce.bh.title.Title_1_11_R1;
import me.Indyuce.bh.title.Title_1_12_R1;
import me.Indyuce.bh.title.Title_1_8_R3;
import me.Indyuce.bh.title.Title_1_9_R1;
import me.Indyuce.bh.title.Title_1_9_R2;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	public Title title;
	public Json json;
	private static Economy economy = null;

	public String chat_window = "§e-----------------------------------------------------";
	public String prefix = "§8[§eBH§8] §7";

	public void onDisable() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage("§8------------------------------------------------------");
		Bukkit.getConsoleSender().sendMessage("§8Enabling " + getName() + " " + getDescription().getVersion() + "...");

		Bukkit.getServer().getPluginManager().registerEvents(new MainListener(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new Utils(this), this);

		Bukkit.getServer().getPluginManager().registerEvents(new BountiesGUI(this), this);
		Bukkit.getServer().getPluginManager().registerEvents(new LeaderboardGUI(this), this);

		if (setupCompatibility()) {
			Bukkit.getConsoleSender().sendMessage("§8Detected Server Version: " + VersionUtils.version);
		} else {
			Bukkit.getConsoleSender().sendMessage(
					"§8Your server version is not compatible. Perhaps compatibility was still not added or your server version is outdated?");
			Bukkit.getConsoleSender().sendMessage("§8------------------------------------------------------");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getConsoleSender().sendMessage("§cPlease install Vault in order to use this plugin!");
			Bukkit.getConsoleSender()
					.sendMessage("§8Disabling " + getName() + " " + getDescription().getVersion() + "...");
			Bukkit.getConsoleSender().sendMessage("§8------------------------------------------------------");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		ConfigData.setupCD(this, "", "levels");
		ConfigData.setupCD(this, "", "data");
		ConfigData.setupCD(this, "/language", "messages");
		ConfigData.setupCD(this, "/language", "items");
		FileConfiguration messages = ConfigData.getCD(this, "/language", "messages");
		FileConfiguration items = ConfigData.getCD(this, "/language", "items");
		FileConfiguration levels = ConfigData.getCD(this, "", "levels");

		getConfig().options().copyDefaults(true);
		saveConfig();

		if (levels.getConfigurationSection("").getKeys(false).isEmpty()) {
			for (TitleReward title : TitleReward.values()) {
				levels.set("reward.title." + title.level, title.title);
			}
			for (QuoteReward quote : QuoteReward.values()) {
				levels.set("reward.quote." + quote.level, quote.quote);
			}
			levels.set("bounties-needed-to-lvl-up", 5);
			levels.set("reward.money.base", 50);
			levels.set("reward.money.per-lvl", 6);
		}
		for (MessagesParams pa : MessagesParams.values()) {
			String path = pa.name().toLowerCase().replace("_", "-");
			if (!messages.getConfigurationSection("").getKeys(false).contains(path)) {
				messages.set(path, pa.value);
			}
		}
		for (Items i : Items.values()) {
			if (!items.getConfigurationSection("").getKeys(false).contains(i.name())) {
				items.set(i.name() + ".name", i.a().getItemMeta().getDisplayName());
				items.set(i.name() + ".lore", i.a().getItemMeta().getLore());
			}
		}
		for (Player p : Bukkit.getOnlinePlayers()) {
			File file = new File(this.getDataFolder() + "/userdata", p.getUniqueId().toString() + ".yml");
			if (file.exists()) {
				continue;
			}
			ConfigData.setupCD(this, "/userdata", p.getUniqueId().toString());
			FileConfiguration pconfig = ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());
			for (UserdataParams pa : UserdataParams.values()) {
				String path = pa.name().toLowerCase().replace("_", "-");
				if (!pconfig.getKeys(false).contains(path)) {
					pconfig.set(path, pa.value);
				}
			}
			ConfigData.saveCD(this, pconfig, "/userdata", p.getUniqueId().toString());
		}
		ConfigData.saveCD(this, items, "/language", "items");
		ConfigData.saveCD(this, messages, "/language", "messages");
		ConfigData.saveCD(this, levels, "", "levels");

		Bukkit.getConsoleSender()
				.sendMessage("§8" + getName() + " " + getDescription().getVersion() + " has been enabled!");
		Bukkit.getConsoleSender().sendMessage("§8------------------------------------------------------");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		
		// world blacklist
		if (getConfig().getStringList("world-blacklist").contains(p.getWorld().getName())) {
			return false;
		}
		// ====================================================================================================================================
		if (cmd.getName().equalsIgnoreCase("addbounty")) {
			if (!p.hasPermission("bountyhunters.add")) {
				p.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return false;
			}
			if (args.length < 2) {
				p.sendMessage("§c" + Utils.msg("command-usage").replace("%command%", "/bounty <player> <reward>"));
				return false;
			}
			FileConfiguration config = ConfigData.getCD(this, "", "data");
			Player t = Bukkit.getPlayer(args[0]);
			if (t == null) {
				p.sendMessage("§c" + Utils.msg("error-player"));
				return false;
			}
			if (!t.isOnline()) {
				p.sendMessage("§c" + Utils.msg("error-player"));
				return false;
			}
			if (config.getConfigurationSection("").getKeys(false).contains(t.getName())) {
				p.sendMessage("§c" + Utils.msg("already-bounty-on-player"));
				return false;
			}
			if (t.getName().equals(p.getName())) {
				p.sendMessage("§c" + Utils.msg("cant-set-bounty-on-yourself"));
				return false;
			}
			if (t.hasPermission("bountyhunters.imun") && !p.hasPermission("bountyhunters.bypass-imun")) {
				p.sendMessage("§c" + Utils.msg("bounty-imun"));
				return false;
			}
			double reward = 0;
			try {
				reward = Integer.parseInt(args[1]);
			} catch (Exception e) {
				p.sendMessage("§c" + Utils.msg("not-valid-number").replace("%arg%", args[1]));
				return false;
			}

			int min = (getConfig().getInt("min-reward") > 0 ? getConfig().getInt("min-reward") : 0);
			int max = getConfig().getInt("max-reward");
			if ((reward < min) || (max > 0 && reward > max)) {
				p.sendMessage("§c" + Utils.msg("wrong-reward").replace("%max%", "" + max).replace("%min%", "" + min));
				return false;
			}

			double tax = reward * (getConfig().getDouble("tax") / 100);
			tax = Utils.tronc(tax, 3);
			int restriction = getConfig().getInt("bounty-set-restriction") * 1000;
			if (MainListener.last_bounty.containsKey(p)) {
				long last = MainListener.last_bounty.get(p);
				if (last + restriction > System.currentTimeMillis()) {
					p.sendMessage(
							"§c" + Utils.msg("bounty-set-restriction").replace("%time%", "" + restriction / 1000));
					return false;
				}
			}
			if (!setupEconomy()) {
				p.sendMessage("§cAn error occured. Please try again later.");
				return false;
			}
			if (!economy.has(p, reward)) {
				p.sendMessage("§c" + Utils.msg("not-enough-money"));
				return false;
			}

			// API
			BountyCreateEvent e = new BountyCreateEvent(new Bounty(p, t, reward));
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				return false;
			}
			reward = e.getBounty().getReward();

			economy.withdrawPlayer(p, reward);
			reward -= (tax > reward ? reward : tax);
			MainListener.last_bounty.put(p.getUniqueId(), System.currentTimeMillis());
			config.set(t.getName() + ".creator", p.getName());
			config.set(t.getName() + ".reward", reward);
			config.set(t.getName() + ".hunters", new String[] {});
			ConfigData.saveCD(this, config, "", "data");

			Utils.newBountyAlert(p, t);
			if (tax > 0) {
				p.sendMessage("§c" + Utils.msg("tax-explain").replace("%percent%", "" + getConfig().getDouble("tax"))
						.replace("%price%", "" + tax));
			}
		}
		// ====================================================================================================================================
		if (cmd.getName().equalsIgnoreCase("bounties")) {
			Utils.updateLvl(p);
			if (!p.hasPermission("bountyhunters.gui")) {
				p.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return false;
			}
			if (args.length < 1) {
				BountiesGUI.openInv(p, 1);
				return false;
			} else if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("lb")) {
				if (!p.hasPermission("bountyhunters.leaderboard-gui")) {
					p.sendMessage("§c" + Utils.msg("not-enough-perms"));
					return false;
				}
				LeaderboardGUI.openInv(p);
			} else if (args[0].equalsIgnoreCase("title")) {
				if (!p.hasPermission("bountyhunters.title-cmd")) {
					p.sendMessage("§c" + Utils.msg("not-enough-perms"));
					return false;
				}
				if (args.length < 2) {
					return false;
				}
				int index = 0;
				try {
					index = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					return false;
				}

				FileConfiguration config = ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());
				String select = config.getStringList("unlocked").get(index);
				config.set("current-title", select);
				ConfigData.saveCD(this, config, "/userdata", p.getUniqueId().toString());
				VersionUtils.sound(p, "ENTITY_PLAYER_LEVELUP", 1, 2);
				p.sendMessage(
						"§e" + Utils.msg("successfully-selected").replace("%item%", Utils.applySpecialChars(select)));
			} else if (args[0].equalsIgnoreCase("quote")) {
				if (!p.hasPermission("bountyhunters.quote-cmd")) {
					p.sendMessage("§c" + Utils.msg("not-enough-perms"));
					return false;
				}
				if (args.length < 2) {
					return false;
				}
				int index = 0;
				try {
					index = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					return false;
				}

				FileConfiguration config = ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());
				String select = config.getStringList("unlocked").get(index);
				config.set("current-quote", select);
				ConfigData.saveCD(this, config, "/userdata", p.getUniqueId().toString());
				VersionUtils.sound(p, "ENTITY_PLAYER_LEVELUP", 1, 2);
				p.sendMessage("§e" + Utils.msg("successfully-selected").replace("%item%", select));
			} else if (args[0].equalsIgnoreCase("titles")) {
				if (!p.hasPermission("bountyhunters.title-cmd")) {
					p.sendMessage("§c" + Utils.msg("not-enough-perms"));
					return false;
				}

				p.sendMessage(chat_window);
				p.sendMessage("§e" + Utils.msg("unlocked-titles"));
				FileConfiguration levels = ConfigData.getCD(this, "", "levels");
				FileConfiguration config = ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());

				List<String> unlocked = config.getStringList("unlocked");
				for (String s : levels.getConfigurationSection("reward.title").getKeys(false)) {
					String title = levels.getString("reward.title." + s);
					if (unlocked.contains(title)) {
						json.sendMsg((Player) sender,
								"{\"text\":\"* §a" + Utils.applySpecialChars(title)
										+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bounties title "
										+ unlocked.indexOf(title)
										+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
										+ Utils.msg("click-select") + "\",\"color\":\"white\"}]}}}");
					}
				}
			} else if (args[0].equalsIgnoreCase("quotes")) {
				if (!p.hasPermission("bountyhunters.quote-cmd")) {
					p.sendMessage("§c" + Utils.msg("not-enough-perms"));
					return false;
				}

				p.sendMessage(chat_window);
				p.sendMessage("§e" + Utils.msg("unlocked-quotes"));
				FileConfiguration levels = ConfigData.getCD(this, "", "levels");
				FileConfiguration config = ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());

				List<String> unlocked = config.getStringList("unlocked");
				for (String s : levels.getConfigurationSection("reward.quote").getKeys(false)) {
					String title = levels.getString("reward.quote." + s);
					if (unlocked.contains(title)) {
						json.sendMsg((Player) sender,
								"{\"text\":\"* §a" + title
										+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bounties quote "
										+ unlocked.indexOf(title)
										+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
										+ Utils.msg("click-select") + "\",\"color\":\"white\"}]}}}");
					}
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!p.hasPermission("bountyhunters.op")) {
					p.sendMessage("§c" + Utils.msg("not-enough-perms"));
					return false;
				}
				reloadConfig();
				p.sendMessage(prefix + "Configuration file reloaded.");
			}
		}
		// ====================================================================================================================================
		return false;
	}

	public Economy getEco() {
		return economy;
	}

	public boolean setupEco() {
		return setupEconomy();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}

	private boolean setupCompatibility() {
		String version1;
		try {
			version1 = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
			return false;
		}
		if (version1.equals("v1_8_R3")) {
			title = new Title_1_8_R3();
			json = new Json_1_8_R3();
		} else if (version1.equals("v1_9_R1")) {
			title = new Title_1_9_R1();
			json = new Json_1_9_R1();
		} else if (version1.equals("v1_9_R2")) {
			title = new Title_1_9_R2();
			json = new Json_1_9_R2();
		} else if (version1.equals("v1_10_R1")) {
			title = new Title_1_10_R1();
			json = new Json_1_10_R1();
		} else if (version1.equals("v1_11_R1")) {
			title = new Title_1_11_R1();
			json = new Json_1_11_R1();
		} else if (version1.equals("v1_12_R1")) {
			title = new Title_1_12_R1();
			json = new Json_1_12_R1();
		}
		VersionUtils.version = version1;
		return title != null;
	}
}