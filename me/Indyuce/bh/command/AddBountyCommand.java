package me.Indyuce.bh.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.Indyuce.bh.ConfigData;
import me.Indyuce.bh.Main;
import me.Indyuce.bh.Utils;
import me.Indyuce.bh.api.Bounty;
import me.Indyuce.bh.api.BountyCreateEvent;

public class AddBountyCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("bountyhunters.add")) {
			sender.sendMessage("§c" + Utils.msg("not-enough-perms"));
			return true;
		}
		if (args.length < 2) {
			sender.sendMessage("§c" + Utils.msg("command-usage").replace("%command%", "/bounty <player> <reward>"));
			return true;
		}
		if (sender instanceof Player)
			if (Main.plugin.getConfig().getStringList("world-blacklist").contains(((Player) sender).getWorld().getName()))
				return true;

		// basic checks
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		Player t = Bukkit.getPlayer(args[0]);
		if (t == null) {
			sender.sendMessage("§c" + Utils.msg("error-player"));
			return true;
		}
		if (!t.isOnline()) {
			sender.sendMessage("§c" + Utils.msg("error-player"));
			return true;
		}
		if (sender instanceof Player)
			if (t.getName().equals(((Player) sender).getName())) {
				sender.sendMessage("§c" + Utils.msg("cant-set-bounty-on-yourself"));
				return true;
			}

		// permission
		if (t.hasPermission("bountyhunters.imun") && !sender.hasPermission("bountyhunters.bypass-imun")) {
			sender.sendMessage("§c" + Utils.msg("bounty-imun"));
			return true;
		}

		// reward
		double reward = 0;
		try {
			reward = Integer.parseInt(args[1]);
		} catch (Exception e) {
			sender.sendMessage("§c" + Utils.msg("not-valid-number").replace("%arg%", args[1]));
			return true;
		}

		// min/max check
		double min = (Main.plugin.getConfig().getDouble("min-reward") > 0 ? Main.plugin.getConfig().getDouble("min-reward") : 0);
		double max = Main.plugin.getConfig().getDouble("max-reward");
		if ((reward < min) || (max > 0 && reward > max)) {
			sender.sendMessage("§c" + Utils.msg("wrong-reward").replace("%max%", Utils.format(max)).replace("%min%", Utils.format(min)));
			return true;
		}

		// tax calculation
		double tax = reward * (Main.plugin.getConfig().getDouble("tax") / 100);
		tax = Utils.tronc(tax, 3);

		// set restriction
		if (sender instanceof Player) {
			int restriction = Main.plugin.getConfig().getInt("bounty-set-restriction") * 1000;
			if (Main.plugin.lastBounty.containsKey(((Player) sender).getUniqueId())) {
				long last = Main.plugin.lastBounty.get(((Player) sender).getUniqueId());
				if (last + restriction > System.currentTimeMillis()) {
					sender.sendMessage("§c" + Utils.msg("bounty-set-restriction").replace("%time%", "" + restriction / 1000));
					return true;
				}
			}
		}

		// money restriction
		if (sender instanceof Player)
			if (!Main.plugin.economy.has((Player) sender, reward)) {
				sender.sendMessage("§c" + Utils.msg("not-enough-money"));
				return true;
			}

		// API
		BountyCreateEvent e = new BountyCreateEvent(new Bounty((sender instanceof Player ? (Player) sender : null), t, reward));
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled())
			return true;
		reward = e.getBounty().getReward();

		// add more reward to bounty if existing
		final boolean newBounty = !config.contains(t.getName());
		if (!newBounty)
			if (!Main.plugin.getConfig().getBoolean("add-reward-to-bounty-when-existing-bounty")) {
				sender.sendMessage("§c" + Utils.msg("already-bounty-on-player"));
				return true;
			}

		if (sender instanceof Player) {
			Main.plugin.economy.withdrawPlayer((Player) sender, reward);
			Main.plugin.lastBounty.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
		}
		reward -= (tax > reward ? reward : tax);

		// new bounty / add
		if (newBounty) {
			config.set(t.getName() + ".creator", (sender instanceof Player ? ((Player) sender).getName() : null));
			config.set(t.getName() + ".hunters", new String[] {});
			config.createSection(t.getName() + ".up");
			config.set(t.getName() + ".reward", reward);
		} else {
			config.set(t.getName() + ".reward", reward + config.getDouble(t.getName() + ".reward"));
			if (sender instanceof Player)
				config.set(t.getName() + ".up." + ((Player) sender).getName(), reward);
		}

		ConfigData.saveCD(Main.plugin, config, "", "data");

		// message
		if (!newBounty)
			for (Player ent : Bukkit.getOnlinePlayers())
				ent.sendMessage(Utils.msg("upped-bounty").replace("%player%", t.getName()).replace("%reward%", Utils.format(config.getDouble(t.getName() + ".reward"))));
		else if (sender instanceof Player)
			Utils.newBountyAlert((Player) sender, t);
		else
			Utils.autoBountyAlert(t);
		if (tax > 0)
			sender.sendMessage("§c" + Utils.msg("tax-explain").replace("%percent%", "" + Main.plugin.getConfig().getDouble("tax")).replace("%price%", "" + Utils.format(tax)));
		return true;
	}
}
