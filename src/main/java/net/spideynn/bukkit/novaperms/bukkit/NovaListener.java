package net.spideynn.bukkit.novaperms.bukkit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class NovaListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        NovaPerms.generateAttachment(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        NovaPerms.unlogAttachment(p);
    }

}
