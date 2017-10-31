package me.Indyuce.bh.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import me.Indyuce.bh.ConfigData;
import me.Indyuce.bh.MainListener;

@SuppressWarnings("deprecation")
public class BHUtils {
	public static List<Bounty> getActiveBounties() {
		List<Bounty> list = new ArrayList<Bounty>();

		FileConfiguration data = ConfigData.getCD(MainListener.plugin, "", "data");
		for (String s : data.getKeys(false)) {
			OfflinePlayer target = Bukkit.getOfflinePlayer(s);
			OfflinePlayer creator = Bukkit.getOfflinePlayer(data.getString(s + ".creator"));
			int reward = data.getInt(s + ".reward");
			List<OfflinePlayer> trackers = new ArrayList<OfflinePlayer>();
			for (String s1 : data.getStringList(s + ".hunters")) {
				OfflinePlayer t = Bukkit.getOfflinePlayer(s1);
				if (t != null) {
					trackers.add(t);
				}
			}

			list.add(new Bounty(creator, target, reward, trackers));
		}

		return list;
	}

	public Bounty getBounty(OfflinePlayer target) {
		FileConfiguration data = ConfigData.getCD(MainListener.plugin, "", "data");
		if (!data.contains(target.getName())) {
			return null;
		}
		
		String creator = data.getString(target.getName() + ".creator");
		int reward = data.getInt(target.getName() + ".reward");
		List<OfflinePlayer> trackers = new ArrayList<OfflinePlayer>();
		for (String s1 : data.getStringList(target.getName() + ".hunters")) {
			OfflinePlayer t = Bukkit.getOfflinePlayer(s1);
			if (t != null) {
				trackers.add(t);
			}
		}
		
		return new Bounty(Bukkit.getOfflinePlayer(creator), target, reward, trackers);
	}
}
