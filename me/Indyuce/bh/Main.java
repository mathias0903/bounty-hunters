package me.Indyuce.bh;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.Indyuce.bh.gui.BountiesGUI;
import me.Indyuce.bh.gui.LeaderboardGUI;
import me.Indyuce.bh.ressource.ConfigParams;
import me.Indyuce.bh.ressource.TitleReward;
import me.Indyuce.bh.ressource.Items;
import me.Indyuce.bh.ressource.MessagesParams;
import me.Indyuce.bh.ressource.UserdataParams;
import me.Indyuce.bh.title.Title;
import me.Indyuce.bh.title.Title_1_10_R1;
import me.Indyuce.bh.title.Title_1_11_R1;
import me.Indyuce.bh.title.Title_1_12_R1;
import me.Indyuce.bh.title.Title_1_8_R3;
import me.Indyuce.bh.title.Title_1_9_R2;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin
{
	public Title title;
	public String chat_window = "§e-----------------------------------------------------";
	public String prefix = "§8[§eBH§8] §7";
    private static Economy economy = null;
	
  	public void onDisable()
  	{
		/*
		FileConfiguration config=ConfigData.getCD(this, "", "data");
		
		for (String s:config.getConfigurationSection("").getKeys(false)) {
			if (config.getConfigurationSection(s).contains("creator")) {
				if (setupEco()) {
					OfflinePlayer p=Bukkit.getPlayer(config.getString(s+".creator"));
					double amount=config.getDouble(s+".reward");
					if (p!=null) {
						economy.depositPlayer(p, amount);
						p.sendMessage("§eYou were refunded §f"+amount+"§e.");
					}
				}
			}
			config.set(s, null);
		}
		
		ConfigData.saveCD(this, config, "", "data");
		
		Bukkit.getConsoleSender().sendMessage("§b------------------------------------------------------");
		Bukkit.getConsoleSender().sendMessage("§bDisabling "+getName()+" "+getDescription().getVersion()+"...");
		Bukkit.getConsoleSender().sendMessage("§bSucesfuly reset all the bounties.");
		Bukkit.getConsoleSender().sendMessage("§b------------------------------------------------------");
		*/
			
	    getConfig().options().copyDefaults(true);
	    saveConfig();
  	}
  	public void onEnable()
  	{
		Bukkit.getConsoleSender().sendMessage("§b------------------------------------------------------");
		Bukkit.getConsoleSender().sendMessage("§bEnabling "+getName()+" "+getDescription().getVersion()+"...");
		
	    Bukkit.getServer().getPluginManager().registerEvents(new MainListener(this), this);
	    Bukkit.getServer().getPluginManager().registerEvents(new Utils(this), this);
	    
	    Bukkit.getServer().getPluginManager().registerEvents(new BountiesGUI(this), this);
	    Bukkit.getServer().getPluginManager().registerEvents(new LeaderboardGUI(this), this);
	    
	    if (setupCompatibility()) {
	        Bukkit.getConsoleSender().sendMessage("§bDetected Server Version: "+VersionUtils.version);
		} else {
			Bukkit.getConsoleSender().sendMessage("§bYour server version is not compatible. Perhaps compatibility was still not added or your server version is outdated?");
			Bukkit.getConsoleSender().sendMessage("§b------------------------------------------------------");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
	    }
	    
	    if (getServer().getPluginManager().getPlugin("Vault")==null) {
			Bukkit.getConsoleSender().sendMessage("§cPlease install Vault in order to use this plugin!");
			Bukkit.getConsoleSender().sendMessage("§bDisabling "+getName()+" "+getDescription().getVersion()+"...");
			Bukkit.getConsoleSender().sendMessage("§b------------------------------------------------------");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
	    }

	    ConfigData.setupCD(this, "", "levels");
	    ConfigData.setupCD(this, "", "data");
	    ConfigData.setupCD(this, "/language", "messages");
	    ConfigData.setupCD(this, "/language", "items");
	    FileConfiguration messages = ConfigData.getCD(this, "/language", "messages");
	    FileConfiguration items = ConfigData.getCD(this, "/language", "items");
	    FileConfiguration levels = ConfigData.getCD(this, "", "levels");
	    getConfig().options().copyDefaults(true);
	    if (levels.getConfigurationSection("").getKeys(false).isEmpty()) {
	    	for (TitleReward title : TitleReward.values()) {
	    		levels.set("reward.title." + title.title, title.level);
	    	}
	    	for (TitleReward title : TitleReward.values()) {
	    		levels.set("reward.title." + title.title, title.level);
	    	}
	    }
	    
	    for (ConfigParams pa:ConfigParams.values())
	    {
	    	String path=pa.name().toLowerCase().replace("_", "-");
	    	if (!getConfig().getConfigurationSection("").getKeys(false).contains(path))
	    	{
	    		getConfig().set(path, pa.value);
	    	}
	    }
	    saveConfig();
	    for (MessagesParams pa:MessagesParams.values())
	    {
	    	String path=pa.name().toLowerCase().replace("_", "-");
	    	if (!messages.getConfigurationSection("").getKeys(false).contains(path))
	    	{
	    		messages.set(path, pa.value);
	    	}
	    }
	    for (Items i:Items.values())
	    {
	    	if (!items.getConfigurationSection("").getKeys(false).contains(i.name()))
	    	{
	    		items.set(i.name()+".name", i.a().getItemMeta().getDisplayName());
	    		items.set(i.name()+".lore", i.a().getItemMeta().getLore());
	    	}
	    }
	    for (Player p:Bukkit.getOnlinePlayers())
	    {
	    	File file=new File(this.getDataFolder()+"/userdata", p.getUniqueId().toString()+".yml");
	    	if (file.exists()) { continue; }
			ConfigData.setupCD(this, "/userdata", p.getUniqueId().toString());
			FileConfiguration pconfig=ConfigData.getCD(this, "/userdata", p.getUniqueId().toString());
			for (UserdataParams pa:UserdataParams.values())
			{
				String path=pa.name().toLowerCase().replace("_", "-");
				if (!pconfig.getKeys(false).contains(path))
				{
					pconfig.set(path, pa.value);
				}
			}
			ConfigData.saveCD(this, pconfig, "/userdata", p.getUniqueId().toString());
	    }
	    ConfigData.saveCD(this, items, "/language", "items");
	    ConfigData.saveCD(this, messages, "/language", "messages");
	    
		Bukkit.getConsoleSender().sendMessage("§b"+getName()+" "+getDescription().getVersion()+" has been enabled!");
		Bukkit.getConsoleSender().sendMessage("§b------------------------------------------------------");
  	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
  	{
  		Player p=(Player)sender;
  		// ====================================================================================================================================
  		if (cmd.getName().equalsIgnoreCase("addbounty"))
  		{
  			if (!p.hasPermission("bountyhunters.add")) {
  				p.sendMessage("§c"+Utils.msg("not-enough-perms"));
  				return false;
  			}
  			if (args.length<2)
  			{
  				p.sendMessage("§c"+Utils.msg("command-usage")
  					.replace("%command%", "/bounty <player> <reward>"));
  				return false;
  			}
  			FileConfiguration config=ConfigData.getCD(this, "", "data");
  	 		Player t=Bukkit.getPlayer(args[0]);
  	 		if (t==null)
  	 		{
  	 			p.sendMessage("§c"+Utils.msg("error-player"));
  	 			return false;
  	 		}
  	 		if (!t.isOnline())
  	 		{
  	 			p.sendMessage("§c"+Utils.msg("error-player"));
  	 			return false;
  	 		}
  	 		if (config.getConfigurationSection("").getKeys(false).contains(t.getName()))
  	 		{
  	 			p.sendMessage("§c"+Utils.msg("already-bounty-on-player"));
  	 			return false;
  	 		}
  	 		if (t.getName().equals(p.getName()))
  	 		{
  	 			p.sendMessage("§c"+Utils.msg("cant-set-bounty-on-yourself"));
  	 			return false;
  	 		}
  	 		if (t.hasPermission("bountyhunters.imun")
  	 				&& !p.hasPermission("bountyhunters.bypass-imun")) {
  	 			p.sendMessage("§c"+Utils.msg("bounty-imun"));
  	 			return false;
  	 		}
  	 		int reward=0;
  	 		try
  	 		{
  	 			reward=Integer.parseInt(args[1]);
  	 		}
  	 		catch (Exception e)
  	 		{
  	 			p.sendMessage("§c"+Utils.msg("not-valid-number").replace("%arg%", args[1]));
  	 			return false;
  	 		}
  	 		
  	 		int min = (getConfig().getInt("min-reward") > 0 ? getConfig().getInt("min-reward") : 0);
  	 		int max = getConfig().getInt("max-reward");
  	 		if ((reward < min) || (max > 0 && reward > max)) {
  	 			p.sendMessage("§c"+Utils.msg("wrong-reward")
  	 					.replace("%max%", "" + max)
	 					.replace("%min%", "" + min));
  	 			return false;
  	 		}
  	 		
  	 		double tax = reward * (getConfig().getDouble("tax") / 100);
  	 		tax = Utils.tronc(tax, 3);
  	 		int restriction=getConfig().getInt("bounty-set-restriction")*1000;
  	 		if (MainListener.last_bounty.containsKey(p))
  	 		{
  	  	 		long last=MainListener.last_bounty.get(p);
  	 			if (last+restriction>System.currentTimeMillis())
  	 			{
  	 				p.sendMessage("§c"+Utils.msg("bounty-set-restriction")
  	 					.replace("%time%", ""+restriction/1000));
  	 				return false;
  	 			}
  	 		}
  	 		if (setupEconomy())
  	 		{
  	  	 		if (!economy.has(p, reward))
  	  	 		{
  	  	 			p.sendMessage("§c"+Utils.msg("not-enough-money"));
  	  	 			return false;
  	  	 		}
  	  	 		economy.withdrawPlayer(p, reward);
  	 		}
  	 		reward -= (tax > reward ? reward : tax);
  	 		MainListener.last_bounty.put(p.getUniqueId(), System.currentTimeMillis());
  	 		config.set(t.getName()+".creator", p.getName());
  	 		config.set(t.getName()+".reward", reward);
  	 		config.set(t.getName()+".hunters", new String[] {});
  	 		ConfigData.saveCD(this, config, "", "data");
  	 		p.sendMessage(chat_window);
  	 		Utils.newBountyAlert(p, t);
  	 		if (tax > 0) {
	  	 		p.sendMessage("§c" + Utils.msg("tax-explain")
	  	 				.replace("%percent%", "" + getConfig().getDouble("tax"))
	  	 				.replace("%price%", "" + tax));
  	 		}
  		}
  		// ====================================================================================================================================
  		if (cmd.getName().equalsIgnoreCase("bounties"))
  		{
  			if (!p.hasPermission("bountyhunters.gui")) {
  				p.sendMessage("§c"+Utils.msg("not-enough-perms"));
  				return false;
  			}
  			if (args.length < 1)
  			{
  				BountiesGUI.openInv(p, 1);
  				return false;
  			}
  			else if (args[0].equalsIgnoreCase("leaderboard")
  					|| args[0].equalsIgnoreCase("lb"))
  			{
  	  			if (!p.hasPermission("bountyhunters.leaderboard-gui")) {
  	  				p.sendMessage("§c"+Utils.msg("not-enough-perms"));
  	  				return false;
  	  			}
  	  			LeaderboardGUI.openInv(p);
  			}
  			else if (args[0].equalsIgnoreCase("reload"))
  			{
  	  			if (!p.hasPermission("bountyhunters.op")) {
  	  				p.sendMessage("§c"+Utils.msg("not-enough-perms"));
  	  				return false;
  	  			}
  				reloadConfig();
  				p.sendMessage(prefix+"Configuration file reloaded.");
  			}
  		}
	  	// ====================================================================================================================================
  		return false;
  	}
	public Economy getEco() {
		return economy;
	}
	public boolean setupEco() {
		return setupEconomy();
	}
	private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider=getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider!=null)
        {
            economy=economyProvider.getProvider();
        }
        return economy!=null;
    }
	private boolean setupCompatibility() {
        String version1;
        try {
            version1 = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];

        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
            return false;
        }
        if (version1.equals("v1_8_R3")) {
        	title = new Title_1_8_R3();
        }
        else if (version1.equals("v1_9_R2")) {
        	title = new Title_1_9_R2();
        }
        else if (version1.equals("v1_10_R1")) {
        	title = new Title_1_10_R1();
        }
        else if (version1.equals("v1_11_R1")) {
        	title = new Title_1_11_R1();
        }
        else if (version1.equals("v1_12_R1")) {
        	title = new Title_1_12_R1();
        }
        VersionUtils.version = version1;
        return title != null;
    }
}