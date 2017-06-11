package me.Indyuce.bh.title;

import org.bukkit.entity.Player;

public interface Title
{
    public void sendTitle(Player player, String msgTitle, String msgSubTitle, int ticks);
	
    public void sendActionBar(Player player, String message);
}
