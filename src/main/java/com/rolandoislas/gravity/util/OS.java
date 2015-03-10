package com.rolandoislas.gravity.util;

import com.rolandoislas.gravity.Main;

import java.io.File;
import java.nio.file.Paths;

public class OS {
	
	private static String OSString = System.getProperty("os.name").toLowerCase();
	
	public static String getSys() {
		if(isWindows()) {
			return "win";
		} else if(isMac()) {
			return "mac";
		} else if(isUnix()) {
			return "nix";
		}
		return null;
	}

	public static boolean isUnix() {
		return (OSString.contains("nix") || OSString.contains("nux") || OSString.contains("aix"));
	}

	public static boolean isMac() {
		return (OSString.contains("mac"));
	}

	public static boolean isWindows() {
		return (OSString.contains("win"));
	}

    public static File getDataPath() {
        String homeDir = System.getProperty("user.home");
        if(isWindows()) {
            return Paths.get(homeDir, "AppData", "Roaming", Main.getName()).toFile();
        }
        if(isMac()) {
            return Paths.get(homeDir, "Library", "Application Support", Main.getName()).toFile();
        }
        // Return Linux
        return Paths.get(homeDir, "." + Main.getName()).toFile();
    }
}
