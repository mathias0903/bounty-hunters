package me.Indyuce.bh.resource;

public enum UserdataParams {
	CLAIMED_BOUNTIES(0),
	SUCCESSFUL_BOUNTIES(0),
	LEVEL(0),
	CURRENT_TITLE(""),
	CURRENT_QUOTE(""),
	UNLOCKED(new String[] {}),;

	public Object value;

	private UserdataParams(Object value) {
		this.value = value;
	}
}