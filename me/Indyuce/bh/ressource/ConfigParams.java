package me.Indyuce.bh.ressource;

public enum ConfigParams
{
	BOUNTY_EFFECT(true),
	AUTO_BOUNTY(false),
	AUTO_BOUNTY_REWARD(100),
	BOUNTY_SET_RESTRICTION(120),
	DROP_HEAD(false),
	NEW_HUNTER_ALERT(false),
	DISABLE_COMPASS(false),
	COMPASS_PRICE(1000),
	MIN_REWARD(0),
	MAX_REWARD(0),
	TAX(15),
	ROUND_DISTANCE(false),
  	;
	
	public Object value;
	
	private ConfigParams(Object value)
	{
	    this.value=value;
	}
}