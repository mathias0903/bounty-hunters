package me.Indyuce.bh;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.Indyuce.bh.command.AddBountyCommand;
import me.Indyuce.bh.command.BountiesCommand;
import me.Indyuce.bh.gui.BountiesGUI;
import me.Indyuce.bh.gui.LeaderboardGUI;
import me.Indyuce.bh.resource.Items;
import me.Indyuce.bh.resource.MessagesParams;
import me.Indyuce.bh.resource.QuoteReward;
import me.Indyuce.bh.resource.TitleReward;
import me.Indyuce.bh.resource.UserdataParams;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	public HashMap<UUID, Long> lastBounty = new HashMap<UUID, Long>();
	public Economy economy;

	public static Main plugin;
	public String chatWindow = "§e-----------------------------------------------------";
	public String prefix = "§8[§eBH§8] §7";

	public void onDisable() {
		for (Player t : Bukkit.getOnlinePlayers())
			t.closeInventory();
	}

	public void onEnable() {
		plugin = this;

		// listeners
		Bukkit.getServer().getPluginManager().registerEvents(new MainListener(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new Utils(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new BountiesGUI(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new LeaderboardGUI(), this);

		// version compatibility + vault
		try {
			VersionUtils.version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			Bukkit.getConsoleSender().sendMessage("[BountyHunters] " + ChatColor.DARK_GRAY + "Detected Server Version: " + VersionUtils.version);
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage("[BountyHunters] " + ChatColor.RED + "Your server version is not compatible.");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			Bukkit.getConsoleSender().sendMessage("[BountyHunters] " + ChatColor.RED + "Please install Vault in order to use this plugin!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null)
			economy = economyProvider.getProvider();
		else {
			Bukkit.getConsoleSender().sendMessage("[BountyHunters] " + ChatColor.RED + "Couldn't load Vault. Disabling...");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		// config files
		ConfigData.setupCD(this, "", "levels");
		ConfigData.setupCD(this, "", "data");
		ConfigData.setupCD(this, "/language", "messages");
		ConfigData.setupCD(this, "/language", "items");
		FileConfiguration messages = ConfigData.getCD(this, "/language", "messages");
		FileConfiguration items = ConfigData.getCD(this, "/language", "items");
		FileConfiguration levels = ConfigData.getCD(this, "", "levels");

		saveDefaultConfig();

		if (levels.getConfigurationSection("").getKeys(false).isEmpty()) {
			for (TitleReward title : TitleReward.values())
				levels.set("reward.title." + title.level, title.title);
			for (QuoteReward quote : QuoteReward.values())
				levels.set("reward.quote." + quote.level, quote.quote);
			levels.set("bounties-needed-to-lvl-up", 5);
			levels.set("reward.money.base", 50);
			levels.set("reward.money.per-lvl", 6);
		}
		for (MessagesParams pa : MessagesParams.values()) {
			String path = pa.name().toLowerCase().replace("_", "-");
			if (!messages.getConfigurationSection("").getKeys(false).contains(path))
				messages.set(path, pa.value);
		}
		for (Items i : Items.values())
			if (!items.getConfigurationSection("").getKeys(false).contains(i.name())) {
				items.set(i.name() + ".name", i.a().getItemMeta().getDisplayName());
				items.set(i.name() + ".lore", i.a().getItemMeta().getLore());
			}
		for (Player p : Bukkit.getOnlinePlayers()) {
			File file = new File(this.getDataFolder() + "/userdata", p.getUniqueId().toString() + ".yml");
			if (file.exists())
				continue;
			ConfigData.setupCD(this, "/userdata", p.getUniqueId().toString());
			FileConfiguration pconfig = ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());
			for (UserdataParams pa : UserdataParams.values()) {
				String path = pa.name().toLowerCase().replace("_", "-");
				if (!pconfig.getKeys(false).contains(path))
					pconfig.set(path, pa.value);
			}
			ConfigData.saveCD(this, pconfig, "/userdata", p.getUniqueId().toString());
		}
		ConfigData.saveCD(this, items, "/language", "items");
		ConfigData.saveCD(this, messages, "/language", "messages");
		ConfigData.saveCD(this, levels, "", "levels");

		// commands
		getCommand("addbounty").setExecutor(new AddBountyCommand());
		getCommand("bounties").setExecutor(new BountiesCommand());
	}

	public boolean checkPl(CommandSender sender, boolean msg) {
		boolean b = sender instanceof Player;
		if (!b && msg)
			sender.sendMessage("§cThis command is for players only.");
		return b;
	}
}