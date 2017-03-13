/*
 * This file is part of UltimateCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) Bammerbom
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package bammerbom.ultimatecore.bukkit.api;

import bammerbom.ultimatecore.bukkit.ErrorLogger;
import bammerbom.ultimatecore.bukkit.JsonConfig;
import bammerbom.ultimatecore.bukkit.UltimateFileLoader;
import bammerbom.ultimatecore.bukkit.listeners.AutomessageListener;
import bammerbom.ultimatecore.bukkit.listeners.TabListener;
import bammerbom.ultimatecore.bukkit.r;
import bammerbom.ultimatecore.bukkit.resources.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.util.*;

public class UServer {
    static String motd = "";
    //Receiver, Sender
    static Map<UUID, UUID> tp = new HashMap<UUID, UUID>();
    static Map<UUID, UUID> tph = new HashMap<UUID, UUID>();
    //Warps
    static HashMap<String, Location> warps = null;

    public static void start() {
        if (!StringUtil.nullOrEmpty(motd)) {
            motd = "";
        }
        try {
            File file = new File(r.getUC().getDataFolder(), "motd.txt");
            if (!file.exists()) {
                r.getUC().saveResource("motd.txt", true);
            }
            ArrayList<String> lines = FileUtil.getLines(file);
            for (String str : lines) {
                motd = motd + ChatColor.translateAlternateColorCodes('&', str) + "\n";
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to load MOTD");
        }

    }

    //Invsee
    public List<Player> getInOnlineInventoryOnlinePlayers() {
        List<Player> pls = new ArrayList<>();
        for (Player pl : r.getOnlinePlayers()) {
            if (UC.getPlayer(pl).isInOnlineInventory()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    public List<Player> getInOfflineInventoryOnlinePlayers() {
        List<Player> pls = new ArrayList<>();
        for (Player pl : r.getOnlinePlayers()) {
            if (UC.getPlayer(pl).isInOfflineInventory()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    //Motd
    public String getMotd() {
        String mt = motd;
        StringBuilder b = new StringBuilder("");
        Integer i = 0;
        for (Player pl : r.getOnlinePlayers()) {
            i++;
            if (!StringUtil.nullOrEmpty(b.toString())) {
                b.append(", ");
            }
            b.append(UC.getPlayer(pl).getDisplayName());
        }
        mt = mt.replace("{ONLINE}", b.toString());
        mt = mt.replace("{PLAYERS}", b.toString());
        mt = mt.replace("{PLAYERLIST}", b.toString());
        mt = mt.replace("{TIME}", DateFormat.getTimeInstance(2, Locale.getDefault()).format(new Date()));
        mt = mt.replace("{DATE}", DateFormat.getDateInstance(2, Locale.getDefault()).format(new Date()));
        mt = mt.replace("{TPS}", PerformanceUtil.getTps() + "");
        mt = mt.replace("{UPTIME}", ChatColor.stripColor(DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime())));
        StringBuilder pb = new StringBuilder();
        for (Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (!StringUtil.nullOrEmpty(pb.toString())) {
                pb.append(", ");
            }
            pb.append(pl.getDescription().getName());
        }
        mt = mt.replace("{PLAYERCOUNT}", i + "");
        mt = mt.replace("{PLUGINS}", pb.toString());
        mt = mt.replace("{VERSION}", Bukkit.getServer().getVersion());
        mt = mt.replace("{WORLD}", ChatColor.stripColor(r.mes("notAvailable")));
        mt = mt.replace("{WORLDNAME}", ChatColor.stripColor(r.mes("notAvailable")));
        mt = mt.replace("{COORDS}", ChatColor.stripColor(r.mes("notAvailable")));
        mt = mt.replace("{PLAYER}", ChatColor.stripColor(r.mes("notAvailable")));
        mt = mt.replace("{NAME}", ChatColor.stripColor(r.mes("notAvailable")));
        mt = mt.replace("{RAWNAME}", ChatColor.stripColor(r.mes("notAvailable")));
        return mt;
    }

    public String getMotd(Player p) {
        String mt = motd;
        mt = mt.replace("{PLAYER}", UC.getPlayer(p).getDisplayName());
        mt = mt.replace("{NAME}", UC.getPlayer(p).getDisplayName());
        mt = mt.replace("{RAWNAME}", p.getName());
        mt = mt.replace("{WORLD}", p.getWorld().getName());
        mt = mt.replace("{WORLDNAME}", p.getWorld().getName());
        mt = mt.replace("{COORDS}", p.getLocation().getBlockX() + ", " + p.getLocation().getBlockY() + ", " +
                p.getLocation().getBlockZ());
        StringBuilder b = new StringBuilder();
        Integer i = 0;
        for (Player pl : r.getOnlinePlayers()) {
            if (p instanceof Player && !p.canSee(pl)) {
                continue;
            }
            i++;
            if (!StringUtil.nullOrEmpty(b.toString())) {
                b.append(", ");
            }
            b.append(UC.getPlayer(pl).getDisplayName());
        }
        mt = mt.replace("{ONLINE}", b.toString());
        mt = mt.replace("{PLAYERCOUNT}", i + "");
        mt = mt.replace("{PLAYERS}", b.toString());
        mt = mt.replace("{PLAYERLIST}", b.toString());
        mt = mt.replace("{TIME}", DateFormat.getTimeInstance(2, Locale.getDefault()).format(new Date()));
        mt = mt.replace("{DATE}", DateFormat.getDateInstance(2, Locale.getDefault()).format(new Date()).replace("-", " "));
        mt = mt.replace("{TPS}", PerformanceUtil.getTps() + "");
        mt = mt.replace("{UPTIME}", ChatColor.stripColor(DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime())));
        mt = TabListener.replaceVariables(mt, p);
        StringBuilder pb = new StringBuilder();
        for (Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (!StringUtil.nullOrEmpty(pb.toString())) {
                pb.append(", ");
            }
            pb.append(pl.getDescription().getName());
        }
        mt = mt.replace("{PLUGINS}", pb.toString());
        mt = mt.replace("{VERSION}", Bukkit.getServer().getVersion());
        return mt;
    }

    public List<OfflinePlayer> getInTeleportMenuOffline() {
        List<OfflinePlayer> pls = new ArrayList<>();
        for (OfflinePlayer pl : r.getOfflinePlayers()) {
            if (UC.getPlayer(pl).isInTeleportMenu()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    public List<Player> getInTeleportMenuOnline() {
        List<Player> pls = new ArrayList<>();
        for (Player pl : r.getOnlinePlayers()) {
            if (UC.getPlayer(pl).isInTeleportMenu()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    public Map<UUID, UUID> getTeleportRequests() {
        return tp;
    }

    public Map<UUID, UUID> getTeleportHereRequests() {
        return tph;
    }

    public void addTeleportRequest(UUID u1, UUID u2) {
        tp.put(u1, u2);
    }

    public void addTeleportHereRequest(UUID u1, UUID u2) {
        tph.put(u1, u2);
    }

    public void removeTeleportRequest(UUID u) {
        tp.remove(u);
    }

    public void removeTeleportHereRequest(UUID u) {
        tph.remove(u);
    }

    //Vanish
    //Deaf
    public List<OfflinePlayer> getVanishOfflinePlayers() {
        List<OfflinePlayer> pls = new ArrayList<>();
        for (OfflinePlayer pl : r.getOfflinePlayers()) {
            if (UC.getPlayer(pl).isVanish()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    public List<Player> getVanishOnlinePlayers() {
        List<Player> pls = new ArrayList<>();
        for (Player pl : r.getOnlinePlayers()) {
            if (UC.getPlayer(pl).isVanish()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    public HashMap<String, Location> getWarps() {
        if (warps != null) {
            return warps;
        }
        warps = new HashMap<>();
        JsonConfig conf = new JsonConfig(UltimateFileLoader.Dwarps);
        if (!conf.contains("warps")) {
            return warps;
        }
        for (String hname : conf.listKeys("warps", false)) {
            warps.put(hname, LocationUtil.convertStringToLocation(conf.getString("warps." + hname)));
        }
        return warps;
    }

    public void setWarps(HashMap<String, Location> nh) {
        warps = nh;
        JsonConfig conf = new JsonConfig(UltimateFileLoader.Dwarps);
        conf.set("warps", null);
        for (String s : nh.keySet()) {
            try {
                conf.set("warps." + s, LocationUtil.convertLocationToString(nh.get(s.toLowerCase())));
            } catch (Exception ex) {
                r.log(r.negative + "Warp " + s + " has been removed. (Invalid location)");
            }
        }
        conf.save();
    }

    public ArrayList<String> getWarpNames() {
        ArrayList<String> h = new ArrayList<>();
        h.addAll(getWarps().keySet());
        return h;
    }

    public void addWarp(String s, Location l) {
        HashMap<String, Location> h = getWarps();
        h.put(s.toLowerCase(), l);
        setWarps(h);
    }

    public void removeWarp(String s) {
        HashMap<String, Location> h = getWarps();
        h.remove(s.toLowerCase());
        setWarps(h);
    }

    public Location getWarp(String s) {
        for (String w : getWarps().keySet()) {
            if (w.equalsIgnoreCase(s)) {
                return getWarps().get(w);
            }
        }
        return null;
    }

    public List<Player> getAfkPlayers() {
        List<Player> pls = new ArrayList<>();
        for (Player pl : r.getOnlinePlayers()) {
            if (UC.getPlayer(pl).isAfk()) {
                pls.add(pl);
            }
        }
        return pls;
    }

    public List<String> getAutomessageMessages() {
        return AutomessageListener.messages;
    }

    public String getAutomessageCurrentmessage() {
        return AutomessageListener.currentmessage;
    }

    public boolean isDebug() {
        return r.isDebug();
    }

    public void setDebug(Boolean value) {
        r.setDebug(value);
    }

    public JsonConfig getGlobalConfig() {
        return new JsonConfig(UltimateFileLoader.Dglobal);
    }

    public Location getGlobalSpawn() {
        if (!new JsonConfig(UltimateFileLoader.Dspawns).contains("global")) {
            return null;
        }
        String s = new JsonConfig(UltimateFileLoader.Dspawns).getString("global");
        Location loc = LocationUtil.convertStringToLocation(s);
        return loc;
    }

    public void setSpawn(Location loc, Boolean world, String group, Boolean firstjoin) {
        String path = "global";
        if (firstjoin) {
            path = "global.firstjoin";
        } else if (world && StringUtil.nullOrEmpty(group)) {
            path = "worlds.world." + loc.getWorld().getName() + ".global";
        } else if (!StringUtil.nullOrEmpty(group) && !world) {
            path = "global.group." + group;
        } else if (!StringUtil.nullOrEmpty(group) && world) {
            path = "worlds.world." + loc.getWorld().getName() + ".group." + group;
        }
        String s = LocationUtil.convertLocationToString(loc);
        JsonConfig conf = new JsonConfig(UltimateFileLoader.Dspawns);
        conf.set(path, s);
        conf.save();
    }

    public boolean delSpawn(World world, String group, Boolean firstjoin) {
        String path = "global";
        if (firstjoin) {
            path = "global.firstjoin";
        } else if (!StringUtil.nullOrEmpty(world) && StringUtil.nullOrEmpty(group)) {
            path = "worlds.world." + world.getName() + ".global";
        } else if (!StringUtil.nullOrEmpty(group) && StringUtil.nullOrEmpty(world)) {
            path = "global.group." + group;
        } else if (!StringUtil.nullOrEmpty(group) && !StringUtil.nullOrEmpty(world)) {
            path = "worlds.world." + world.getName() + ".group." + group;
        }
        JsonConfig conf = new JsonConfig(UltimateFileLoader.Dspawns);
        if (conf.contains(path)) {
            conf.set(path, null);
            conf.save();
            return true;
        } else {
            return false;
        }
    }

}
