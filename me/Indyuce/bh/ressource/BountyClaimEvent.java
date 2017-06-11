package me.Indyuce.bh.ressource;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BountyClaimEvent extends Event implements Cancellable
{
	private Player claimer;
	private Bounty bounty;
	private boolean cancelled;
	
	public BountyClaimEvent(Bounty bounty, Player claimer) {
		this.bounty = bounty;
		this.claimer = claimer;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean bool) {
		cancelled = bool;
	}
	public Bounty getBounty() {
		return bounty;
	}
	public Player getClaimer() {
		return claimer;
	}
	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return null;
	}
}
