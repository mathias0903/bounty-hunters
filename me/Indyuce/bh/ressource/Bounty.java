package me.Indyuce.bh.ressource;

import org.bukkit.OfflinePlayer;

public class Bounty {
	private int reward;
	private OfflinePlayer creator;
	private OfflinePlayer target;
	
	public Bounty(OfflinePlayer creator, OfflinePlayer target, int reward) {
		this.creator = creator;
		this.target = target;
		this.reward = reward;
	}
	public int getReward() {
		return reward;
	}
	public OfflinePlayer getCreator() {
		return creator;
	}
	public OfflinePlayer getTarget() {
		return target;
	}
}
