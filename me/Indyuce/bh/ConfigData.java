package me.Indyuce.bh;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigData {
	public static void setupCD(Plugin plugin, String path, String name) {
		File pfile;
		if (!new File(plugin.getDataFolder() + path).exists()) {
			new File(plugin.getDataFolder() + path).mkdir();
		}
		pfile = new File(plugin.getDataFolder() + path, name + ".yml");
		if (!pfile.exists()) {
			try {
				pfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe("�4Could not create " + name + ".yml!");
			}
		}
	}

	public static FileConfiguration getCD(Plugin plugin, String path, String name) {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(new File(plugin.getDataFolder() + path, name + ".yml"));
		return config;
	}

	public static void saveCD(Plugin plugin, FileConfiguration config, String path, String name) {
		try {
			config.save(new File(plugin.getDataFolder() + path, name + ".yml"));
		} catch (IOException e2) {
			Bukkit.getServer().getLogger().severe("�4Could not save " + name + ".yml!");
		}
	}
}