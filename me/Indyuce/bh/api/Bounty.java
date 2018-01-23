package me.Indyuce.bh.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

public class Bounty {
	private double reward;
	private OfflinePlayer creator;
	private OfflinePlayer target;
	private List<OfflinePlayer> trackers = new ArrayList<OfflinePlayer>();

	public Bounty(OfflinePlayer creator, OfflinePlayer target, double reward) {
		this.creator = creator;
		this.target = target;
		this.reward = reward;
	}

	public Bounty(OfflinePlayer creator, OfflinePlayer target, double reward, List<OfflinePlayer> trackers) {
		this.creator = creator;
		this.target = target;
		this.reward = reward;
		this.trackers = trackers;
	}

	public double getReward() {
		return reward;
	}

	public boolean hasCreator() {
		return creator != null;
	}

	public OfflinePlayer getCreator() {
		return creator;
	}

	public OfflinePlayer getTarget() {
		return target;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

	public boolean isAutoBounty() {
		return creator == null;
	}

	public List<OfflinePlayer> getHuntingPlayers() {
		return trackers;
	}

	public Bounty clone() {
		return new Bounty(creator, target, reward, trackers);
	}
}
