package me.Indyuce.bh.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.Ordering;

import me.Indyuce.bh.ConfigData;
import me.Indyuce.bh.Main;
import me.Indyuce.bh.Utils;

public class LeaderboardGUI implements Listener {
	public static Main plugin;
	public LeaderboardGUI(Main ins) {
		plugin = ins;
	}
	static Integer[] slots = new Integer[] {
		             13,
			      21,22,23,
			   29,30,31,32,33,
			37,38,39,40,41,42,43 };
	static int getAvailableSlot(Inventory inv) {
		for (int available : slots) {
			if (inv.getItem(available) == null) {
				return available;
			}
		}
		return -1;
	}
	public static String getGUIName() {
  		String name = Utils.msg("leaderboard-gui-name");
  		name = ChatColor.stripColor(name);
  		int max_chars = 25;
  		if (name.length() > max_chars) {
  			name = name.substring(0, max_chars);
  		}
  		return name;
	}
	public static void openInv(Player p) {
  		HashMap<String, Integer> map = new HashMap<String, Integer>();
  		
  		File f = new File(plugin.getDataFolder(), "userdata");
  		for (File f1 : f.listFiles()) {
  			String name = f1.getName()
  					.replace(".yml", "");
  			UUID uuid;
  			try {
  				uuid = UUID.fromString(name);
  			} catch (Exception e) {
  				continue;
  			}
  			OfflinePlayer t = Bukkit.getOfflinePlayer(uuid);
  			if (t == null) {
  				continue;
  			}
  			
  			FileConfiguration tconfig = ConfigData.getCD(plugin, "/userdata", uuid.toString());
  			map.put(t.getName(), tconfig.getInt("claimed-bounties"));
  		}
  		
  		List<Integer> order = Ordering.natural().greatestOf(map.values(), 20);
  		
		Inventory inv = Bukkit.createInventory(null, 54, "§n" + getGUIName());
	    for (int j = 0;j < slots.length;j++) {
	    	if (j < order.size()) {
	    		String s = getKeyByValue(map, order.get(j));
	    		map.remove(s);
	    		if (order.get(j) <= 0) { continue; }
	    		
	    		ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
				SkullMeta i_meta = (SkullMeta) i.getItemMeta();
				i_meta.addItemFlags(ItemFlag.values());
				i_meta.setOwner(s);
				i_meta.setDisplayName("§a[" + (j + 1) + "] " + s);
				List<String> i_lore = new ArrayList<String>();
				i_lore.add("§7" + Utils.msg("leaderboard-gui-completed-bounties")
						.replace("%bounties%", "" + order.get(j)));
				i_meta.setLore(i_lore);
				i.setItemMeta(i_meta);
				
				inv.setItem(getAvailableSlot(inv), i);
	    	}
  		}
    	
    	p.openInventory(inv);
	}
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
	@EventHandler
	public void a(InventoryClickEvent e) {
		if (e.getInventory().getName().equals("§n" + getGUIName())) {
			e.setCancelled(true);
		}
	}
}