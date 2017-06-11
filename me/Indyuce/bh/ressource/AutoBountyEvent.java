package me.Indyuce.bh.ressource;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AutoBountyEvent extends Event implements Cancellable
{
	private Player victim;
	private Bounty bounty;
	private boolean cancelled;
	
	public AutoBountyEvent(Bounty bounty, Player victim) {
		this.bounty = bounty;
		this.victim = victim;
	}
	public boolean isCancelled() {
		return cancelled;
	}
	public void setCancelled(boolean bool) {
		cancelled = bool;
	}
	public Player getVictim() {
		return victim;
	}
	public Bounty getBounty() {
		return bounty;
	}
	@Override
	public HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return null;
	}
}
