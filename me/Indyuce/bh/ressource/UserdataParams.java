package me.Indyuce.bh.ressource;

public enum UserdataParams
{
	CLAIMED_BOUNTIES(0),
	SUCCESSFUL_BOUNTIES(0),
  	;
  
	public Object value;
	
	private UserdataParams(Object value)
	{
	    this.value=value;
	}
}