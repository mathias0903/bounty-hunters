package me.Indyuce.bh.api;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.Indyuce.bh.resource.BountyCause;

public class BountyCreateEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Bounty bounty;
	private boolean cancelled;
	private BountyCause cause;

	public BountyCreateEvent(Bounty bounty, BountyCause cause) {
		this.bounty = bounty;
		this.cause = cause;
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

	public BountyCause getCause() {
		return cause;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
