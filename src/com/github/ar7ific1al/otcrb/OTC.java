package com.github.ar7ific1al.otcrb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OTC extends JavaPlugin	{
	//	TEST TIME :D
	// ~Jag was here! :D
	public final Logger console = Logger.getLogger("OTC");
	
	public OTCListener listener = new OTCListener(this);
	
	File pluginDir = new File("plugins/OnTheClock");
	File clockDir = new File("plugins/OnTheClock/Players/");
	public static File settingsFile;
	static FileConfiguration settings;
	
	static String modAnnounce;
	
	@Override
	public void onEnable()	{
		
		PluginManager pm = getServer().getPluginManager();
		PluginDescriptionFile pdFile = this.getDescription();
		String ver = pdFile.getVersion();
		String enabled = "OTC v" + ver + " enabled.";
		
		settingsFile = new File(getDataFolder(), "settings.yml");
		settings = new YamlConfiguration();
		
		console.info(enabled);
		if(!pluginDir.exists())	{
			console.log(Level.WARNING, "[OTC] Plugin directory not found! Creating...");
			pluginDir.mkdir();
			console.log(Level.INFO, "[OTC] Plugin directory created successfully.");
		}
		if(!clockDir.exists())	{
			console.log(Level.WARNING, "[OTC] Clock directory not found! Creating...");
			clockDir.mkdir();
			console.log(Level.INFO, "[OTC] Clock directory created successfully.");
		}
		
		firstRun();
		loadSettings();
		pm.registerEvents(listener, this);
		modAnnounce = settings.getString("Defaults.Announcements");
	}
	
	@Override
	public void onDisable()	{
		PluginDescriptionFile pdFile = this.getDescription();
		String ver = pdFile.getVersion();
		String disabled = "OTC v" + ver + " disabled.";
		console.info(disabled);
		
		for(Player p : Bukkit.getServer().getOnlinePlayers())	{
			if (p.hasPermission("otc.clock"))	{
				try {
					clock(p.getName(), "out");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void firstRun()	{
		if(!settingsFile.exists())	{
			saveResource("settings.yml", false);
		}
	}
	
	public void copy(InputStream in, File file)	{
		try	{
			FileOutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while((len = in.read(buf)) > 0)	{
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch(Exception e)	{
			e.printStackTrace();
		}
	}
	
	public void loadSettings()	{
		try {
			settings.load(settingsFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public void newPlayerConfig(Player player) throws FileNotFoundException, IOException, InvalidConfigurationException	{
		if (player.hasPermission("otc.announce.special"))	{
			File pFile = new File(getDataFolder(), player.getName() + ".yml");
			FileConfiguration tmpfc = new YamlConfiguration();
			tmpfc.load(pFile);
			tmpfc.addDefault("Announcement", settings.get("Defaults.Announcements"));
			tmpfc.options().copyDefaults(true);
			tmpfc.save(pFile);
		}
	}
	
	public static void clock(String pName, String clock) throws FileNotFoundException, IOException, InvalidConfigurationException	{
		File pConfig = new File("plugins/OnTheClock/Players/", pName + ".yml");
		if (!pConfig.exists())	{
			pConfig.createNewFile();
		}
		FileConfiguration tmpfc = new YamlConfiguration();
		tmpfc.load(pConfig);
		
		Calendar cal = Calendar.getInstance();
		Date date= new Date();
		cal.setTime(date);
		
		int year = cal.get(Calendar.YEAR);
		String month = new DateFormatSymbols().getMonths()[cal.get(Calendar.MONTH)];
		int day = cal.get(Calendar.DAY_OF_MONTH);
		String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" +  cal.get(Calendar.SECOND);
		if(clock.equalsIgnoreCase("in"))	{
			if(tmpfc.contains(year + "." + month + "." + day))	{
				List<String> clocks = new ArrayList<String>();
				for(Object s : tmpfc.getList(year + "." + month + "." + day))	{
					clocks.add((String) s);
				}
				clocks.add("In: " + time);
				tmpfc.set(year + "." + month + "." + day, clocks);
			}
			else	{
				List<String> clocks = new ArrayList<String>();
				clocks.add("In: " + time);
				tmpfc.addDefault(year + "." + month + "." + day, clocks);
			}
		}
		else if(clock.equalsIgnoreCase("out"))	{
			if(tmpfc.contains(year + "." + month + "." + day))	{
				List<String> clocks = new ArrayList<String>();
				for(Object s : tmpfc.getList(year + "." + month + "." + day))	{
					clocks.add((String) s);
				}
				clocks.add("Out: " + time);
				tmpfc.set(year + "." + month + "." + day, clocks);
			}
			else	{
				List<String> clocks = new ArrayList<String>();
				clocks.add("Out: " + time);
				tmpfc.addDefault(year + "." + month + "." + day, clocks);
			}
		}
		tmpfc.options().copyDefaults(true);
		tmpfc.save(pConfig);
	}
	
	public static String broadcastJoin(Player player, String type) throws FileNotFoundException, IOException, InvalidConfigurationException {
		String pName = player.getName();
		String message = "";
		if(type.equalsIgnoreCase("mod"))	{
			settings.load(settingsFile);
			message = formatBroadcast(settings.getString("Defaults.Announcements"), pName);
		}
		else if(type.equalsIgnoreCase("custom"))	{
			File tf = new File("plugins/OnTheClock/Players/", pName + ".yml");
			FileConfiguration tmpfc = new YamlConfiguration();
			tmpfc.load(tf);
			tmpfc.addDefault("Announcement", settings.getString("Defaults.Announcements"));
			tmpfc.options().copyDefaults(true);
			tmpfc.save(tf);
			message = tmpfc.getString("Announcement");
		}
		return formatBroadcast(settings.getString("Variables.Announcement Prefix") + message, pName);
	}
	
	public static String formatBroadcast(String message, String pName)	{
		message = message.replaceAll("%p", pName);
		message = message.replaceAll("&1", "\u00A71");
		message = message.replaceAll("&2", "\u00A72");
		message = message.replaceAll("&3", "\u00A73");
		message = message.replaceAll("&4", "\u00A74");
		message = message.replaceAll("&5", "\u00A75");
		message = message.replaceAll("&6", "\u00A76");
		message = message.replaceAll("&7", "\u00A77");
		message = message.replaceAll("&8", "\u00A78");
		message = message.replaceAll("&9", "\u00A79");
		message = message.replaceAll("&0", "\u00A70");
		message = message.replaceAll("&a", "\u00A7a");
		message = message.replaceAll("&b", "\u00A7b");
		message = message.replaceAll("&c", "\u00A7c");
		message = message.replaceAll("&d", "\u00A7d");
		message = message.replaceAll("&e", "\u00A7e");
		message = message.replaceAll("&f", "\u00A7f");
		message = message.replaceAll("&k", "\u00A7k");
		message = message.replaceAll("&l", "\u00A7l");
		message = message.replaceAll("&m", "\u00A7m");
		message = message.replaceAll("&n", "\u00A7n");
		message = message.replaceAll("&o", "\u00A7o");
		message = message.replaceAll("&r", "\u00A7r");
		return message;
	}

	
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args)	{
		if (sender instanceof Player)	{
			Player player = (Player) sender;
			String pName = player.getName();
			if (cmdLabel.equalsIgnoreCase("otc"))	{
				try	{
					if (args.length < 1)	{
						player.sendMessage(ChatColor.GOLD + "[OTC] " + ChatColor.YELLOW +  "On The Clock v" + getDescription().getVersion() + " by " +	 ChatColor.RED + "Ar7ific1al");
						if (player.hasPermission("otc.announce.custom"))	{
							player.sendMessage(ChatColor.YELLOW + " You have otc.announce.custom. Use " + ChatColor.RED + "/otc cb Message" + ChatColor.YELLOW
									+ " to set your own custom join message. You can use format codes for color, bold, italics, etc.");
						}
					}
					else	{
						if (args[0].equalsIgnoreCase("cb"))	{
							if (player.hasPermission("otc.announce.custom"))	{
								if (args.length < 2)	{
									File pFile = new File("plugins/OnTheClock/Players/", player.getName() + ".yml");
									if (!pFile.exists())	{
										player.sendMessage(ChatColor.GOLD + "[OTC] " + ChatColor.YELLOW + "You do not currently have a special announcement.\n\tUse " + ChatColor.RED + "/otc sa message" + ChatColor.YELLOW + " to set your announcement.");
									}
									else	{
										FileConfiguration tmpfc = new YamlConfiguration();
										tmpfc.load(pFile);
										player.sendMessage(ChatColor.GOLD + "[OTC] " + ChatColor.YELLOW + "Your announcement is currently set to:");
										player.sendMessage(formatBroadcast(tmpfc.getString("Announcement"), pName));
									}
								}
								else if (args.length > 1)	{
									String message = "";
									for (int i = 1; i < args.length; ++i)	{
										message += args[i];
										if(i != args.length)	{
											message += " ";
										}
									}
									File pFile = new File("plugins/OnTheClock/Players/", pName + ".yml");
									if (pFile.exists())	{
										FileConfiguration tmpfc = new YamlConfiguration();
										tmpfc.load(pFile);
										tmpfc.set("Announcement", message);
										tmpfc.save(pFile);
									}
									else if (!pFile.exists())	{
										console.log(Level.INFO, "File " + pFile + " doesn't exist! Creating file...");
										pFile = new File("plugins/OnTheClock/Players", pName + ".yml");
										pFile.createNewFile();
										FileConfiguration tmpfc = new YamlConfiguration();
										tmpfc.load(pFile);
										tmpfc.set("Announcement", message);
										tmpfc.save(pFile);
										console.log(Level.INFO, "File " + pFile + " created, log has been updated.");
									}
									message = formatBroadcast(message, pName);
									player.sendMessage(ChatColor.GOLD + "[OTC] " + ChatColor.YELLOW + "Your announcement was changed to:");
									player.sendMessage(formatBroadcast(message, pName));
								}
							}
							else	{
								player.sendMessage(ChatColor.GOLD + "[OTC] " + ChatColor.YELLOW + "You do not have permission to do that.");
							}
						}
					}				
				
				} catch (IndexOutOfBoundsException ex)	{
					return false;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	
}
