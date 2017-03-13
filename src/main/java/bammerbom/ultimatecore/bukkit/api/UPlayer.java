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

import bammerbom.ultimatecore.bukkit.JsonConfig;
import bammerbom.ultimatecore.bukkit.UltimateFileLoader;
import bammerbom.ultimatecore.bukkit.r;
import bammerbom.ultimatecore.bukkit.resources.utils.InventoryUtil;
import bammerbom.ultimatecore.bukkit.resources.utils.LocationUtil;
import bammerbom.ultimatecore.bukkit.resources.utils.TitleUtil;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.io.File;
import java.util.*;

public class UPlayer {
    static Random ra = new Random();
    String name = null;
    UUID uuid = null;
    Location lastLocation = null;
    HashMap<String, Location> homes = null;
    UUID onlineInv = null;
    UUID offlineInv = null;
    Boolean spy = null;
    String nickname = null;
    HashMap<Material, List<String>> pts = null;
    Boolean vanish = null;
    Long vanishtime = null;
    Long lastconnect = null;
    Boolean inTeleportMenu = false;
    Boolean teleportEnabled = null;
    boolean afk = false;
    long lastaction = System.currentTimeMillis();
    String lastip;
    String lasthostname = null;
    String afkmessage = null;

    public UPlayer(OfflinePlayer p) {
        name = p.getName();
        uuid = p.getUniqueId();
    }

    public UPlayer(UUID uuid) {
        OfflinePlayer p = r.searchOfflinePlayer(uuid);
        name = p.getName();
        this.uuid = p.getUniqueId();
    }

    private void save() {
        UC.uplayers.remove(this);
        UC.uplayers.add(this);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public Player getOnlinePlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String getAfkMessage() {
        return afkmessage;
    }

    public void setAfkMessage(String mes) {
        afkmessage = mes;
    }

    public long getLastConnectMillis() {
        if (lastconnect != null) {
            return lastconnect;
        }
        final JsonConfig conf = getPlayerConfig();
        if (conf.get("lastconnect") != null) {
            lastconnect = conf.getLong("lastconnect");
            save();
            return lastconnect;
        } else {
            lastconnect = getPlayer().getLastPlayed();
            save();
            return getPlayer().getLastPlayed();
        }
    }

    public void updateLastConnectMillis() {
        lastconnect = System.currentTimeMillis();
        final JsonConfig conf = getPlayerConfig();
        conf.set("lastconnect", System.currentTimeMillis());
        conf.save();
        save();
    }

    public void updateLastConnectMillis(Long millis) {
        lastconnect = millis;
        final JsonConfig conf = getPlayerConfig();
        conf.set("lastconnect", millis);
        conf.save();
        save();
    }

    public String getLastIp() {
        if (lastip != null) {
            return lastip;
        }
        final JsonConfig conf = getPlayerConfig();
        if (conf.get("ip") != null) {
            lastip = conf.getString("ip");
            save();
            return lastip;
        } else {
            if (getPlayer().isOnline()) {
                setLastIp(getOnlinePlayer().getAddress().toString().split("/")[1].split(":")[0]);
                return lastip;
            }
            return null;
        }
    }

    public void setLastIp(String ip) {
        lastip = ip;
        final JsonConfig conf = getPlayerConfig();
        conf.set("ip", ip);
        conf.save();
        save();
    }

    public String getLastHostname() {
        if (lasthostname != null) {
            return lasthostname;
        }
        final JsonConfig conf = getPlayerConfig();
        if (conf.get("hostname") != null) {
            lastip = conf.getString("hostname");
            save();
            return lastip;
        } else {
            if (getPlayer().isOnline()) {
                setLastHostname(getOnlinePlayer().getAddress().getHostName());
                return lastip;
            }
            return null;
        }
    }

    public void setLastHostname(String ip) {
        lasthostname = ip;
        final JsonConfig conf = getPlayerConfig();
        conf.set("hostname", ip);
        conf.save();
        save();
    }

    //Configuration
    public JsonConfig getPlayerConfig() {
        return UltimateFileLoader.getPlayerConfig(getPlayer());
    }

    public File getPlayerFile() {
        return UltimateFileLoader.getPlayerFile(getPlayer());
    }

    public void setLastLocation() {
        if (!getPlayer().isOnline()) {
            return;
        }
        setLastLocation(getOnlinePlayer().getLocation());
    }

    public Location getLastLocation() {
        if (lastLocation == null) {
            if (!getPlayerConfig().contains("lastlocation")) {
                return null;
            }
            Location loc = LocationUtil.convertStringToLocation(getPlayerConfig().getString("lastlocation"));
            lastLocation = loc;
            save();
            return loc;
        }
        return lastLocation;
    }

    public void setLastLocation(Location loc) {
        lastLocation = loc;
        JsonConfig conf = getPlayerConfig();
        conf.set("lastlocation", loc == null ? null : LocationUtil.convertLocationToString(loc));
        conf.save();
        save();
    }

    public HashMap<String, Location> getHomes() {
        if (homes != null) {
            return homes;
        }
        homes = new HashMap<>();
        JsonConfig conf = getPlayerConfig();
        if (!conf.contains("homes")) {
            return homes;
        }
        for (String hname : conf.listKeys("homes", false)) {
            try {
                homes.put(hname, LocationUtil.convertStringToLocation(conf.getString("homes." + hname)));
            } catch (Exception ex) {
                r.log(r.negative + "Home " + getPlayer().getName() + ":" + hname + " has been removed. (Invalid location)");
            }
        }
        save();
        return homes;
    }

    public void setHomes(HashMap<String, Location> nh) {
        homes = nh;
        save();
        JsonConfig conf = getPlayerConfig();
        conf.set("homes", null);
        for (String s : nh.keySet()) {
            try {
                conf.set("homes." + s.toLowerCase(), LocationUtil.convertLocationToString(nh.get(s)));
            } catch (Exception ex) {
                r.log("Invalid home: " + getPlayer().getName() + "/" + s);
            }
        }
        conf.save();
    }

    public ArrayList<String> getHomeNames() {
        ArrayList<String> h = new ArrayList<>();
        h.addAll(getHomes().keySet());
        return h;
    }

    public void addHome(String s, Location l) {
        HashMap<String, Location> h = getHomes();
        h.put(s.toLowerCase(), l);
        setHomes(h);
    }

    public void removeHome(String s) {
        HashMap<String, Location> h = getHomes();
        h.remove(s.toLowerCase());
        setHomes(h);
    }

    public Location getHome(String s) {
        return getHomes().get(s.toLowerCase());
    }

    public boolean isInOnlineInventory() {
        return onlineInv != null;
    }

    public Player getInOnlineInventory() {
        if (onlineInv == null) {
            return null;
        }
        return r.searchPlayer(onlineInv);
    }

    public void setInOnlineInventory(Player p) {
        if (p == null) {
            onlineInv = null;
        } else {
            onlineInv = p.getUniqueId();
        }
        save();
    }

    public boolean isInOfflineInventory() {
        return offlineInv != null;
    }

    public OfflinePlayer getInOfflineInventory() {
        if (offlineInv == null) {
            return null;
        }
        return r.searchOfflinePlayer(offlineInv);
    }


    public void setInOfflineInventory(OfflinePlayer p) {
        if (p == null) {
            offlineInv = null;
        } else {
            offlineInv = p.getUniqueId();
        }
        save();
    }

    public void updateLastInventory() {
        JsonConfig conf = getPlayerConfig();
        conf.set("lastinventory", InventoryUtil.convertInventoryToString(getOnlinePlayer().getInventory()));
        conf.save();
    }

    public Inventory getLastInventory() {
        JsonConfig conf = getPlayerConfig();
        if (!conf.contains("lastinventory")) {
            return null;
        }
        return InventoryUtil.convertStringToInventory(conf.getString("lastinventory"), r.mes("inventoryOfflineTitle", "%Name", name));
    }

    public boolean isSpy() {
        if (spy != null) {
            return spy;
        }
        if (!getPlayerConfig().contains("spy")) {
            return false;
        }
        spy = getPlayerConfig().getBoolean("spy");
        save();
        return spy;
    }

    public void setSpy(Boolean sp) {
        spy = sp;
        JsonConfig conf = getPlayerConfig();
        conf.set("spy", sp);
        conf.save();
        save();
    }

    public String getDisplayName() {
        if (getNick() != null) {
            return getNick();
        }
        if (getPlayer().isOnline()) {
            if (getOnlinePlayer().getDisplayName() != null) {
                return getOnlinePlayer().getDisplayName();
            }
        }
        return getPlayer().getName();
    }

    public String getNick() {
        if (nickname != null) {
            return nickname;
        }
        JsonConfig data = getPlayerConfig();
        if (data.get("nick") == null) {
            return null;
        }
        String nick = ChatColor.translateAlternateColorCodes('&', data.getString("nick"));
        if (getPlayer().isOnline()) {
            getPlayer().getPlayer().setDisplayName(nick.replace("&y", ""));
        }
        if (getPlayer().isOnline() && r.perm((CommandSender) getPlayer(), "uc.chat.rainbow", false, false)) {
            nick = nick.replaceAll("&y", r.getRandomChatColor() + "");
        }
        nickname = nick + ChatColor.RESET;
        save();
        return nick + ChatColor.RESET;
    }

    public void setNick(String str) {
        nickname = str == null ? null : str + ChatColor.RESET;
        save();
        if (str != null) {
            if (getPlayer().isOnline()) {
                getPlayer().getPlayer().setDisplayName(nickname.replace("&y", ""));
            }
        } else {
            if (getPlayer().isOnline()) {
                getPlayer().getPlayer().setDisplayName(getPlayer().getPlayer().getName());
            }
        }
        JsonConfig data = getPlayerConfig();
        data.set("nick", str);
        data.save(UltimateFileLoader.getPlayerFile(getPlayer()));
    }

    public boolean isInTeleportMenu() {
        return inTeleportMenu;
    }

    public void setInTeleportMenu(Boolean b) {
        inTeleportMenu = b;
        save();
    }

    public boolean hasTeleportEnabled() {
        if (teleportEnabled != null) {
            return teleportEnabled;
        }
        if (!getPlayerConfig().contains("teleportenabled")) {
            return true;
        }
        teleportEnabled = getPlayerConfig().getBoolean("teleportenabled");
        save();
        return teleportEnabled;
    }

    public void setTeleportEnabled(Boolean tpe) {
        teleportEnabled = tpe;
        JsonConfig conf = getPlayerConfig();
        conf.set("teleportenabled", tpe);
        conf.save();
        save();
    }

    public boolean isVanish() {
        if (getVanishTime() >= 1 && getVanishTimeLeft() <= 1 && (vanish != null ? vanish : getPlayerConfig().getBoolean("vanish"))) {
            setVanish(false);
            if (getPlayer().isOnline()) {
                r.sendMes(getOnlinePlayer(), "unvanishTarget");
            }
            return false;
        }
        if (vanish != null) {
            return vanish;
        }
        if (!getPlayerConfig().contains("vanish")) {
            vanish = false;
            save();
            return false;
        }
        vanish = getPlayerConfig().getBoolean("vanish");
        save();
        return getPlayerConfig().getBoolean("vanish");
    }

    public void setVanish(Boolean fr) {
        setVanish(fr, -1L);
    }

    public Long getVanishTime() {
        if (vanishtime != null) {
            return vanishtime;
        }
        if (!getPlayerConfig().contains("vanishtime")) {
            vanishtime = 0L;
            save();
            return 0L;
        }
        vanishtime = getPlayerConfig().getLong("vanishtime");
        save();
        return getPlayerConfig().getLong("vanishtime");

    }

    public Long getVanishTimeLeft() {
        return getVanishTime() - System.currentTimeMillis();
    }

    public void setVanish(Boolean fr, Long time) {
        JsonConfig conf = getPlayerConfig();
        if (vanishtime == null || vanishtime == 0L) {
            vanishtime = -1L;
        }
        if (time >= 1) {
            time = time + System.currentTimeMillis();
        }
        conf.set("vanish", fr);
        conf.set("vanishtime", time);
        conf.save();
        vanish = fr;
        vanishtime = fr ? time : 0L;
        if (getOnlinePlayer() != null) {
            for (Player pl : r.getOnlinePlayers()) {
                if (fr) {
                    pl.hidePlayer(getOnlinePlayer());
                } else {
                    pl.showPlayer(getOnlinePlayer());
                }
            }
        }
        save();
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean news) {
        if (news == false && getPlayer().isOnline()) {
            TitleUtil.clearTitle(getOnlinePlayer());
        }
        afk = news;
        save();
    }

    public long getLastActivity() {
        return lastaction;
    }

    public void setLastActivity(Long last) {
        lastaction = last;
        save();
    }

    public void updateLastActivity() {
        setLastActivity(System.currentTimeMillis());
        if (isAfk()) {
            setAfk(false);
            Bukkit.broadcastMessage(r.mes("afkUnafk", "%Player", UC.getPlayer(getPlayer()).getDisplayName()));
        }
    }

    public Location getSpawn(Boolean firstjoin) {
        JsonConfig conf = new JsonConfig(UltimateFileLoader.Dspawns);
        String loc;
        Player p = r.searchPlayer(uuid);
        Boolean world = conf.contains("worlds.world." + p.getWorld().getName() + ".global");
        String world_ = world ? conf.getString("worlds.world." + p.getWorld().getName() + ".global") : null;
        Boolean group = (r.getVault() != null && r.getVault().getPermission() != null && r.getPrimaryGroup(p) != null) && conf.contains("global.group." + r.getPrimaryGroup(p));
        String group_ = r.getVault() != null && r.getVault().getPermission() != null && r.getPrimaryGroup(p) != null ? (group ? conf.getString("global.group." + r.getPrimaryGroup(p)) :
                null) : null;
        Boolean gw = (r.getVault() != null && r.getVault().getPermission() != null && r.getPrimaryGroup(p) != null) && conf.contains("worlds.world." + p.getWorld().getName() + ".group." +
                r.getPrimaryGroup(p));
        String gw_ = r.getVault() != null && r.getVault().getPermission() != null && r.getPrimaryGroup(p) != null ? conf.getString("worlds.world." + p.getWorld().getName() + ".group." + r
                .getPrimaryGroup(p)) : null;
        if (firstjoin && conf.contains("global.firstjoin")) {
            loc = conf.getString("global.firstjoin");
        } else if (gw) {
            loc = gw_;
        } else if (world && group) {
            if (r.getCnfg().getBoolean("Command.Spawn.WorldOrGroup")) {
                loc = world_;
            } else {
                loc = group_;
            }
        } else if (world) {
            loc = world_;
        } else if (group) {
            loc = group_;
        } else if (conf.contains("global")) {
            loc = conf.getString("global");
        } else {
            return null;
        }
        return LocationUtil.convertStringToLocation(loc);
    }


}
