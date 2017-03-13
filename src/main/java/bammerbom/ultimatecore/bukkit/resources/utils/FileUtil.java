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

import org.apache.commons.io.IOUtils;

import bammerbom.ultimatecore.bukkit.ErrorLogger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileUtil {

    public static ArrayList<String> getLines(File file) {
        try {
            ArrayList<String> lines = new ArrayList<>();
            if (!file.exists()) {
                return null;
            }
            Path path = Paths.get(file.getAbsolutePath());
            Charset ENCODING = StandardCharsets.UTF_8;
            Scanner scanner = new Scanner(path, ENCODING.name());
            try {
                while (scanner.hasNextLine()) {
                    lines.add(scanner.nextLine());
                }
            }finally {
                scanner.close();
            }
            return lines;
        } catch (Exception ex) {
            ErrorLogger.log(ex, "Failed to get lines of file " + file.getName());
            return null;
        }
    }

    public static List<String> getLines(InputStream stream) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(stream, writer);
        } catch (IOException ex) {
            ErrorLogger.log(ex, "Failed to read file.");
        }
        String list = writer.toString();
        return Arrays.asList(list.split("\n"));
    }

    public static void writeFile(File file, List<String> aLines) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()), StandardCharsets.UTF_8)) {
            for (String line : aLines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void copy(File src, File dest) throws IOException {

        if (src.isDirectory()) {

            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copy(srcFile, destFile);
            }

        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
}
