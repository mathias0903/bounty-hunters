package me.Indyuce.bh;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.Indyuce.bh.ressource.Items;

public class Utils implements Listener
{
	public static Main plugin;
	public Utils(Main ins)
	{
		plugin=ins;
    	if (!plugin.getConfig().getBoolean("disable-compass")) {
			new BukkitRunnable() {
				public void run() {
					for (Player p:Bukkit.getOnlinePlayers()) {
						loop(p);
					}
				}
			}.runTaskTimer(plugin,0,10);
    	}
	}
	public static boolean isCompass(ItemStack i) {
		if (!isPluginItem(i, true)) { return false; }
    	List<String> lore=i.getItemMeta().getLore();
    	List<String> lore1=Items.BOUNTY_COMPASS.a().getItemMeta().getLore();
    	for (int j=0;j<lore1.size();j++) {
    		if (!lore.get(j).equals(lore1.get(j))) {
    			return false;
    		}
    	}
    	return true;
	}
    public static double tronc(double x, int n) {
        double pow=Math.pow(10.0, n);
        return Math.floor(x*pow)/pow;
    }
	void loop(Player p) {
    	if (plugin.getConfig().getBoolean("disable-compass")) { return; }
    	ItemStack i=VersionUtils.getMainItem(p);
    	if (!isCompass(i)) { return; }
    	Player t = null;
    	
    	FileConfiguration config=ConfigData.getCD(plugin, "", "data");
    	for (String s:config.getConfigurationSection("").getKeys(false)) {
			if (config.getConfigurationSection(s).contains("hunters")) {
				List<String> hunters=config.getStringList(s+".hunters");
				if (hunters.contains(p.getName())) {
					t=Bukkit.getPlayer(s);
					break;
				}
			}
    	}
    	
    	if (t==null) {
    		if (!i.getItemMeta().getDisplayName().equals(Items.BOUNTY_COMPASS.a().getItemMeta().getDisplayName())) {
	    		i.setItemMeta(Items.BOUNTY_COMPASS.a().getItemMeta());
    		}
    		return;
    	}

		String format = msg("in-another-world");
		if (p.getWorld().getName().equals(t.getWorld().getName())) {
			if (plugin.getConfig().getBoolean("round-distance")) {
				format = (int) (t.getLocation().distance(p.getLocation())) + " blocks";
			} else {
				format = tronc(t.getLocation().distance(p.getLocation()), 3) + " blocks";
			}
			p.setCompassTarget(t.getLocation().clone().add(.5,0,.5));
		}
		
		ItemMeta i_meta=i.getItemMeta();
		i_meta.setDisplayName("§7§l[ §6§l"+format+" §7§l]");
		i.setItemMeta(i_meta);
	}
    public static boolean isPluginItem(ItemStack i, boolean lore)
    {
        if (i==null) { return false; }
        if (i.getItemMeta()==null) { return false; }
        if (i.getItemMeta().getDisplayName()==null) { return false; }
        if (lore&&i.getItemMeta().getLore()==null) { return false; }
        return true;
    }
	public static String updateName(String folder_path, String file, String path, String main_name, String name)
	{
		FileConfiguration config=ConfigData.getCD(plugin, folder_path, file);
		if (config.getConfigurationSection("").getKeys(false).contains(main_name))
		{
			return config.getString(main_name+"."+path);
		}
		return name;
	}
	public static String[] updateList(String folder_path, String file, String path, String name, String[] list)
	{
		FileConfiguration config=ConfigData.getCD(plugin, folder_path, file);
		if (config.getConfigurationSection("").getKeys(false).contains(name))
		{
			return config.getStringList(name+"."+path).toArray(new String[0]);
		}
		return list;
	}
    public static String msg(String path)
    {
    	FileConfiguration messages=ConfigData.getCD(plugin, "/language", "messages");
    	String msg=messages.getString(path);
    	msg=ChatColor.translateAlternateColorCodes('&', msg);
    	return msg;
    }
    public static ItemStack getProfile(Player p)
    {
    	FileConfiguration config=ConfigData.getCD(plugin, "/userdata", p.getUniqueId().toString());
    	ItemStack profile=Items.PROFILE.a().clone();
		SkullMeta profile_meta=(SkullMeta) profile.getItemMeta();
		profile_meta.setDisplayName(profile_meta.getDisplayName().replace("%name%", p.getName()));
		profile_meta.setOwner(p.getName());
		List<String> profile_lore=profile_meta.getLore();
		for (int j=0;j<profile_lore.size();j++)
		{
			String s=profile_lore.get(j);
			s=s.replace("%claimed-bounties%", ""+config.getInt("claimed-bounties"))
					.replace("%successful-bounties%", ""+config.getInt("successful-bounties"));
			profile_lore.set(j, s);
		}
		profile_meta.setLore(profile_lore);
		profile.setItemMeta(profile_meta);
		return profile;
    }
    public static void bountyClaimedAlert(Player p, Player t)
    {
    	FileConfiguration config=ConfigData.getCD(plugin, "", "data");
    	p.sendMessage(plugin.chat_window);
		p.sendMessage("§e"+msg("bounty-claimed-by-you")
				.replace("%target%", t.getName())
				.replace("%reward%", ""+config.getInt(t.getName()+".reward")));
		for (String h_format : config.getStringList(t.getName() + ".hunters")) {
			Player h = Bukkit.getPlayer(h_format);
			if (h == null) { continue; }
			h.setCompassTarget(h.getWorld().getSpawnLocation());
		}
		for (Player t1:Bukkit.getOnlinePlayers())
		{
			VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
			if (t1!=p)
			{
				t1.sendMessage("§e"+msg("bounty-claimed")
						.replace("%reward%", ""+config.getInt(t.getName()+".reward"))
						.replace("%killer%", p.getName())
						.replace("%target%", t.getName()));
			}
		}
    }
    public static void newBountyAlert(Player p, Player t)
    {
    	FileConfiguration config=ConfigData.getCD(plugin, "", "data");
    	int reward=config.getInt(t.getName()+".reward");
		VersionUtils.sound(t, "ENTITY_ENDERMEN_HURT", 1, 0);
    	if (p!=t)
    	{
        	t.sendMessage("§c"+msg("new-bounty-on-you")
        		.replace("%creator%", p.getName()));
	    }
		p.sendMessage("§e"+msg("bounty-created")
	 		.replace("%target%", t.getName()));
	 	p.sendMessage("§e"+msg("bounty-explain")
	 		.replace("%reward%", ""+reward));
    	for (Player t1:Bukkit.getOnlinePlayers())
    	{
    		if (t1!=t)
    		{
    			VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
    		}
    		if (t1!=t
    				&&t1!=p)
    		{
    			t1.sendMessage("§e"+msg("new-bounty-on-player")
	    			.replace("%creator%", p.getName())
	    			.replace("%target%", t.getName())
	    			.replace("%reward%", ""+reward));
    		}
    	}
    }
    public static void newHunterAlert(Player t, Player h)
    {
    	t.sendMessage("§c"+msg("new-hunter-alert"));
    	VersionUtils.sound(t.getLocation(), "ENTITY_ENDERMEN_HURT", 1, 1);
    }
    public static void autoBountyAlert(Player t)
    {
    	FileConfiguration config=ConfigData.getCD(plugin, "", "data");
    	int reward=config.getInt(t.getName()+".reward");
    	t.sendMessage("§c"+msg("illegal-kill"));
		VersionUtils.sound(t, "ENTITY_ENDERMEN_HURT", 1, 0);
    	for (Player t1:Bukkit.getOnlinePlayers())
    	{
    		if (t1!=t)
    		{
    			VersionUtils.sound(t1, "ENTITY_PLAYER_LEVELUP", 1, 2);
    		}
    		if (t1!=t)
    		{
    			t1.sendMessage("§e"+msg("auto-bounty")
	    			.replace("%target%", t.getName())
	    			.replace("%reward%", ""+reward));
    		}
    	}
    }
    public static void bountyExpired(String name)
    {
		for (Player t1:Bukkit.getOnlinePlayers())
		{
			VersionUtils.sound(t1, "ENTITY_VILLAGER_NO", 1, 2);
			t1.sendMessage("§e"+msg("bounty-expired")
				.replace("%target%", name));
		}
    }
}