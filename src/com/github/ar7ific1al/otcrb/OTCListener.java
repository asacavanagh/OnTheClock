package com.github.ar7ific1al.otcrb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OTCListener implements Listener	{

	public OTC plugin;
	
	public OTCListener(OTC instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void login(PlayerJoinEvent event)	{
		Player player = event.getPlayer();
		UUID pUUID = player.getUniqueId();
		try	{
			if(player.hasPermission("otc.clock"))	{
					OTC.clock(pUUID, "in");
			}
			if(player.hasPermission("otc.announce.mod") && !player.hasPermission("otc.announce.custom"))	{
				event.setJoinMessage(OTC.broadcastJoin(player, "mod"));
			}
			else if(player.hasPermission("otc.announce.custom"))	{
				File pFile = new File("plugins/OnTheClock/Players/", pUUID + ".yml");
				if(!pFile.exists())	{
					pFile.createNewFile();
				}
				event.setJoinMessage(OTC.broadcastJoin(player, "custom"));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
	}
	
	@EventHandler
	public void logout(PlayerQuitEvent event)	{
		Player player = event.getPlayer();
		UUID pUUID = player.getUniqueId();
		try	{
			if(player.hasPermission("otc.clock"))	{
					OTC.clock(pUUID, "out");
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}
	}
	
}
