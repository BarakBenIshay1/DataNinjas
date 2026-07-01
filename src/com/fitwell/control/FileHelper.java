package com.fitwell.control;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import com.fitwell.entity.DBConst;

public class FileHelper {
	
	public static String getDataPath(String fileName) {
        try {
            URL location = DBConst.class.getProtectionDomain().getCodeSource().getLocation();
            String decoded = URLDecoder.decode(location.getPath(), StandardCharsets.UTF_8);

            File codeLocation = new File(decoded);
            File baseDir = codeLocation.isFile() ? codeLocation.getParentFile() : codeLocation;

            // We'll search in both: data/ and db/
            String[] folders = {"data", "db"};

            // 1) <runFolder>/data|db/DB_NAME   (works when running from IDE output or from a JAR folder)
            for (String f : folders) {
                File candidate = new File(baseDir, f + File.separator + fileName);
                if (candidate.exists()) return candidate.getAbsolutePath();
            }

            // 2) parent of runFolder (common in IDE: target/classes -> project root)
            File parent = baseDir.getParentFile();
            if (parent != null) {
                for (String f : folders) {
                    File candidate = new File(parent, f + File.separator + fileName);
                    if (candidate.exists()) return candidate.getAbsolutePath();
                }
            }

            // 3) relative to current working dir
            for (String f : folders) {
                File candidate = new File(f + File.separator + fileName);
                if (candidate.exists()) return candidate.getAbsolutePath();
            }

            throw new RuntimeException(
                    "DB file not found. Expected: data/" + fileName + " or db/" + fileName + "\n" +
                    "Tip: keep the folder (data/ or db/) in the project root, and next to the JAR when exporting."
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve DB path: " + e.getMessage(), e);
        }
    }

}
