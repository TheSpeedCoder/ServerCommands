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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class ErrorLogger {
    static StringWriter writer = null;
    static Long countdown = null;

    public static void log(final Throwable t, final String s) {
        String error = ExceptionUtils.getFullStackTrace(t);
        if (error.contains("java.lang.UnsupportedOperationException: SuperPerms no group permissions.")) {
            r.log("ERROR: Your permissions plugin '" + r.getVault().getPermission().getName() + "' does not support group permissions.");
            return;
        }
        final String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(Calendar.getInstance().getTime());
        //CONSOLE
        r.log(" ");
        r.log(ChatColor.DARK_RED + "=========================================================");
        r.log(ChatColor.RED + "ServerCommands has run into an error, Include the file");
        r.log(ChatColor.YELLOW + "plugins/ServerCommands/Errors/" + time + ".txt ");
        r.log(ChatColor.RED + "Please add the the file in the issue page on github");
        r.log(ChatColor.RED + "Bukkit version: " + Bukkit.getServer().getVersion());
         r.log(ChatColor.RED + "ServerCommands version: " + Bukkit.getPluginManager().getPlugin("ServerCommands").getDescription().getVersion());
         r.log(ChatColor.RED + "Plugins loaded (" + Bukkit.getPluginManager().getPlugins().length + "): " + Arrays.asList(Bukkit.getPluginManager().getPlugins()));
         r.log(ChatColor.RED + "Java version: " + System.getProperty("java.version"));
         r.log(ChatColor.RED + "Error message: " + t.getMessage());
         r.log(ChatColor.RED + "ServerCommands message: " + s);
        r.log(ChatColor.DARK_RED + "=========================================================");
        if (t instanceof Exception) {
            r.log(ChatColor.RED + "Stacktrace: ");
            t.printStackTrace();
            r.log(ChatColor.DARK_RED + "=========================================================");
            r.log(" ");
        }
    }
}