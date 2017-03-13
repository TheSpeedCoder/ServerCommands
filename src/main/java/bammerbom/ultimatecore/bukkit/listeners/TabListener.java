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
import bammerbom.ultimatecore.bukkit.r;
import bammerbom.ultimatecore.bukkit.resources.utils.DateUtil;
import bammerbom.ultimatecore.bukkit.resources.utils.StringUtil;
import bammerbom.ultimatecore.bukkit.resources.utils.TabUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

public class TabListener implements Listener {

    static String defaultFormat = ChatColor.translateAlternateColorCodes('&', r.getCnfg().getString("Chat.Tab.DefaultFormat"));
    static HashMap<String, String> tabFormats = new HashMap<>();
    static String afkFormat = ChatColor.translateAlternateColorCodes('&', r.getCnfg().getString("Chat.Tab.AfkFormat"));
    static boolean headerFooterEnabled = r.getCnfg().getBoolean("Chat.Tab.HeaderFooterEnabled");
    static String header = ChatColor.translateAlternateColorCodes('&', r.getCnfg().isList("Chat.Tab.Header") ? StringUtil.join("\n", r.getCnfg().getStringList("Chat.Tab.Header")) : r
            .getCnfg().getString("Chat.Tab.Header"));
    static String footer = ChatColor.translateAlternateColorCodes('&', r.getCnfg().isList("Chat.Tab.Footer") ? StringUtil.join("\n", r.getCnfg().getStringList("Chat.Tab.Footer")) : r
            .getCnfg().getString("Chat.Tab.Footer"));
    static Boolean abovehead = r.getCnfg().getBoolean("Chat.Nametag.Enabled");
    static String defaultPrefix = r.getCnfg().getString("Chat.Nametag.DefaultPrefix");
    static String defaultSuffix = r.getCnfg().getString("Chat.Nametag.DefaultSuffix");
    static HashMap<String, String> prefixes = new HashMap<>();
    static HashMap<String, String> suffixes = new HashMap<>();

    public static void start() {
        if (!r.getCnfg().getBoolean("Chat.Tab.Enabled")) {
            return;
        }
        for (String key : r.getCnfg().getConfigurationSection("Chat.Tab.Groups").getValues(false).keySet()) {
            tabFormats.put(key, ChatColor.translateAlternateColorCodes('&', r.getCnfg().getString("Chat.Tab.Groups." + key)));
        }
        for (String key : r.getCnfg().getConfigurationSection("Chat.Nametag.Groups").getValues(false).keySet()) {
            prefixes.put(key, ChatColor.translateAlternateColorCodes('&', r.getCnfg().getString("Chat.Nametag.Groups." + key + ".Prefix")));
            suffixes.put(key, ChatColor.translateAlternateColorCodes('&', r.getCnfg().getString("Chat.Nametag.Groups." + key + ".Suffix")));
        }


        Bukkit.getPluginManager().registerEvents(new TabListener(), r.getUC());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(r.getUC(), new Runnable() {
            @SuppressWarnings("deprecation")
			@Override
            public void run() {
                for (Player p : r.getOnlinePlayers()) {
                    //Header and footer
                    if (headerFooterEnabled) {
                        TabUtil.sendTabTitle(p, replaceVariables(header, p), replaceVariables(footer, p));
                    }

                    //Player names
                    String group = "";
                    if (r.getVault() != null && r.getVault().getPermission() != null && r.getVault().getPermission().getPrimaryGroup(p) != null) {
                        group = r.getVault().getPermission().getPrimaryGroup(p) != null ? r.getVault().getPermission().getPrimaryGroup(p) : "";
                    }

                    //
                    String base = (!group.isEmpty() && tabFormats.containsKey(group)) ? tabFormats.get(group) : defaultFormat;
                    base = replaceVariables(base, p);
                    if (UC.getPlayer(p).isAfk()) {
                        base = afkFormat.replace("+Original", base);
                    }
                    p.setPlayerListName(base);
                    if (abovehead) {
                        Scoreboard board = p.getScoreboard();
                        if (board == null) {
                            board = Bukkit.getScoreboardManager().getNewScoreboard();
                        }
                        Team team = board.getTeam(p.getName());
                        if (team == null) {
                            team = board.registerNewTeam(p.getName());
                        }
                        String prefix = defaultPrefix;
                        if (prefixes.containsKey(group)) {
                            prefix = prefixes.get(group);
                        }
                        String suffix = defaultSuffix;
                        if (suffixes.containsKey(group)) {
                            suffix = suffixes.get(group);
                        }
                        team.addPlayer(p);
                        prefix = replaceVariables(prefix, p);
                        suffix = replaceVariables(suffix, p);
                        if (prefix.length() > 16) {
                            prefix = prefix.substring(0, 15);
                        }
                        if (suffix.length() > 16) {
                            suffix = suffix.substring(0, 15);
                        }
                        team.setPrefix(prefix);
                        team.setSuffix(suffix);
                        // Add a check for 1.8/1.8.8 users
                        if(Bukkit.getVersion().equals(1.8)){
                        	return;
                        } else {
                        	team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
                        }
                        for (Player pl : r.getOnlinePlayers()) {
                            pl.setScoreboard(board);
                        }
                    }
                }
            }
        }, 0L, 100L);
    }

    public static String replaceVariables(String base, Player p) {
        String name = "";
        name = p.getName();
        int maxplayers = Bukkit.getServer().getMaxPlayers();
        int onlineplayers = r.getOnlinePlayers().length;
        String group = "";
        String prefix = "";
        String suffix = "";
        if (r.getVault() != null && r.getVault().getPermission() != null && r.getVault().getPermission().getPrimaryGroup(p) != null) {
            group = r.getVault().getPermission().getPrimaryGroup(p) != null ? r.getVault().getPermission().getPrimaryGroup(p) : "";
            if (r.getVault().getChat() != null && !group.isEmpty()) {
                prefix = r.getVault().getChat().getGroupPrefix(p.getWorld(), r.getPrimaryGroup(p));
                suffix = r.getVault().getChat().getGroupSuffix(p.getWorld(), r.getPrimaryGroup(p));
                if ((r.getVault().getChat().getPlayerPrefix(p) != null) && !r.getVault().getChat().getPlayerPrefix(p).isEmpty()) {
                    prefix = r.getVault().getChat().getPlayerPrefix(p);
                }
                if ((r.getVault().getChat().getPlayerSuffix(p) != null) && !r.getVault().getChat().getPlayerSuffix(p).isEmpty()) {
                    suffix = r.getVault().getChat().getPlayerSuffix(p);
                }
            }
        }
        String displayname = UC.getPlayer(p).getDisplayName();
        base = base.replace("+Group", group);
        base = base.replace("+Prefix", prefix);
        base = base.replace("+Suffix", suffix);
        base = base.replace("+Name", name);
        base = base.replace("+Displayname", displayname);
        base = base.replace("+Maxplayers", maxplayers + "");
        base = base.replace("+Onlineplayers", onlineplayers + "");
        base = base.replace("+Uptime", ChatColor.stripColor(DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime())));
        base = ChatColor.translateAlternateColorCodes('&', base);
        return base;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //Format
        Player p = e.getPlayer();
        //Header and footer
        if (headerFooterEnabled) {
            TabUtil.sendTabTitle(p, replaceVariables(header, p), replaceVariables(footer, p));
        }

        //Player names
        String group = "";
        if (r.getVault() != null && r.getVault().getPermission() != null && r.getVault().getPermission().getPrimaryGroup(p) != null) {
            group = r.getVault().getPermission().getPrimaryGroup(p) != null ? r.getVault().getPermission().getPrimaryGroup(p) : "";
        }

        //
        String base = (!group.isEmpty() && tabFormats.containsKey(group)) ? tabFormats.get(group) : defaultFormat;
        base = replaceVariables(base, p);
        if (!p.getPlayerListName().equals(base)) {
            p.setPlayerListName(base);
        }
    }
}
