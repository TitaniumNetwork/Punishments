package com.Grqpple.Punishments.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.Grqpple.Punishments.Universal;

public class ChatListener implements Listener{

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e){
        if(Universal.get().getMethods().callChat(e.getPlayer())) e.setCancelled(true);
    }
}
