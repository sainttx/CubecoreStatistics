package com.kingarchie.plugins.cubecorestatistics;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CubecoreStatistics extends JavaPlugin {

    private Chat chat;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.chat = getServer().getServicesManager().getRegistration(Chat.class).getProvider();
        this.getServer().getPluginManager().registerEvents(new CSListener(this), this);
    }

    @Override
    public void onDisable() {
        this.saveConfig();
    }
    
    public Chat getChat() {
        return chat;
    }
    
    public int getScore(Player player) {
        return getConfig().getInt(player.getName(), 0); // Default score of 0 if they don't already have one
    }
    
    public void setScore(Player player, int score) {
        getConfig().set(player.getName(), score);
    }
}