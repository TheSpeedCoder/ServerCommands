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
package bammerbom.ultimatecore.bukkit.listeners;

import bammerbom.ultimatecore.bukkit.api.UC;
import bammerbom.ultimatecore.bukkit.ErrorLogger;
import bammerbom.ultimatecore.bukkit.JsonConfig;
import bammerbom.ultimatecore.bukkit.r;
import bammerbom.ultimatecore.bukkit.resources.utils.LocationUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.EventExecutor;

public class GlobalPlayerListener implements Listener {
    static boolean spawnOnJoin = r.getCnfg().getBoolean("SpawnOnJoin", false);

    public static void start() {
        final GlobalPlayerListener gpl = new GlobalPlayerListener();
        Bukkit.getPluginManager().registerEvents(gpl, r.getUC());
        EventPriority p;
        String s = r.getCnfg().getString("Command.Spawn.Priority");
        if (s.equalsIgnoreCase("lowest")) {
            p = EventPriority.LOWEST;
        } else if (s.equalsIgnoreCase("high")) {
            p = EventPriority.HIGH;
        } else if (s.equalsIgnoreCase("highest")) {
            p = EventPriority.HIGHEST;
        } else {
            r.log("Spawn priority is invalid.");
            return;
        }
        Bukkit.getPluginManager().registerEvent(PlayerRespawnEvent.class, gpl, p, new EventExecutor() {
            @Override
            public void execute(Listener l, Event e) throws EventException {
                gpl.onRespawn((PlayerRespawnEvent) e);
            }
        }, r.getUC());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent e) {
        try {
            //Back
            if (e.getCause().equals(TeleportCause.COMMAND)) {
                UC.getPlayer(e.getPlayer()).setLastLocation(e.getFrom());
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: PlayerTeleportEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        try {
            //Spawn on join
            if (spawnOnJoin && UC.getPlayer(e.getPlayer()).getSpawn(false) != null) {
                LocationUtil.teleportUnsafe(e.getPlayer(), UC.getPlayer(e.getPlayer()).getSpawn(false), TeleportCause.PLUGIN, false);
            }
            //Inventory
            UC.getPlayer(e.getPlayer()).updateLastInventory();
            //Lastconnect
            UC.getPlayer(e.getPlayer()).updateLastConnectMillis();
            //Lastip
            UC.getPlayer(e.getPlayer()).setLastIp(e.getPlayer().getAddress().toString().split("/")[1].split(":")[0]);
            UC.getPlayer(e.getPlayer()).setLastHostname(e.getPlayer().getAddress().getHostName());
            //Vanish
            for (Player p : UC.getServer().getVanishOnlinePlayers()) {
                e.getPlayer().hidePlayer(p);
            }
            //Name changes
            if (UC.getPlayer(e.getPlayer()).getPlayerConfig().contains("oldname")) {
                JsonConfig conf = UC.getPlayer(e.getPlayer()).getPlayerConfig();
                r.sendMes(e.getPlayer(), "nameChanged", "%Oldname", conf.getString("oldname"), "%Newname", e.getPlayer().getName());
                conf.set("oldname", null);
                conf.save();
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: PlayerJoinEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent e) {
        try {
            //Inventory
            if (UC.getPlayer(e.getPlayer()).isInOfflineInventory()) {
                UC.getPlayer(e.getPlayer()).setInOfflineInventory(null);
            }
            if (UC.getPlayer(e.getPlayer()).isInOnlineInventory()) {
                UC.getPlayer(e.getPlayer()).setInOnlineInventory(null);
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (UC.getPlayer(p).getInOnlineInventory() != null && UC.getPlayer(p).getInOnlineInventory().equals(e.getPlayer().getUniqueId())) {
                    p.closeInventory();
                    UC.getPlayer(p).setInOnlineInventory(null);
                }
            }
            UC.getPlayer(e.getPlayer()).updateLastInventory();
            //Last connect
            UC.getPlayer(e.getPlayer()).updateLastConnectMillis();
            //Vanish
            for (Player p : UC.getServer().getVanishOnlinePlayers()) {
                e.getPlayer().showPlayer(p);
            }
            if (UC.getPlayer(e.getPlayer()).isVanish()) {
                for (Player p : r.getOnlinePlayers()) {
                    p.showPlayer(e.getPlayer());
                }
            }
            //Fly
            if (e.getPlayer().isFlying()) {
                LocationUtil.teleport(e.getPlayer(), e.getPlayer().getLocation(), TeleportCause.PLUGIN, true, false);
                e.getPlayer().setFallDistance(0.0F);
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: PlayerQuitEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClose(InventoryCloseEvent e) {
        try {
            //Inventory
            if (UC.getPlayer(e.getPlayer().getUniqueId()).isInOfflineInventory()) {
                UC.getPlayer(e.getPlayer().getUniqueId()).setInOfflineInventory(null);
            }
            if (UC.getPlayer(e.getPlayer().getUniqueId()).isInOnlineInventory()) {
                UC.getPlayer(e.getPlayer().getUniqueId()).setInOnlineInventory(null);
            }
            //Teleportmenu
            if (UC.getPlayer(e.getPlayer().getUniqueId()).isInTeleportMenu()) {
                UC.getPlayer(e.getPlayer().getUniqueId()).setInTeleportMenu(false);
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: InventoryCloseEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(final InventoryClickEvent e) {
        try {
            //Inventory
            if (UC.getPlayer(e.getWhoClicked().getUniqueId()).isInOfflineInventory()) {
                e.setCancelled(true);
            }
            if (UC.getPlayer(e.getWhoClicked().getUniqueId()).isInOnlineInventory()) {
                if (!r.perm(e.getWhoClicked(), "uc.inventory.edit", false, true)) {
                    e.setCancelled(true);
                }
            }
            //Teleportmenu
            if (UC.getPlayer(e.getWhoClicked().getUniqueId()).isInTeleportMenu() && e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem()
                    .getItemMeta().hasDisplayName()) {
                UC.getPlayer(e.getWhoClicked().getUniqueId()).setInTeleportMenu(false);
                Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "tp " + ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: InventoryClickEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent e) {
        try {
            //Back
            if (r.perm(e.getEntity(), "uc.back.death", true, false)) {
                UC.getPlayer(e.getEntity()).setLastLocation();
                r.sendMes(e.getEntity(), "backDeathMessage");
            } else {
                UC.getPlayer(e.getEntity()).setLastLocation(null);
            }

            //
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: PlayerMoveEvent");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        try {
            //Gamemode
            if (UC.getWorld(e.getPlayer().getWorld()).getDefaultGamemode() != null && !r.perm(e.getPlayer(), "uc.world.flag.override", false, false)) {
                e.getPlayer().setGameMode(UC.getWorld(e.getPlayer().getWorld()).getDefaultGamemode());
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: PlayerChangedWorldEvent");
        }
    }

    public void onRespawn(PlayerRespawnEvent e) {
        try {
            if (e.getPlayer().getBedSpawnLocation() != null) {
                e.setRespawnLocation(e.getPlayer().getBedSpawnLocation());
            } else if (UC.getPlayer(e.getPlayer()).getSpawn(false) != null) {
                e.setRespawnLocation(UC.getPlayer(e.getPlayer()).getSpawn(false));
            }
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to handle event: PlayerRespawnEvent");
        }
    }

}
