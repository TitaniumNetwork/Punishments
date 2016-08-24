package com.Grqpple.Punishments.listener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.Grqpple.Punishments.manager.CommandManager;

public class CommandReceiver implements CommandExecutor{

    private static CommandReceiver instance = null;
    public static CommandReceiver get(){
        return instance == null ? instance = new CommandReceiver() : instance;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CommandManager.get().onCommand(commandSender, command.getName(), strings);
        return true;
    }
}
