package com.Grqpple.Punishments.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.Grqpple.Punishments.MethodInterface;
import com.Grqpple.Punishments.Universal;
import com.Grqpple.Punishments.utils.Punishment;
import com.Grqpple.Punishments.utils.PunishmentType;

public class PunishmentManager {

    private static PunishmentManager instance = null;
    public static PunishmentManager get(){
        return instance == null ? instance = new PunishmentManager() : instance;
    }

    private List<Punishment> punishments = Collections.synchronizedList(new ArrayList<Punishment>());
    private List<Punishment> history = Collections.synchronizedList(new ArrayList<Punishment>());

    public void setup(){
            MethodInterface mi = Universal.get().getMethods();
            if(mi.contains(mi.getData(), "Punishments")){
                for(String key : mi.getKeys(mi.getData(), "Punishments")) {
                    punishments.add(
                            new Punishment(mi.getString(mi.getData(), "Punishments."+key+".name"),
                                    mi.getString(mi.getData(), "Punishments."+key+".uuid"), mi.getString(mi.getData(), "Punishments."+key+".reason"),
                                    mi.getString(mi.getData(), "Punishments."+key+".operator"),
                                    PunishmentType.valueOf(mi.getString(mi.getData(), "Punishments."+key+".punishmentType")),
                                    mi.getLong(mi.getData(), "Punishments."+key+".start"),
                                    mi.getLong(mi.getData(), "Punishments."+key+".end"),
                                    mi.getString(mi.getData(), "Punishments."+key+"calculation"),
                                    Integer.valueOf(key)
                                    )
                            );
                }
            }
            if(mi.contains(mi.getData(), "PunishmentHistory")) {
                for(String key : mi.getKeys(mi.getData(), "PunishmentHistory")) {
                    history.add(
                            new Punishment(mi.getString(mi.getData(), "PunishmentHistory."+key+".name"),
                                    mi.getString(mi.getData(), "PunishmentHistory."+key+".uuid"), mi.getString(mi.getData(), "PunishmentHistory."+key+".reason"),
                                    mi.getString(mi.getData(), "PunishmentHistory."+key+".operator"),
                                    PunishmentType.valueOf(mi.getString(mi.getData(), "PunishmentHistory."+key+".punishmentType")),
                                    mi.getLong(mi.getData(), "PunishmentHistory."+key+".start"),
                                    mi.getLong(mi.getData(), "PunishmentHistory."+key+".end"),
                                    mi.getString(mi.getData(), "PunishmentHistory."+key+"calculation"),
                                    Integer.valueOf(key)
                            )
                            );
                }
            }
    }

    public List<Punishment> getPunishments(String uuid, PunishmentType put, boolean current) {
        List<Punishment> punList = new ArrayList<Punishment>();
        for(Punishment pu : current ? punishments : history){
            if((put == null || put == pu.getType().getBasic()) && pu.getUuid().equals(uuid)){
                if(!current || !pu.isExpired()) punList.add(pu);
                else pu.delete();
            }
        }
        return punList;
    }

    public Punishment getWarn(int id){
        for(Punishment pt : getPunishments(true)){
            if(pt.getType().getBasic() == PunishmentType.WARNING && pt.getId() == id){
                return pt;
            }
        }
        return null;
    }

    public Punishment getBan(String uuid){
        for(Punishment pt : getPunishments(true)){
            if(pt.getType().getBasic() == PunishmentType.BAN && pt.getUuid().equals(uuid)){
                return pt;
            }
        }
        return null;
    }

    public Punishment getMute(String uuid){
        for(Punishment pt : getPunishments(true)){
            if(pt.getType().getBasic() == PunishmentType.MUTE && pt.getUuid().equals(uuid)){
                return pt;
            }
        }
        return null;
    }

    public boolean isBanned(String uuid){
        return getBan(uuid) != null;
    }

    public boolean isMuted(String uuid){
        return getMute(uuid) != null;
    }

    public int getCurrentWarns(String uuid){
        int i = 0;
        for(Punishment pt : getPunishments(true))
            if(pt.getType().getBasic() == PunishmentType.WARNING && pt.getUuid().equals(uuid))
                i++;
        return i;
    }

    public List<Punishment> getPunishments(boolean checkExpired) {
        if(checkExpired){
            List<Punishment> toDelete = new ArrayList<Punishment>();
            for(Punishment pu : punishments) if(pu.isExpired()) toDelete.add(pu);
            for(Punishment pu : toDelete) pu.delete();
        }
        return punishments;
    }

    public long getCalculation(String layout, String uuid){
        long end = TimeManager.getTime();
        MethodInterface mi = Universal.get().getMethods();

        int i = 0;
        for (Punishment pts : getHistory()) {
            if (pts.getUuid().equals(uuid) && pts.getCalculation() != null && pts.getCalculation().equalsIgnoreCase(layout))
                i++;
        }

        List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + layout);
        String time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
        long toAdd = TimeManager.toMilliSec(time.toLowerCase());
        end += toAdd;

        return end;
    }

    public List<Punishment> getHistory() {
        return history;
    }
}
