package com.kingarchie.plugins.cubecorestatistics;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CSListener implements Listener {
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent e) {
		if(!CubecoreStatistics.instance.file.pathExists(e.getPlayer().getName())) {
			CubecoreStatistics.instance.file.set(e.getPlayer().getName(), 0);
			CubecoreStatistics.instance.getChat().setPlayerSuffix(e.getPlayer(), CubecoreStatistics.instance.col(" &8[&60&8]"));
		} else if(CubecoreStatistics.instance.file.pathExists(e.getPlayer().getName())) {
			int playerScore = CubecoreStatistics.instance.file.getInt(e.getPlayer().getName());
			String playerScoreString = CubecoreStatistics.instance.col("&6" + String.valueOf(playerScore));
			CubecoreStatistics.instance.getChat().setPlayerSuffix(e.getPlayer(), CubecoreStatistics.instance.col(" &8[" + playerScoreString + "&8]"));
		}
	}
	
	@EventHandler
	public void PlayerDeathEvent(PlayerDeathEvent e) {
		if (e.getEntity().getKiller() instanceof Player) {
			Player killer = e.getEntity().getKiller();
			Player deceased = e.getEntity();
			int kScore = (int) CubecoreStatistics.instance.file.get(killer.getName());
			int dScore = (int) CubecoreStatistics.instance.file.get(deceased.getName());
			CubecoreStatistics.instance.file.set(killer.getName(), kScore + 1);
			CubecoreStatistics.instance.file.set(deceased.getName(), dScore - 1);
			refreshStats(killer);
			refreshStats(deceased);
		}
	}
	
	public void refreshStats(Player player) {
		int score = (int) CubecoreStatistics.instance.file.get(player.getName());
		String scoreString = CubecoreStatistics.instance.col("&6" + String.valueOf(score));
		CubecoreStatistics.instance.getChat().setPlayerSuffix(player, CubecoreStatistics.instance.col(" &8[" + scoreString + "&8]"));
	}

}
