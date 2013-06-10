package org.fatecrafters.plugins;

import java.io.File;
import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiEco extends JavaPlugin {

	public static Economy econ = null;
	private boolean blockCmds = false;

	@Override
	public void onEnable() {
		MEUtil.setPlugin(this);
		getServer().getPluginManager().registerEvents(new MEListener(this), this);
		saveDefaultConfig();
		File f = new File(getDataFolder()+File.separator+"userdata");
		if (!f.exists()) {
			f.mkdirs();
		}
		File groupsFile = new File(getDataFolder()+File.separator+"groups.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(groupsFile);
		if (!groupsFile.exists()) {
			try {
				groupsFile.createNewFile();
				String[] exampleList = {"aWorld", "anotherWorld"};
				config.set("Groups.exampleGroup", exampleList);
				config.save(groupsFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			MEUtil.setupGroups();
		}
		File messageFile = new File(getDataFolder()+File.separator+"messages.yml");
		if (!messageFile.exists()) {
			try {
				messageFile.createNewFile();
				FileConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);
				messageConfig.set("onBlockedCommand", "&cYou can not send money to a player in a seperate economy as you.");
			    messageConfig.save(messageFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!setupEconomy()) {
			getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (getConfig().getBoolean("blockCommandsToDifferentGroups")) {
			blockCmds = true;
			int i = 0;
			for (String s : getConfig().getStringList("blockedCommands")) {
				i++;
				String[] split = s.split(" ");
				MEUtil.cmds.add(i, split);
			}
		}
		getLogger().info("MultiEco has been enabled.");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		getServer().getPluginManager().disablePlugin(this);
		getLogger().info("MultiEco has been disabled.");
	}

	public boolean onCommand(final CommandSender sender, Command cmd, String label, final String[] args) {
		if(cmd.getName().equalsIgnoreCase("multieco")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (!sender.hasPermission("multieco.reload")) {
						sender.sendMessage(ChatColor.LIGHT_PURPLE+"~! "+ChatColor.GRAY+"You do not have permission.");
						return false;
					}
					MEUtil.setupGroups();
					reloadConfig();
					MEUtil.cmds.clear();
					if (getConfig().getBoolean("blockCommandsToDifferentGroups")) {
						blockCmds = true;
						int i = 0;
						for (String s : getConfig().getStringList("blockedCommands")) {
							i++;
							String[] split = s.split(" ");
							MEUtil.cmds.add(i, split);
						}
					}
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"~! "+ChatColor.GRAY+"MultiEco's groups file, config file, and messages file has been reloaded.");
					return true;
				}
				if (args[0].equalsIgnoreCase("reset")) {
					if (!sender.hasPermission("multieco.reset")) {
						sender.sendMessage(ChatColor.LIGHT_PURPLE+"~! "+ChatColor.GRAY+"You do not have permission.");
						return false;
					}
					if (args.length == 2) {
						getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
							public void run() {
								File dir = new File(getDataFolder()+File.separator+"userdata");
								int i = 0, times = 0, files = dir.listFiles().length;
								for (File child : dir.listFiles()) {
									FileConfiguration config = YamlConfiguration.loadConfiguration(child);
									i++;
									config.set("Data.Worlds."+args[1], null);
									try {
										config.save(child);
									} catch (IOException e) {
										e.printStackTrace();
									}
									if (i == 100) {
										i = 0;
										times++;
										sender.sendMessage(ChatColor.LIGHT_PURPLE+""+times*100+"/"+files+" user files have been reset.");
									}
								}
								sender.sendMessage(ChatColor.GREEN+"The world "+ChatColor.GOLD+args[1]+ChatColor.GREEN+" has been reset in the user files.");
							}
						});
						return true;
					} else {
						sender.sendMessage(ChatColor.LIGHT_PURPLE+"~! "+ChatColor.GRAY+"Correct usage: /multieco reset <world>");
						return false;
					}
				}
				if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					if (!sender.hasPermission("multieco.help")) {
						sender.sendMessage(ChatColor.LIGHT_PURPLE+"~! "+ChatColor.GRAY+"You do not have permission.");
						return false;
					}
					sender.sendMessage(ChatColor.LIGHT_PURPLE +" ~! "+ChatColor.GRAY+"/multieco reload"+ChatColor.LIGHT_PURPLE+"- Reloads the groups.yml and config.yml files");
					sender.sendMessage(ChatColor.LIGHT_PURPLE +" ~! "+ChatColor.GRAY+"/multieco reset <world>"+ChatColor.LIGHT_PURPLE+"- Resets *ALL* data of this world. The balances of this world will become 0. Only use this when changing around groups and balances are being duped.");
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
    public boolean isBlockingCmds() {
        return blockCmds;
    }
    
    public String blockedMessage() {
		File messageFile = new File(getDataFolder()+File.separator+"messages.yml");
		FileConfiguration messageConfig = YamlConfiguration.loadConfiguration(messageFile);
        return messageConfig.getString("onBlockedCommand");
    }

}
