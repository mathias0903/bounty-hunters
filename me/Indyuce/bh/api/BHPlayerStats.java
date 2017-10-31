package me.Indyuce.bh.api;

import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import me.Indyuce.bh.ConfigData;
import me.Indyuce.bh.MainListener;

public class BHPlayerStats {
	private UUID uuid;
	
	public BHPlayerStats(UUID uuid) {
		this.uuid = uuid;
	}
	
	private FileConfiguration getUserdataFile() {
		return ConfigData.getCD(MainListener.plugin, "/userdata", uuid.toString());
	}
	
	public int getClaimedBounties() {
		return getUserdataFile().getInt("claimed-bounties");
	}
	
	public int getSuccessfulBounties() {
		return getUserdataFile().getInt("successful-bounties");
	}
	
	public int getLevel() {
		return getUserdataFile().getInt("level");
	}
	
	public String getCurrentTitle() {
		return getUserdataFile().getString("current-title");
	}
	
	public String getCurrentQuote() {
		return getUserdataFile().getString("current-quote");
	}
	
	public List<String> getUnlocked() {
		return getUserdataFile().getStringList("unlocked");
	}
}
