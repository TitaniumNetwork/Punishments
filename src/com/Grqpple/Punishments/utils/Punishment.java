package com.Grqpple.Punishments.utils;

import java.util.List;

import com.Grqpple.Punishments.MethodInterface;
import com.Grqpple.Punishments.Universal;
import com.Grqpple.Punishments.manager.MessageManager;
import com.Grqpple.Punishments.manager.PunishmentManager;
import com.Grqpple.Punishments.manager.TimeManager;

public class Punishment {
    private int id;

    private String name;
    private String uuid;

    private String reason;
    private String operator;

    private long start;
    private long end;

    private String calculation;

    private PunishmentType type;

    private static MethodInterface mi = Universal.get().getMethods();

    public Punishment(String name, String uuid, String reason, String operator, PunishmentType type, long start, long end, String calculation, int id) {
        this.name = name;
        this.uuid = uuid;
        this.reason = reason;
        this.operator = operator;
        this.type = type;
        this.start = start;
        this.end = end;
        this.calculation = calculation;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getReason() {
        return reason == null ? "none" : reason;
    }

    public String getOperator() {
        return operator;
    }

    public String getCalculation() {
        return calculation;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public PunishmentType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public void create(){
        if(id != -1) {
            System.out.println("!! Failed! AB tried to overwrite the punishment:");
            System.out.println("!! Failed at: "+toString());
            return;
        }

        if(uuid == null){
            System.out.println("!! Failed! AB has not saved the "+getType().getName()+" because there is no fetched UUID");
            System.out.println("!! Failed at: "+toString());
            return;
        }

            int i = 1;
            while(mi.contains(mi.getData(), "Punishments."+i)) i++;
            id = i;

            String pathT = "Punishments.";
            for (int j = 0; j < 2; j++) {
                if(j != 0 || getType() != PunishmentType.KICK) {
                    mi.set(mi.getData(), pathT + id + ".name", getName());
                    mi.set(mi.getData(), pathT + id + ".uuid", getUuid());
                    mi.set(mi.getData(), pathT + id + ".reason", getReason());
                    mi.set(mi.getData(), pathT + id + ".operator", getOperator());
                    mi.set(mi.getData(), pathT + id + ".punishmentType", getType().name());
                    mi.set(mi.getData(), pathT + id + ".start", getStart());
                    mi.set(mi.getData(), pathT + id + ".end", getEnd());
                    mi.set(mi.getData(), pathT + id + ".calculation", getCalculation());
                }
                pathT = "PunishmentHistory.";
            }

            mi.saveData();


        final int cWarnings =  getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid())+1) : 0;

        System.out.println("Called!");
        if(getType().getBasic() == PunishmentType.WARNING){
            String cmd = "";
            for (int i1 = 1; i1 <= cWarnings; i1++) {
                System.out.println("Checking #"+i1+" CONTAINS: "+mi.contains(mi.getConfig(), "WarnActions."+i1)+" | VALUE: "+mi.getString(mi.getConfig(), "WarnActions."+i1, "none"));
                if(mi.contains(mi.getConfig(), "WarnActions."+i1)) cmd = mi.getString(mi.getConfig(), "WarnActions."+i1);
            }
            final String finalCmd = cmd;
            mi.runSync(new Runnable() {
                @Override
                public void run() {
                    mi.executeCommand(finalCmd.replaceAll("%PLAYER%", getName()).replaceAll("%COUNT%", cWarnings+""));
                }
            });
            System.out.println("Executing ... "+cmd);
        }

        List<String> notification = MessageManager.getLayout( mi.getMessages(),
                getType().getConfSection()+".Notification",
                "OPERATOR", getOperator(),
                "PREFIX", MessageManager.getMessage("General.Prefix"),
                "DURATION", getDuration(true),
                "REASON", getReason(),
                "NAME", getName(),
                "COUNT", cWarnings+""
        );

        for(Object op : mi.getOnlinePlayers()){
            if(mi.hasPerms(op, "ab."+getType().getName()+".notify"))
                for(String str : notification)
                    mi.sendMessage(op, str);
        }

        if(mi.isOnline(getName())){
            final Object p = mi.getPlayer(getName());

            if(getType().getBasic() == PunishmentType.BAN || getType() == PunishmentType.KICK){
                mi.runSync(new Runnable() {
                    @Override
                    public void run() {
                        mi.kickPlayer(p, getLayoutBSN());
                    }
                });
            }else{
                for(String str : getLayout()) mi.sendMessage(p, str);
            }
        }

        if(getType() != PunishmentType.KICK) PunishmentManager.get().getPunishments(false).add(this);
        PunishmentManager.get().getHistory().add(this);
    }

    public void delete(){
        if(getType() == PunishmentType.KICK){
            System.out.println("!! Failed deleting! You are not able to delete Kicks!");
        }

        if(id == -1){
            System.out.println("!! Failed deleting! The Punishment is not created yet!");
            System.out.println("!! Failed at: "+toString());
            return;
        }


      
            mi.set(mi.getData(), "Punishments."+getId(), null);
            mi.saveData();

//        if(PunishmentManager.get().getPunishments().contains(this))
        PunishmentManager.get().getPunishments(false).remove(this);
    }

    public List<String> getLayout(){
        boolean isLayout = getReason().matches("@.+") || getReason().matches("~.+") ;
        List<String> layout = MessageManager.getLayout(
                isLayout ? mi.getLayouts() : mi.getMessages(),
                isLayout ? "Message."+getReason().substring(1) : getType().getConfSection()+".Layout",
                "OPERATOR", getOperator(),
                "PREFIX", MessageManager.getMessage("General.Prefix"),
                "DURATION", getDuration(false),
                "REASON", getReason(),
                "COUNT", getType().getBasic() == PunishmentType.WARNING ? (PunishmentManager.get().getCurrentWarns(getUuid())+1)+"" : "0"
        );

        return layout;
    }

    public String getDuration(boolean fromStart){
        String duraton = "permanent";
        if(getType().isTemp()){
            long diff = (getEnd()-(fromStart?start:TimeManager.getTime()))/1000;
            if(diff > 60*60*24) duraton = MessageManager.getMessage("General.TimeLayoutD", "D", diff/60/60/24+"", "H", diff/60/60%24+"", "M", diff/60%60+"", "S", diff%60+"");
            else if(diff > 60*60) duraton = MessageManager.getMessage("General.TimeLayoutH", "H", diff/60/60+"", "M", diff/60%60+"", "S", diff%60+"");
            else if(diff > 60) duraton = MessageManager.getMessage("General.TimeLayoutM", "M", diff/60+"", "S", diff%60+"");
            else duraton = MessageManager.getMessage("General.TimeLayoutS", "S", diff+"");
        }
        return duraton;
    }

    public String getLayoutBSN(){
        String msg = "";
        for(String str : getLayout()) msg+= "\n"+str;
        return msg.substring(1);
    }

    public boolean isExpired(){
        return getType().isTemp() && getEnd() <= TimeManager.getTime();
    }

    @Override
    public String toString() {
        return "Punishment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", reason='" + reason + '\'' +
                ", operator='" + operator + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", calculation='" + calculation + '\'' +
                ", type=" + type +
                '}';
    }
}
