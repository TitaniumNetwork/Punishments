package com.Grqpple.Punishments.manager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.Grqpple.Punishments.MethodInterface;
import com.Grqpple.Punishments.Universal;
import com.Grqpple.Punishments.utils.Punishment;
import com.Grqpple.Punishments.utils.PunishmentType;

public class CommandManager{
    private static CommandManager instance = null;
    public static CommandManager get(){
        return instance == null ? instance = new CommandManager() : instance;
    }
    
    public void onCommand(final Object sender, final String cmd, final String[] args){
        Universal.get().getMethods().runAsync(new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                if (args.length > 0) args[0] = args[0].toLowerCase();

                PunishmentType pt = PunishmentType.fromCommandName(cmd);
                MethodInterface mi = Universal.get().getMethods();
                if (pt != null) {
                    if (mi.hasPerms(sender, pt.getPerms())) {
                        boolean isTemp = pt.isTemp();//cmd.getName().matches("temp.*");
                        int argsLenght = (isTemp ? 2 : 1);
                        if (args.length >= argsLenght) {

                            if (!isTemp || (args[1].toLowerCase().matches("[1-9][0-9]*([w,d,h,m,s]|mo)") || args[1].toLowerCase().matches("#.+"))) {
                                String reason = null;
                                if (args.length > argsLenght) {
                                    if (!args[argsLenght].matches("@.+") && !args[argsLenght].matches("~.+")) {
                                        reason = "";
                                        for (int i = argsLenght; i < args.length; i++) {
                                            reason += " " + args[i];
                                        }
                                        reason = reason.substring(1);
                                    } else {
                                        if (!mi.contains(mi.getLayouts(), "Message." + args[argsLenght].substring(1))) {
                                            MessageManager.sendMessage(sender, "General.LayoutNotFound", true, "NAME", args[argsLenght].substring(1));
                                            return;
                                        }
                                        reason = args[argsLenght];
                                    }
                                }

                                String name = args[0];
                                String uuid;
                                if (pt != PunishmentType.IP_BAN) {
                                    uuid = UUIDManager.get().getUUID(args[0]);
                                } else {
                                    if (args[0].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                                        uuid = args[0];
                                    } else {
                                        if (Universal.get().getIps().containsKey(args[0].toLowerCase())) {
                                            uuid = Universal.get().getIps().get(args[0].toLowerCase());
                                        } else {
                                            MessageManager.sendMessage(sender, "Ipban.IpNotCashed", true, "NAME", args[0]);
                                            return;
                                        }
                                    }
                                }

                                long end;
                                if (isTemp) {
                                    end = TimeManager.getTime();
                                    String time = args[1];
                                    if (args[1].matches("#.+")) {
                                        if (!mi.contains(mi.getLayouts(), "Time." + args[1].substring(1))) {
                                            MessageManager.sendMessage(sender, "General.LayoutNotFound", true, "NAME", args[1].substring(1));
                                            return;
                                        }
                                        int i = 0;
                                        for (Punishment pts : PunishmentManager.get().getHistory()) {
                                            if (pts.getUuid().equals(uuid) && pts.getCalculation() != null && pts.getCalculation().equalsIgnoreCase(args[1].substring(1)))
                                                i++;
                                        }
                                        List<String> timeLayout = mi.getStringList(mi.getLayouts(), "Time." + args[1].substring(1));
                                        time = timeLayout.get(timeLayout.size() <= i ? timeLayout.size() - 1 : i);
                                    }
                                    long toAdd = TimeManager.toMilliSec(time.toLowerCase());
                                    end += toAdd;

                                    if (!mi.hasPerms(sender, "ab." + pt.getName() + ".dur.max")) {
                                        long max = -1;
                                        for (int i = 10; i >= 1; i--) {
                                            if (mi.hasPerms(sender, "ab." + pt.getName() + ".dur." + i) && mi.contains(mi.getConfig(), "TempPerms." + i)) {
                                                max = mi.getLong(mi.getConfig(), "TempPerms." + i) * 1000;
                                                break;
                                            }
                                        }
                                        if (max != -1 && toAdd > max) {
                                            MessageManager.sendMessage(sender, pt.getConfSection() + ".MaxDuration", true, "MAX", max / 1000 + "");
                                            return;
                                        }
                                    }
                                } else end = -1;

                                if (!mi.isOnline(args[0])) {
                                    if (pt == PunishmentType.KICK) {
                                        MessageManager.sendMessage(sender, "Kick.NotOnline", true, "NAME", args[0]);
                                        return;
                                    }
                                } else {
                                    if (mi.hasPerms(mi.getPlayer(args[0]), "ab." + pt.getName() + ".exempt")) {
                                        MessageManager.sendMessage(sender, pt.getBasic().getConfSection() + ".Exempt", true, "NAME", args[0]);
                                        return;
                                    }
                                }

                                if ((pt.getBasic() == PunishmentType.MUTE && PunishmentManager.get().isMuted(uuid)) ||
                                        (pt.getBasic() == PunishmentType.BAN && PunishmentManager.get().isBanned(uuid))) {
                                    MessageManager.sendMessage(sender, pt.getBasic().getConfSection() + ".AlreadyDone", true, "NAME", args[0]);
                                    return;
                                }

                                new Punishment(name, uuid, reason, mi.getName(sender), pt, TimeManager.getTime(), end, isTemp && args[1].matches("#.+") ? args[1].substring(1) : null, -1).create();
                            } else MessageManager.sendMessage(sender, pt.getConfSection() + ".Usage", true);
                        } else MessageManager.sendMessage(sender, pt.getConfSection() + ".Usage", true);
                    } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                } else if (cmd.toLowerCase().matches("un.+")) {
                    pt = PunishmentType.fromCommandName(cmd.toLowerCase().substring(2));
                    if (mi.hasPerms(sender, "ab." + pt.getName() + ".undo")) {
                        if (args.length == 1) {
                            if (pt != PunishmentType.WARNING) {
                                String uuid = args[0];
                                if (!args[0].matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"))
                                    uuid = UUIDManager.get().getUUID(args[0]);

                                Punishment pnt = pt == PunishmentType.MUTE ? PunishmentManager.get().getMute(uuid) : PunishmentManager.get().getBan(uuid);
                                if (pnt != null) {
                                    pnt.delete();
                                    MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Done", true, "NAME", args[0]);
                                } else
                                    MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".NotPunished", true, "NAME", args[0]);
                            } else {
                                if (args[0].matches("[0-9]+")) {
                                    Punishment pnt = PunishmentManager.get().getWarn(Integer.valueOf(args[0]));
                                    if (pnt != null) {
                                        pnt.delete();
                                        MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Done", true, "ID", args[0]);
                                    } else
                                        MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".NotFound", true, "ID", args[0]);
                                } else MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Usage", true);
                            }
                        } else MessageManager.sendMessage(sender, "Un" + pt.getConfSection() + ".Usage", true);
                    } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                } else if (cmd.equalsIgnoreCase("banlist")) {
                    if (mi.hasPerms(sender, "ab.banlist")) {
                        if (args.length == 0 || (args.length == 1 && args[0].matches("[1-9][0-9]*"))) {
                            performList(sender, args.length == 0 ? 1 : Integer.valueOf(args[0]), "Banlist", PunishmentManager.get().getPunishments(true), "nope", false);
                        } else MessageManager.sendMessage(sender, "Banlist.Usage", true);
                    } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                } else if (cmd.equalsIgnoreCase("history")) {
                    if (mi.hasPerms(sender, "ab.history")) {
                        if (args.length == 1 || (args.length == 2 && args[1].matches("[1-9][0-9]*"))) {
                            performList(sender, args.length == 1 ? 1 : Integer.valueOf(args[1]), "History", PunishmentManager.get().getPunishments(UUIDManager.get().getUUID(args[0]), null, false), args[0], true);
                        } else MessageManager.sendMessage(sender, "History.Usage", true);
                    } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                } else if (cmd.equalsIgnoreCase("warns")) {
                    String name = mi.getName(sender);
                    int page = 1;
                    if (args.length == 0 || (args.length == 1 && args[0].matches("[1-9][0-9]*"))) {
                        if (!mi.hasPerms(sender, "ab.warns.own")) {
                            MessageManager.sendMessage(sender, "General.NoPerms", true);
                            return;
                        } else if (args.length == 1) page = Integer.valueOf(args[0]);
                    } else if (args.length == 1 || (args.length == 2 && args[1].matches("[1-9][0-9]*"))) {
                        if (!mi.hasPerms(sender, "ab.warns.other")) {
                            MessageManager.sendMessage(sender, "General.NoPerms", true);
                            return;
                        } else if (args.length == 2) page = Integer.valueOf(args[1]);
                        name = args[0];
                    } else {
                        MessageManager.sendMessage(sender, "Warns.Usage", true);
                        return;
                    }
                    performList(sender, page, "Warns", PunishmentManager.get().getPunishments(UUIDManager.get().getUUID(name), PunishmentType.WARNING, true), name, false);
                } else if (cmd.equalsIgnoreCase("check")) {
                    if (mi.hasPerms(sender, "ab.check")) {
                        if (args.length == 1) {
                            try {
                                String uuid = UUIDManager.get().getUUID(args[0].toLowerCase());
                                String ip = Universal.get().getIps().containsKey(args[0].toLowerCase()) ? Universal.get().getIps().get(args[0]).toLowerCase() : "none cashed";
                                String loc = mi.getFromURL_JSON("http://freegeoip.net/json/" + ip, "country_name");
                                Punishment mute = PunishmentManager.get().getMute(uuid);
                                Punishment ban = PunishmentManager.get().getBan(uuid);

                                MessageManager.sendMessage(sender, "Check.Header", true, "NAME", args[0]);
                                MessageManager.sendMessage(sender, "Check.UUID", false, "UUID", uuid);
                                if (mi.hasPerms(sender, "ab.check.ip")) MessageManager.sendMessage(sender, "Check.IP", false, "IP", ip);
                                MessageManager.sendMessage(sender, "Check.Geo", false, "LOCATION", loc == null ? "failed!" : loc);
                                MessageManager.sendMessage(sender, "Check.Mute", false, "DURATION", mute == null ? "�anone" : mute.getType().isTemp() ? "�e" + mute.getDuration(false) : "�cperma");
                                MessageManager.sendMessage(sender, "Check.Ban", false, "DURATION", ban == null ? "�anone" : ban.getType().isTemp() ? "�e" + ban.getDuration(false) : "�cperma");
                                MessageManager.sendMessage(sender, "Check.Warn", false, "COUNT", PunishmentManager.get().getCurrentWarns(uuid) + "");
                            } catch (NullPointerException exc) {
                                MessageManager.sendMessage(sender, "Check.NotFound", true, "NAME", args[0]);
                            }
                        } else MessageManager.sendMessage(sender, "Check.Usage", true);
                    } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                } else if (cmd.equalsIgnoreCase("systemPrefs")) {
                    if (mi.hasPerms(sender, "ab.systemprefs")) {
                        mi.sendMessage(sender, "�c�lPunishments v9000 �cSystemPrefs");
                        mi.sendMessage(sender, "�cServer-Time �8>> �7" + new Date().getHours() + ":" + new Date().getMinutes());
                        mi.sendMessage(sender, "�cYour UUID (Intern) �8>> �7" + mi.getInternUUID(sender));
                    } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                } else if (cmd.equalsIgnoreCase("punishments")) {
                    if (args.length == 0) {
                        mi.sendMessage(sender, "�8�l�m-=====�r �c�lPunishments v9000 �8�l�m=====-�r ");
                        mi.sendMessage(sender, "  �cDev �8>> �7Grqpple");
                        mi.sendMessage(sender, "  �cStatus �8>> �a�oStable");
                        mi.sendMessage(sender, "  �cVersion �8>> �7" + mi.getVersion());
                        mi.sendMessage(sender, "  �cLicense �8>> �7Private");
                        mi.sendMessage(sender, "  �cPrefix �8>> �7" + MessageManager.getMessage("General.Prefix"));
                        mi.sendMessage(sender, "�8�l�m-===========================-�r ");
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        if (mi.hasPerms(sender, "ab.reload")) {
                            mi.loadFiles();
                            mi.sendMessage(sender, "�aReloaded!");
                        } else MessageManager.sendMessage(sender, "General.NoPerms", true);
                    }
                }
            }
        });
    }

    private void performList(Object sender, int cPage, String confName, List<Punishment> pnts, String name, boolean history){
        MethodInterface mi = Universal.get().getMethods();
        if(pnts.size() == 0){
            MessageManager.sendMessage(sender, confName+".NoEntries", true, "NAME", name);
            return;
        }
        for(Punishment pnt : pnts) if(pnt.isExpired()) pnt.delete();
        if(pnts.size()/5.0+1 > cPage){
            for(String str : MessageManager.getLayout(mi.getMessages(), confName+".Header", "PREFIX", MessageManager.getMessage("General.Prefix"), "NAME", name)) mi.sendMessage(sender, str);


            SimpleDateFormat format = new SimpleDateFormat(mi.getString(mi.getConfig(), "DateFormat", "dd.MM.yyyy-HH:mm"));
            for (int i = (cPage-1)*5; i < cPage*5 && pnts.size() > i; i++) {
                Punishment pnt = pnts.get(i);
                for(String str : MessageManager.getLayout(mi.getMessages(), confName+".Entry",
                        "PREFIX", MessageManager.getMessage("General.Prefix"),
                        "NAME", pnt.getName(),
                        "DURATION", pnt.getDuration(history),
                        "OPERATOR", pnt.getOperator(),
                        "REASON", pnt.getReason(),
                        "TYPE", pnt.getType().getConfSection(),
                        "ID", pnt.getId()+"",
                        "DATE", format.format(new Date(pnt.getStart())))){
                    mi.sendMessage(sender, str);
                }
            }
            MessageManager.sendMessage(sender, confName+".Footer", false,
                    "CURRENT_PAGE", cPage+"",
                    "TOTAL_PAGES", (pnts.size()/5+(pnts.size()%5 != 0 ? 1 : 0))+"",
                    "COUNT", pnts.size()+"");
            if(pnts.size()/5.0+1 > cPage+1)
                MessageManager.sendMessage(sender, confName+".PageFooter", false,
                        "NEXT_PAGE", (cPage+1)+"",
                        "NAME", name);
        }else MessageManager.sendMessage(sender, confName+".OutOfIndex", true, "PAGE", cPage+"");
    }
}
