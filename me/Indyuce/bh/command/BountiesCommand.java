package me.Indyuce.bh.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Indyuce.bh.ConfigData;
import me.Indyuce.bh.Main;
import me.Indyuce.bh.Utils;
import me.Indyuce.bh.VersionUtils;
import me.Indyuce.bh.gui.BountiesGUI;
import me.Indyuce.bh.gui.LeaderboardGUI;
import me.Indyuce.bh.reflect.Json;

public class BountiesCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String commandLabel, String[] args) {
		if (sender instanceof Player)
			Utils.updateLvl((Player) sender);
		if (!sender.hasPermission("bountyhunters.gui")) {
			sender.sendMessage("§c" + Utils.msg("not-enough-perms"));
			return true;
		}
		if (args.length < 1) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			BountiesGUI.openInv((Player) sender, 1);
			return true;
		} else if (args[0].equalsIgnoreCase("leaderboard") || args[0].equalsIgnoreCase("lb")) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			if (!sender.hasPermission("bountyhunters.leaderboard-gui")) {
				sender.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return true;
			}
			LeaderboardGUI.openInv((Player) sender);
		} else if (args[0].equalsIgnoreCase("compass")) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			Player p = (Player) sender;
			p.setCompassTarget((p.getBedSpawnLocation() == null ? p.getWorld().getSpawnLocation() : p.getBedSpawnLocation()));
			p.sendMessage(ChatColor.YELLOW + Utils.msg("tracking-compass-reset"));
		} else if (args[0].equalsIgnoreCase("title")) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			Player p = (Player) sender;
			if (!sender.hasPermission("bountyhunters.title-cmd")) {
				sender.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return true;
			}
			if (args.length < 2)
				return true;
			int index = 0;
			try {
				index = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				return true;
			}

			FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
			String select = config.getStringList("unlocked").get(index);
			config.set("current-title", select);
			ConfigData.saveCD(Main.plugin, config, "/userdata", p.getUniqueId().toString());
			VersionUtils.sound(p, "ENTITY_PLAYER_LEVELUP", 1, 2);
			p.sendMessage(ChatColor.YELLOW + Utils.msg("successfully-selected").replace("%item%", Utils.applySpecialChars(select)));
		} else if (args[0].equalsIgnoreCase("quote")) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			Player p = (Player) sender;
			if (!p.hasPermission("bountyhunters.quote-cmd")) {
				p.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return true;
			}
			if (args.length < 2)
				return true;
			int index = 0;
			try {
				index = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				return true;
			}

			FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());
			String select = config.getStringList("unlocked").get(index);
			config.set("current-quote", select);
			ConfigData.saveCD(Main.plugin, config, "/userdata", p.getUniqueId().toString());
			VersionUtils.sound(p, "ENTITY_PLAYER_LEVELUP", 1, 2);
			p.sendMessage(ChatColor.YELLOW + Utils.msg("successfully-selected").replace("%item%", select));
		} else if (args[0].equalsIgnoreCase("titles")) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			Player p = (Player) sender;
			if (!p.hasPermission("bountyhunters.title-cmd")) {
				p.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return true;
			}

			p.sendMessage(Utils.msg("chat-bar"));
			p.sendMessage(ChatColor.YELLOW + Utils.msg("unlocked-titles"));
			FileConfiguration levels = ConfigData.getCD(Main.plugin, "", "levels");
			FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());

			List<String> unlocked = config.getStringList("unlocked");
			for (String s : levels.getConfigurationSection("reward.title").getKeys(false)) {
				String title = levels.getString("reward.title." + s);
				if (unlocked.contains(title)) {
					Json.json((Player) sender, "{\"text\":\"* §a" + Utils.applySpecialChars(title) + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bounties title " + unlocked.indexOf(title) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + Utils.msg("click-select") + "\",\"color\":\"white\"}]}}}");
				}
			}
		} else if (args[0].equalsIgnoreCase("quotes")) {
			if (!Main.plugin.checkPl(sender, true))
				return true;
			Player p = (Player) sender;
			if (!p.hasPermission("bountyhunters.quote-cmd")) {
				p.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return true;
			}

			p.sendMessage(Utils.msg("chat-bar"));
			p.sendMessage(ChatColor.YELLOW + Utils.msg("unlocked-quotes"));
			FileConfiguration levels = ConfigData.getCD(Main.plugin, "", "levels");
			FileConfiguration config = ConfigData.getCD(Main.plugin, "/userdata", p.getUniqueId().toString());

			List<String> unlocked = config.getStringList("unlocked");
			for (String s : levels.getConfigurationSection("reward.quote").getKeys(false)) {
				String title = levels.getString("reward.quote." + s);
				if (unlocked.contains(title)) {
					Json.json((Player) sender, "{\"text\":\"* §a" + title + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/bounties quote " + unlocked.indexOf(title) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + Utils.msg("click-select") + "\",\"color\":\"white\"}]}}}");
				}
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("bountyhunters.op")) {
				sender.sendMessage("§c" + Utils.msg("not-enough-perms"));
				return true;
			}
			Main.plugin.reloadConfig();
			sender.sendMessage(ChatColor.YELLOW + "Configuration file reloaded.");
		}

		return false;
	}

}
