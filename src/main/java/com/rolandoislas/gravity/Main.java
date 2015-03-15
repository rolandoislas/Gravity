package com.rolandoislas.gravity;

import com.rolandoislas.gravity.state.SplashScreen;
import com.rolandoislas.gravity.util.FileUtil;
import com.rolandoislas.gravity.util.OS;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Rolando Islas
 */
public class Main extends StateBasedGame {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int WIDTH_4x3 = 1024;
    private static final int HEIGHT_4x3 = 768;
    private static final int FPS = 100;
    private static final String GAME_NAME = "Gravity";
    private static int width;
    private static int height;
    private static String nativesFolder;

    public static String getName() {
        return GAME_NAME;
    }

    public enum STATE_ID {
        GAME(0),
        MAIN_MENU(1),
        MULTIPLAYER_MENU(2),
        MULTIPLAYER_LOBBY(3),
        MULTIPLAYER_JOIN_MENU(4),
        SPLASH_SCREEN(5);
        public int id;
        private STATE_ID(int id) {
            this.id = id;
        }
    }

    public Main(String title) {
        super(title);
    }

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    @Override
    public void initStatesList(GameContainer container) throws SlickException {
        addState(new SplashScreen(STATE_ID.SPLASH_SCREEN.id));
    }

    public static void main(String[] args) {
        checkArgs(args);
        try {
            createNatives();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            AppGameContainer gc = new AppGameContainer(new Main(GAME_NAME));
            gc.setDisplayMode(width, height, false);
            gc.setTargetFrameRate(FPS);
            gc.setShowFPS(false);
            gc.setUpdateOnlyWhenVisible(false);
            gc.setAlwaysRender(true);
            gc.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    private static void checkArgs(String[] args) {
        int index = 0;
        // resolution
        width = WIDTH;
        height = HEIGHT;
        // natives
        nativesFolder = "natives";
        for(String arg : args) {
            // 4x3
            if(arg.equals("4x3")) {
                System.out.println("Using 4x3 resolution.");
                width = WIDTH_4x3;
                height = HEIGHT_4x3;
            }
            // natives
            if(arg.equals("natives")) {
                if(args.length > index + 1) {
                    nativesFolder = args[index + 1];
                    System.out.println("Using " + nativesFolder + " as natives folder.");
                }
            }
            index++;
        }

    }

    private static void createNatives() throws IOException {
        String os = OS.getSys();
        switch (os) {
            case "win":
                extractNatives("windows");
                break;
            case "mac":
                extractNatives("osx");
                break;
            case "nix":
                extractNatives("linux");
                break;
        }
        System.setProperty("org.lwjgl.librarypath", new File(OS.getDataPath(), nativesFolder).getAbsolutePath());
        System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
    }

    private static void extractNatives(String os) throws IOException {
        // Get resources
        String nativeFileName = "lwjgl-platform-2.9.1-natives-" + os + ".jar";
        InputStream nativeJar = Main.class.getResourceAsStream("/natives/" + nativeFileName);
        String nativeInputFileName = "jinput-platform-2.0.5-natives-" + os + ".jar";
        InputStream nativeInputjar = Main.class.getResourceAsStream("/natives/" + nativeInputFileName);
        // define natives path
        File nativesPath = new File(OS.getDataPath(), nativesFolder);
        // export native jars
        FileUtil.export(nativeJar, nativesPath, nativeFileName);
        FileUtil.export(nativeInputjar, nativesPath, nativeInputFileName);
        // unzip natives
        FileUtil.unZip(new File(nativesPath, nativeFileName), nativesPath);
        FileUtil.unZip(new File(nativesPath, nativeInputFileName), nativesPath);
		if(os.equals("osx")) {
			FileUtil.rename(new File(nativesPath, "liblwjgl.jnilib"), new File(nativesPath, "liblwjgl.dylib"), true);
			FileUtil.rename(new File(nativesPath, "libjinput-osx.jnilib"), new File(nativesPath, "libjinput-osx.dylib"), true);
		}
    }

}
