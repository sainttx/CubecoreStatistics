package com.kingarchie.plugins.cubecorestatistics;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;

public class CubecoreStatistics extends JavaPlugin {
	
	public static CubecoreStatistics instance;
	private Chat chat = null;
	public DataFile file = new DataFile("plugins" + File.separator + "Cubecore Statistics" + File.separator , "config", ".yml" ).copyDefaults(this.getResource("config.yml"), false);

	public void onEnable() {
		instance = this;
		setupChat();
		getServer().getPluginManager().registerEvents(new CSListener(), this);
		getLogger().info("Loading plugin...");
	}
	
	public void onDisable() { 
		file.save();
		getLogger().info("Unloading plugin...");
		instance = null;
	}
	
	 private boolean setupChat() {
	        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
	        chat = rsp.getProvider();
	        return chat != null;
	    }


	public Chat getChat() {
		return chat;
	}
	
	public String col(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
}