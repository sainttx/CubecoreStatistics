package com.kingarchie.plugins.cubecorestatistics;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class CSListener implements Listener {

    private CubecoreStatistics plugin;

    public CSListener(CubecoreStatistics plugin) {
        this.plugin = plugin;
    }

    private String createSuffix(int score) {
        return ChatColor.translateAlternateColorCodes('&', " &8[&6" + score + "&8]");
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        int score = plugin.getScore(player);
        plugin.getChat().setPlayerSuffix(player, createSuffix(score));
    }

    @EventHandler
    public void playerDeathEvent(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        Player killer = dead.getKiller();

        if (killer != null) {
            int deadScore = plugin.getScore(dead) - 1; // Remove 1 point from the dead player
            int killerScore = plugin.getScore(killer); // Add 1 point to the killer

            plugin.setScore(dead, deadScore); // Update the dead players score
            plugin.setScore(killer, killerScore); // Update the killers score

            // Update player suffixes
            plugin.getChat().setPlayerSuffix(dead, createSuffix(deadScore));
            plugin.getChat().setPlayerSuffix(killer, createSuffix(killerScore));
        }
    }
}
