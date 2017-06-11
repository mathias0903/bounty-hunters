package me.Indyuce.bh.ressource;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.Indyuce.bh.Utils;

public enum Items
{
	NEXT_PAGE(new ItemStack(Material.ARROW), "Next", null),
	PREVIOUS_PAGE(new ItemStack(Material.ARROW), "Previous", null),
	PLAYER_HEAD(new ItemStack(Material.SKULL_ITEM, 1, (short)3), "%name%", null),
	PROFILE(new ItemStack(Material.SKULL_ITEM, 1, (short)3), "%name%", new String[] { "§8--------------------------------", "Bounties you claimed: §f%claimed-bounties%", "Successful bounties: §f%successful-bounties%", "§8--------------------------------" }),
	SET_BOUNTY(new ItemStack(Material.BOOK_AND_QUILL), "How to create a bounty?", new String[] { "Use /addbounty <player> <reward>", "to create a bounty on a player." }),
	BOUNTY_COMPASS(new ItemStack(Material.COMPASS), "Bounty Compass", new String[] { "It allows you to see at which", "distance your target is." }),
	;

	ItemStack item;
	short durability;
	String name;
	String[] lore;
  
	private Items(ItemStack item, String name, String[] lore) {
		lore=Utils.updateList("/language", "items", "lore", name(), lore);
		name=Utils.updateName("/language", "items", "name", name(), name);
		
		this.item=item;
	    this.name=name;
	    this.lore=lore;
	}
	public ItemStack a()
	{
	    ItemStack i=item.clone();
	    ItemMeta meta=i.getItemMeta();
	    meta.setDisplayName("§a"+name);
	    meta.addItemFlags(ItemFlag.values());
	    if (lore!=null)
	    {
		    ArrayList<String> lore=new ArrayList<String>();
		    for (int counter=0;counter<this.lore.length;counter++)
		    {
		    	String s=this.lore[counter];
		    	if (s.startsWith("§7")) {
			    	lore.add(s);
		    		continue;
		    	}
		    	lore.add("§7"+s);
		    }
		    meta.setLore(lore);
	    }
	    i.setItemMeta(meta);
	    return i;
	}
}