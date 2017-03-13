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
package bammerbom.ultimatecore.bukkit;

import bammerbom.ultimatecore.bukkit.commands.*;
import bammerbom.ultimatecore.bukkit.resources.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UltimateCommands implements TabCompleter {

    public static List<UltimateCommand> cmds = new ArrayList<UltimateCommand>();
    public static List<String> disabled;
    public static UltimateCommands ucmds;

    public static void load() {
        disabled = r.getCnfg().getStringList("Command.DisabledCommands");
        cmds.add(new CmdAfk());
        cmds.add(new CmdBack());
        cmds.add(new CmdClear());
        cmds.add(new CmdCoordinates());
        cmds.add(new CmdDelhome());
        cmds.add(new CmdDelspawn());
        cmds.add(new CmdDelwarp());
        cmds.add(new CmdFeed());
        cmds.add(new CmdFly());
        cmds.add(new CmdGamemode());
        cmds.add(new CmdGive());
        cmds.add(new CmdHat());
        cmds.add(new CmdHeal());
        cmds.add(new CmdHelp());
        cmds.add(new CmdHome());
        cmds.add(new CmdHunger());
        cmds.add(new CmdInventory());
        cmds.add(new CmdItem());
        cmds.add(new CmdKill());
        cmds.add(new CmdKillall());
        cmds.add(new CmdMoney());
        cmds.add(new CmdMotd());
        cmds.add(new CmdNames());
        cmds.add(new CmdNear());
        cmds.add(new CmdNick());
        cmds.add(new CmdPay());
        cmds.add(new CmdPing());
        cmds.add(new CmdRealname());
        cmds.add(new CmdRemoveall());
        cmds.add(new CmdRepair());
        cmds.add(new CmdRules());
        cmds.add(new CmdSave());
        cmds.add(new CmdSethome());
        cmds.add(new CmdSetspawn());
        cmds.add(new CmdSetwarp());
        cmds.add(new CmdSkull());
        cmds.add(new CmdSpawn());
        cmds.add(new CmdSpy());
        cmds.add(new CmdTeleport());
        cmds.add(new CmdTeleportaccept());
        cmds.add(new CmdTeleportall());
        cmds.add(new CmdTeleportask());
        cmds.add(new CmdTeleportaskall());
        cmds.add(new CmdTeleportaskhere());
        cmds.add(new CmdTeleportdeny());
        cmds.add(new CmdTeleporthere());
        cmds.add(new CmdTeleporttoggle());
        cmds.add(new CmdTime());
        cmds.add(new CmdTop());
        cmds.add(new CmdUptime());
        cmds.add(new CmdVanish());
        cmds.add(new CmdWarp());
        cmds.add(new CmdWeather());
        cmds.add(new CmdWorld());
        //
        ucmds = new UltimateCommands();
        //
        for (UltimateCommand cmd : cmds) {
            if (Bukkit.getPluginCommand("ultimatecore:" + cmd.getName()) == null) {
                r.log("Failed to load command: " + cmd.getName());
                continue;
            }
            Bukkit.getPluginCommand("ultimatecore:" + cmd.getName()).setTabCompleter(ucmds);
        }
    }

    public static void onCmd(final CommandSender sender, Command cmd, String label, final String[] args) {
        if (label.startsWith("ultimatecore:")) {
            label = label.replaceFirst("ultimatecore:", "");
        }
        for (UltimateCommand cmdr : cmds) {
            if (label.equals(cmdr.getName()) || cmdr.getAliases().contains(label)) {
                if (disabled.contains(cmdr.getName())) {
                    r.sendMes(sender, "commandDisabled");
                    return;
                }
                for (String a : cmdr.getAliases()) {
                    if (disabled.contains(a)) {
                        r.sendMes(sender, "commandDisabled");
                        return;
                    }
                }
                cmdr.run(sender, label, args);
                break;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> rtrn = null;
        if (label.startsWith("ultimatecore:")) {
            label = label.replaceFirst("ultimatecore:", "");
        }
        for (UltimateCommand cmdr : cmds) {
            if (cmdr.getName().equals(label) || cmdr.getAliases().contains(label)) {
                if (disabled.contains(cmdr.getName())) {
                    r.sendMes(sender, "commandDisabled");
                    return new ArrayList<>();
                }
                for (String a : cmdr.getAliases()) {
                    if (disabled.contains(a)) {
                        r.sendMes(sender, "commandDisabled");
                        return new ArrayList<>();
                    }
                }
                try {
                    if (!r.perm(sender, cmdr.getPermission(), false, true)) {
                        return new ArrayList<>();
                    }
                    rtrn = cmdr.onTabComplete(sender, cmd, label, args, args[args.length - 1], args.length - 1);
                } catch (Exception ex) {
                    ErrorLogger.log(ex, "Failed tabcompleting for " + label);
                }
                break;
            }
        }
        if (rtrn == null) {
            rtrn = new ArrayList<>();
            for (Player p : r.getOnlinePlayers()) {
                rtrn.add(p.getName());
            }
        }
        ArrayList<String> rtrn2 = new ArrayList<>();
        rtrn2.addAll(rtrn);
        rtrn = rtrn2;
        if (!StringUtil.nullOrEmpty(args[args.length - 1])) {
            List<String> remv = new ArrayList<>();
            for (String s : rtrn) {
                if (!StringUtils.startsWithIgnoreCase(s, args[args.length - 1])) {
                    remv.add(s);
                }
            }
            rtrn.removeAll(remv);
        }
        return rtrn;
    }

}
