package me.Indyuce.bh.ressource;

public enum MessagesParams {
	SET_BY("Set by &f%creator%&7."),
	SET_BY_YOURSELF("You set this bounty."),
	REWARD_IS("The reward is &f%reward%&7."),
	KILL_HIM_CLAIM_BOUNTY("Kill him to claim the bounty!"),
	DONT_LET_THEM_KILL_U("Don't let them kill you."),
	THUG_PLAYER("This player is a thug!"),
	RIGHT_CLICK_REMOVE_BOUNTY("Right click to remove this bounty."),
	CLICK_BUY_COMPASS("Click to buy the compass for %price%."),
	CLICK_TARGET("Click to target him."),
	CLICK_UNTARGET("Click to untarget him."),
	CURRENT_HUNTERS("There are §f%hunters% §7players tracking him."),
	
	GUI_NAME("Bounties"),
	
	LEADERBOARD_GUI_NAME("Hunter Leaderboard"),
	LEADERBOARD_GUI_COMPLETED_BOUNTIES("This player has completed &f%bounties% &7bounties."),
	LEADERBOARD_GUI_TITLE("- &a#title# &7-"),
	LEADERBOARD_GUI_LEVEL("This player is level &f#level#&7."),
	
	ERROR_PLAYER("That player doesn't exist or isn't online."),
	TARGET_DISCONNECT("Your target disconnected."),
	NEW_HUNTER_ALERT("%hunter% now targets you."),
	EMPTY_INV_FIRST("Please empty your inventory first."),
	IN_ANOTHER_WORLD("IN ANOTHER WORLD"),
	NOT_ENOUGH_PERMS("You don't have enough permissions!"),
	ALREADY_BOUNTY_ON_PLAYER("There is already a bounty on that player."),
	COMMAND_USAGE("Usage: %command%"),
	NOT_VALID_NUMBER("&f%arg% &eis not a valid number!"),
	BOUNTY_CREATED("You succesfully set a bounty on &f%target%&e."),
	BOUNTY_EXPLAIN("The first to kill this player will receive &f%reward%&e$."),
	BOUNTY_IMUN("You can't set a bounty on this player."),
	TRACK_IMUN("You can't track this player."),
	CANT_SET_BOUNTY_ON_YOURSELF("You can't set a bounty on yourself!"),
	NEW_BOUNTY_ON_YOU("&f%creator% &ejust set a bounty on you!"),
	NEW_BOUNTY_ON_PLAYER("&f%creator% &eset a bounty on &f%target%&e! Kill him to get &f%reward%&e$!"),
	BOUNTY_EXPIRED("The bounty on &f%target% &ehas expired."),
	WRONG_REWARD("Reward must be between §f%min% §cand §f%max%§c!"),
	NOT_ENOUGH_MONEY("You don't have enough money."),
	BOUNTY_CLAIMED("&f%killer% &eclaimed &f%target%&e's bounty: &f%reward%&e$!"),
	BOUNTY_CLAIMED_BY_YOU("You succesfully claimed &f%target%&e's bounty: &f%reward%&e$!"),
	AUTO_BOUNTY("&f%target%&e killed a player illegaly: a bounty was set on him! Kill him to get &f%reward%&e$!"),
	ILLEGAL_KILL("You killed a man illegaly! A bounty was set on you!"),
	BOUNTY_SET_RESTRICTION("You must wait &f%time% &cseconds between each bounty."),
	TARGET_SET("Target set."),
	TARGET_REMOVED("Target removed."),
	BOUGHT_COMPASS("You Succesfully bought a Tracking Compass."),
	TAX_EXPLAIN("§f%percent%§e% of the reward (§f%price%§e) were taken as tax."),
	PLAYER_MUST_BE_CONNECTED("The player must be connected in order to be tracked."),
  	;
  
	public Object value;
	
	private MessagesParams(Object value) {
	    this.value = value;
	}
}