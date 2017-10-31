package me.Indyuce.bh.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BountyCreateEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Bounty bounty;
	private boolean cancelled;

	public BountyCreateEvent(Bounty bounty) {
		this.bounty = bounty;
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

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
