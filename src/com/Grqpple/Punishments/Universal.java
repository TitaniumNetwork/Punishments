package com.Grqpple.Punishments;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.Grqpple.Punishments.manager.PunishmentManager;
import com.Grqpple.Punishments.manager.UUIDManager;
import com.Grqpple.Punishments.utils.Punishment;

public class Universal {
    private static Universal instance = null;
    public static Universal get(){
        return instance == null ? instance = new Universal() : instance;
    }

    private MethodInterface mi;

    private Map<String, String> ips = new HashMap<String, String>();

    public void setup(MethodInterface mi){
        this.mi = mi;
        mi.loadFiles();

        PunishmentManager.get().setup();

        mi.setCommandExecutor("punishments");
        mi.setCommandExecutor("ban");
        mi.setCommandExecutor("tempban");
        mi.setCommandExecutor("ipban");
        mi.setCommandExecutor("kick");
        mi.setCommandExecutor("warn");
        mi.setCommandExecutor("tempwarn");
        mi.setCommandExecutor("mute");
        mi.setCommandExecutor("tempmute");
        mi.setCommandExecutor("unmute");
        mi.setCommandExecutor("unwarn");
        mi.setCommandExecutor("unban");
        mi.setCommandExecutor("banlist");
        mi.setCommandExecutor("history");
        mi.setCommandExecutor("warns");
        mi.setCommandExecutor("check");
        mi.setCommandExecutor("systemprefs");
    }

    public Map<String, String> getIps() {
        return ips;
    }

    public MethodInterface getMethods() {
        return mi;
    }

    public String getFromURL(String surl){
        String response = null;
        try{
            URL url = new URL(surl);
            Scanner s = new Scanner(url.openStream());
            if(s.hasNext()){
                response = s.next();
                s.close();
            }
        }catch(IOException exc){ System.out.println("aBans <> !! Failed to connect to URL: "+surl); }
        return response;
    }

    public boolean isMuteCommand(String cmd){
        cmd = cmd.contains(":") ? cmd.split(":", 2)[1] : cmd;
        for(String str : getMethods().getStringList(getMethods().getConfig(), "MuteCommands")) if(cmd.equals(str)) return true;
        return false;
    }

    public String callConnection(String name, String ip) {
        name = name.toLowerCase();
        String uuid = UUIDManager.get().getUUID(name);
        Punishment pt = PunishmentManager.get().getBan(uuid);
        if(pt == null) pt = PunishmentManager.get().getBan(ip);
        if(pt != null){
            return pt.getLayoutBSN();
        }

        if(Universal.get().getIps().containsKey(name)){
            Universal.get().getIps().remove(name);
        }
        Universal.get().getIps().put(name, ip);
        return null;
    }
}
