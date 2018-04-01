package me.Indyuce.bh.resource;

public enum QuoteReward {
	DEATH("May Death welcome you...", 3),
	STEAL("You better not steal that diamond again.", 6),
	MATTER("It's a matter of gold.", 10),;

	public String quote;
	public int level;

	private QuoteReward(String quote, int level) {
		this.quote = quote;
		this.level = level;
	}
}