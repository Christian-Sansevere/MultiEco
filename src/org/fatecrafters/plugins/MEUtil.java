package org.fatecrafters.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MEUtil {

	private static MultiEco plugin;

	public static HashMap<String, List<String>> groupMap = new HashMap<String, List<String>>();
	public static List<String> groups = new ArrayList<String>();
	public static List<String> worlds = new ArrayList<String>();
	public static List<String[]> cmds = new ArrayList<String[]>();

	public static void setPlugin(MultiEco plugin) {
		MEUtil.plugin = plugin;
	}

	public static void setupGroups() {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			public void run() {
				File f = new File(plugin.getDataFolder()+File.separator+"groups.yml");
				FileConfiguration config = YamlConfiguration.loadConfiguration(f);
				final Collection<String> groupNames = config.getConfigurationSection("Groups").getKeys(false);
				groupMap.clear();
				worlds.clear();
				for (String group : groupNames) {
					List<String> groupWorldList = config.getStringList("Groups."+group);
					groupMap.put(group, groupWorldList);
					groups.add(group);
				}
			}
		});
		
		for (World w : plugin.getServer().getWorlds()) {
			worlds.add(w.getName());
		}
		
	}

}
