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
package bammerbom.ultimatecore.bukkit.resources.utils;

import bammerbom.ultimatecore.bukkit.ErrorLogger;
import bammerbom.ultimatecore.bukkit.JsonConfig;
import bammerbom.ultimatecore.bukkit.r;

import com.google.common.collect.ImmutableList;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Callable;

public class UuidUtil {

    public static Map<Long, String> getNameHistory(UUID u) throws Exception {
        //https://api.mojang.com/user/profiles/27d51a021cec49fa8afa4a6aff0e5b0a/names
        String uuid = u.toString().replace("-", "");
        //Webb web = Webb.create();

        URL url = new URL("https://api.mojang.com/user/profiles/" + uuid + "/names");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String response = in.readLine();
        in.close();

        Object obj = JSONValue.parse(response);
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            TreeMap<Long, String> map = new TreeMap<>();
            for (Object ob : array) {
                if (ob instanceof JSONObject) {
                    JSONObject job = (JSONObject) ob;
                    String name = job.get("name").toString();
                    Long time = job.containsKey("changedToAt") ? Long.parseLong(job.get("changedToAt").toString()) : -1L;
                    map.put(time, name);
                } else {
                    r.log("Response " + ob.toString() + " was not a JSONObject.");
                }
            }
           /* Map<String, Long> items = new TreeMap<String, Long>(Collections.reverseOrder());
            items.putAll(map);
            return items;*/
            return map;
        } else {
            r.log("Response " + obj.toString() + " was not a JSONArray.");
            return null;
        }
    }

    //
    public static void loadPlayers() {
        File directory = new File(r.getUC().getDataFolder() + File.separator + "Players");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        ArrayList<UUID> request = null;
        for (OfflinePlayer p : r.getOfflinePlayers()) {
            if (p.getUniqueId() == null) {
                continue;
            }
            final File file = new File(r.getUC().getDataFolder() + File.separator + "Players" + File.separator + p.getUniqueId() + ".json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    ErrorLogger.log(e, "Failed to create new file for " + p.getUniqueId());
                }
            }
            JsonConfig conf = new JsonConfig(file);
            if (p.getName() == null) {
                if (!conf.contains("name")) {
                    if (request == null) {
                        request = new ArrayList<>();
                    }
                    request.add(p.getUniqueId());
                }
            } else {
                if (!conf.contains("name")) {
                    conf.set("name", p.getName());
                    conf.save();
                } else {
                    if (!conf.getString("name").equals(p.getName())) {
                        String oldname = conf.getString("name");
                        conf.set("name", p.getName());
                        if (p.isOnline()) {
                            r.sendMes((CommandSender) p, "nameChanged", "%Oldname", oldname, "%Newname", p.getName());
                        } else {
                            conf.set("oldname", oldname);
                        }
                        conf.save();
                    }
                }
            }
        }
        if (request != null) {
            final ArrayList<UUID> req = request;
            try {
                r.log("Starting playerfile update...");
                HashMap<UUID, String> s = new UuidToName(req).call();
                for (UUID u : s.keySet()) {
                    String n = s.get(u);
                    File f = new File(r.getUC().getDataFolder() + File.separator + "Players" + File.separator + u + "" +
                            ".json");
                    JsonConfig conf = new JsonConfig(f);
                    conf.set("name", n);
                    conf.save();
                }
                r.log("Playerfile update complete.");
            } catch (Exception e) {
                ErrorLogger.log(e, "Failed to convert uuids to names.");
            }
        }

    }

    public static class UuidToName implements Callable<Map<UUID, String>> {

        private static final String PROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/";
        private final JSONParser jsonParser = new JSONParser();
        private final List<UUID> uuids;

        public UuidToName(List<UUID> uuids) {
            this.uuids = ImmutableList.copyOf(uuids);
        }

        public UuidToName(UUID uuid) {
            this.uuids = Arrays.asList(uuid);
        }

        @Override
        public HashMap<UUID, String> call() throws Exception {
            try {
                HashMap<UUID, String> uuidStringMap = new HashMap<>();
                for (UUID uuid : uuids) {
                    HttpURLConnection connection = (HttpURLConnection) new URL(PROFILE_URL + uuid.toString().replace("-", "")).openConnection();
                    JSONObject response = (JSONObject) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                    String name = (String) response.get("name");
                    if (name == null) {
                        continue;
                    }
                    String cause = (String) response.get("cause");
                    String errorMessage = (String) response.get("errorMessage");
                    if (cause != null && cause.length() > 0) {
                        throw new IllegalStateException(errorMessage);
                    }
                    uuidStringMap.put(uuid, name);
                }
                return uuidStringMap;
            } catch (Exception ex) {
                //Failed because of offline mode
                return new HashMap<>();
            }
        }
    }

    public static class NameToUuid implements Callable<Map<String, UUID>> {

        private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
        private final JSONParser jsonParser = new JSONParser();
        private final List<String> names;

        public NameToUuid(List<String> names) {
            this.names = ImmutableList.copyOf(names);
        }

        private static void writeBody(HttpURLConnection connection, String body) throws Exception {
            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(body.getBytes());
                stream.flush();
            }
        }

        private static HttpURLConnection createConnection() throws Exception {
            URL url = new URL(PROFILE_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            return connection;
        }

        private static UUID getUUID(String id) {
            return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
        }

        public static byte[] toBytes(UUID uuid) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            return byteBuffer.array();
        }

        public static UUID fromBytes(byte[] array) {
            if (array.length != 16) {
                throw new IllegalArgumentException("Illegal byte array length: " + array.length);
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(array);
            long mostSignificant = byteBuffer.getLong();
            long leastSignificant = byteBuffer.getLong();
            return new UUID(mostSignificant, leastSignificant);
        }

        public static UUID getUUIDOf(String name) throws Exception {
            return new NameToUuid(Arrays.asList(name)).call().get(name);
        }

        @Override
        public Map<String, UUID> call() throws Exception {
            Map<String, UUID> uuidMap = new HashMap<>();
            int requests = new Double(Math.ceil(names.size() / 100)).intValue();
            for (int i = 0; i < requests; i++) {
                HttpURLConnection connection = createConnection();
                String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
                writeBody(connection, body);
                JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                for (Object profile : array) {
                    JSONObject jsonProfile = (JSONObject) profile;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = NameToUuid.getUUID(id);
                    uuidMap.put(name, uuid);
                }
            }
            return uuidMap;
        }

    }
}
