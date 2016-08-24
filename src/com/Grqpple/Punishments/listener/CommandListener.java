package com.Grqpple.Punishments.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.Grqpple.Punishments.Universal;

public class CommandListener implements Listener{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e){
        if(Universal.get().getMethods().callCMD(e.getPlayer(), e.getMessage())) e.setCancelled(true);
    }
}
