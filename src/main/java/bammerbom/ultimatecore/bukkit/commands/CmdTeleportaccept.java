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
package bammerbom.ultimatecore.bukkit.commands;

import bammerbom.ultimatecore.bukkit.UltimateCommand;
import bammerbom.ultimatecore.bukkit.api.UC;
import bammerbom.ultimatecore.bukkit.r;
import bammerbom.ultimatecore.bukkit.resources.utils.LocationUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdTeleportaccept implements UltimateCommand {

    @Override
    public String getName() {
        return "teleportaccept";
    }

    @Override
    public String getPermission() {
        return "uc.teleportaccept";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tpaccept", "tpyes");
    }

    @Override
    public void run(final CommandSender cs, String label, String[] args) {
        if (!r.isPlayer(cs)) {
            return;
        }
        if (!r.perm(cs, "uc.teleportaccept", true, true)) {
            return;
        }
        Player p = (Player) cs;
        if (UC.getServer().getTeleportHereRequests().containsKey(p.getUniqueId())) {
            Player t = r.searchPlayer(UC.getServer().getTeleportHereRequests().get(p.getUniqueId()));
            if (t == null) {
                r.sendMes(p, "teleportaskNoRequests");
            } else {
                LocationUtil.teleport(p, t, TeleportCause.COMMAND, true, true);
                r.sendMes(cs, "teleportaskhereAcceptSender", "%Player", t.getName());
                r.sendMes(t, "teleportaskhereAcceptTarget", "%Player", r.getDisplayName(p));
                UC.getServer().removeTeleportHereRequest(t.getUniqueId());
            }
        } else if (!UC.getServer().getTeleportRequests().containsKey(p.getUniqueId())) {
            r.sendMes(p, "teleportaskNoRequests");
        } else {
            Player t = r.searchPlayer(UC.getServer().getTeleportRequests().get(p.getUniqueId()));
            if (t == null) {
                r.sendMes(p, "teleportaskNoRequests");
            } else {
                LocationUtil.teleport(t, p, TeleportCause.COMMAND, true, true);
                r.sendMes(cs, "teleportaskAcceptSender", "%Player", t.getName());
                r.sendMes(t, "teleportaskAcceptTarget", "%Player", r.getDisplayName(p));
                UC.getServer().removeTeleportRequest(p.getUniqueId());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmd, String alias, String[] args, String curs, Integer curn) {
        return new ArrayList<>();
    }
}
