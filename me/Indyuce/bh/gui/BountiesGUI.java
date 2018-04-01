package me.Indyuce.bh.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.Indyuce.bh.ConfigData;
import me.Indyuce.bh.Main;
import me.Indyuce.bh.Utils;
import me.Indyuce.bh.VersionUtils;
import me.Indyuce.bh.resource.Items;

public class BountiesGUI implements Listener {
	static HashMap<UUID, Integer> currentPage = new HashMap<UUID, Integer>();

	static int getAvailableSlot(Inventory inv) {
		Integer[] slots = new Integer[] { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };
		for (int available : slots)
			if (inv.getItem(available) == null)
				return available;
		return -1;
	}

	@SuppressWarnings("deprecation")
	public static void openInv(Player p, int page) {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		ConfigurationSection section = config.getConfigurationSection("");
		int max_page = getMaxPage();
		Inventory inv = Bukkit.createInventory(null, 54, Utils.msg("gui-name").replace("%page%", "" + page).replace("%max-page%", "" + max_page));
		int min = (page - 1) * 21;
		int max = page * 21;
		if (!Main.plugin.getConfig().getBoolean("hide-bounty-when-no-perm") || p.hasPermission("bountyhunters.claim"))
			for (int j = min; j < max; j++)
				if (j < section.getKeys(false).size()) {
					String s = new ArrayList<String>(section.getKeys(false)).get(j);
					ItemStack i = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
					SkullMeta i_meta = (SkullMeta) i.getItemMeta();
					i_meta.addItemFlags(ItemFlag.values());
					i_meta.setOwner(s);
					i_meta.setDisplayName("§a" + s);
					List<String> i_lore = new ArrayList<String>();

					// creator, reward, hunters size
					if (!config.getConfigurationSection(s).contains("creator"))
						i_lore.add("§c" + Utils.msg("thug-player"));
					else if (config.getString(s + ".creator").equals(p.getName()))
						i_lore.add("§7" + Utils.msg("set-by-yourself"));
					else
						i_lore.add("§7" + Utils.msg("set-by").replace("%creator%", config.getString(s + ".creator")));
					i_lore.add("§7" + Utils.msg("reward-is").replace("%reward%", Utils.format(config.getDouble(s + ".reward"))));
					i_lore.add("§7" + Utils.msg("current-hunters").replace("%hunters%", "" + config.getStringList(s + ".hunters").size()));
					i_lore.add("");

					// bounty status
					if (p.getName().equals(s))
						i_lore.add("§c" + Utils.msg("dont-let-them-kill-u"));
					else if (!config.getConfigurationSection(s).contains("creator"))
						i_lore.add("§e" + Utils.msg("kill-him-claim-bounty"));
					else if (config.getString(s + ".creator").equals(p.getName()))
						i_lore.add("§e" + Utils.msg("right-click-remove-bounty"));
					else
						i_lore.add("§e" + Utils.msg("kill-him-claim-bounty"));

					// target compass
					if (!p.getName().equals(s) && Main.plugin.getConfig().getBoolean("compass.enabled")) {
						if (config.getStringList(s + ".hunters").contains(p.getName()))
							i_lore.add("§c" + Utils.msg("click-untarget"));
						else
							i_lore.add("§e" + Utils.msg("click-target"));

					}
					i_meta.setLore(i_lore);
					i.setItemMeta(i_meta);

					inv.setItem(getAvailableSlot(inv), i);
				}

		ItemStack compass = Items.BOUNTY_COMPASS.a().clone();
		ItemMeta compass_meta = compass.getItemMeta();
		List<String> compass_lore = compass_meta.getLore();
		compass_lore.add("");
		compass_lore.add("§e" + Utils.msg("click-buy-compass").replace("%price%", Utils.format(Main.plugin.getConfig().getDouble("compass.price"))));
		compass_meta.setLore(compass_lore);
		compass.setItemMeta(compass_meta);

		inv.setItem(26, Items.NEXT_PAGE.a());
		inv.setItem(18, Items.PREVIOUS_PAGE.a());

		if (Main.plugin.getConfig().getBoolean("enable-quotes-levels-titles"))
			inv.setItem(45, Utils.getLvlAdvancement(p));
		inv.setItem(47, Utils.getProfile(p));
		inv.setItem(49, Items.SET_BOUNTY.a());

		if (Main.plugin.getConfig().getBoolean("compass.enabled"))
			inv.setItem(51, compass);

		p.openInventory(inv);
		currentPage.put(p.getUniqueId(), page);
	}

	private int getCurrentPage(Player p) {
		return (currentPage.containsKey(p.getUniqueId()) ? currentPage.get(p.getUniqueId()) : 0);
	}

	@EventHandler
	public void b(InventoryCloseEvent e) {
		currentPage.remove(e.getPlayer().getUniqueId());
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void a(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack i = (ItemStack) e.getCurrentItem();

		// page returns 0 if inv not open
		int page = getCurrentPage(p);
		if (page < 1)
			return;

		e.setCancelled(true);
		if (e.getInventory() != e.getClickedInventory())
			return;
		if (!Utils.isPluginItem(i, false))
			return;

		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		if (i.getItemMeta().getDisplayName().equals(Items.NEXT_PAGE.a().getItemMeta().getDisplayName())) {
			if (page < getMaxPage())
				openInv(p, page + 1);
			return;
		}
		if (i.getItemMeta().getDisplayName().equals(Items.PREVIOUS_PAGE.a().getItemMeta().getDisplayName())) {
			if (page > 1)
				openInv(p, page - 1);
			return;
		}
		if (i.getItemMeta().getDisplayName().equals(Items.BOUNTY_COMPASS.a().getItemMeta().getDisplayName())) {
			if (p.getInventory().firstEmpty() <= -1) {
				p.sendMessage("§c" + Utils.msg("empty-inv-first"));
				return;
			}
			double price = Main.plugin.getConfig().getDouble("compass.price");
			if (Main.plugin.economy.getBalance(p) < price) {
				p.sendMessage("§c" + Utils.msg("not-enough-money"));
				return;
			}
			Main.plugin.economy.withdrawPlayer(p, price);
			VersionUtils.sound(p, "ENTITY_PLAYER_LEVELUP", 1, 2);
			p.getInventory().addItem(Items.BOUNTY_COMPASS.a());
			return;
		}

		// target compass
		if (e.getAction() == InventoryAction.PICKUP_ALL && Main.plugin.getConfig().getBoolean("compass.enabled")) {
			for (String s : config.getConfigurationSection("").getKeys(false)) {
				if (s.equals(p.getName()))
					continue;
				if (s.equals(ChatColor.stripColor(i.getItemMeta().getDisplayName()))) {
					String format = ChatColor.stripColor(i.getItemMeta().getDisplayName());
					Player t = Bukkit.getPlayer(format);
					if (t == null || !t.isOnline()) {
						p.sendMessage("§e" + Utils.msg("player-must-be-connected"));
						return;
					}
					if (t.hasPermission("bountyhunters.imun") && !p.hasPermission("bountyhunters.bypass-imun")) {
						p.sendMessage("§e" + Utils.msg("track-imun"));
						return;
					}

					List<String> hunters = new ArrayList<String>();
					if (config.getConfigurationSection(s).contains("hunters"))
						hunters = config.getStringList(s + ".hunters");

					if (!hunters.contains(p.getName())) {
						hunters.add(p.getName());
						if (Main.plugin.getConfig().getBoolean("new-hunter-alert"))
							Utils.newHunterAlert(t, p);
						p.sendMessage("§e" + Utils.msg("target-set"));
					} else {
						hunters.remove(p.getName());
						p.sendMessage("§e" + Utils.msg("target-removed"));
						p.setCompassTarget(p.getWorld().getSpawnLocation());
					}

					config.set(s + ".hunters", hunters);
					ConfigData.saveCD(Main.plugin, config, "", "data");
					openInv(p, page);
					break;
				}
			}
		}

		// remove bounty
		if (e.getAction() == InventoryAction.PICKUP_HALF)
			for (String s : config.getConfigurationSection("").getKeys(false))
				if (s.equals(ChatColor.stripColor(i.getItemMeta().getDisplayName())) && config.getString(s + ".creator").equals(p.getName())) {
					double reward = config.getDouble(s + ".reward");

					// gives the players the money back if upped the bounty
					if (config.getConfigurationSection(s).contains("up"))
						for (String up : config.getConfigurationSection(s + ".up").getKeys(false)) {
							double given = config.getDouble(s + ".up." + up);
							Main.plugin.economy.depositPlayer(Bukkit.getOfflinePlayer(up), given);
							reward -= given;
						}
					Main.plugin.economy.depositPlayer(p, reward);
					Utils.bountyExpired(s);
					config.set(s, null);
					ConfigData.saveCD(Main.plugin, config, "", "data");
					openInv(p, page);
					break;
				}
	}

	public static int getMaxPage() {
		FileConfiguration config = ConfigData.getCD(Main.plugin, "", "data");
		int size = config.getConfigurationSection("").getKeys(false).size();
		int max_page = 0;
		while (size >= 0) {
			size -= 21;
			max_page++;
		}
		return max_page;
	}
}