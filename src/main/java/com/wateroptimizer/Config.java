package com.wateroptimizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Key that opens the HUD (default Right Shift). */
    public static int menuKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
    /** Key that runs the function (default K). */
    public static int actionKey = GLFW.GLFW_KEY_K;
    /** Slider value 0..1 (lower = smoother/slower). */
    public static float speed = 0.1f;

    private static class Data {
        int menuKey = Config.menuKey;
        int actionKey = Config.actionKey;
        float speed = Config.speed;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("wateroptimizer.json");
    }

    /** Degrees per tick the camera moves down. 0% = 0.5, 100% = 15. */
    public static float degreesPerTick() {
        return 0.5f + speed * 14.5f;
    }

    public static void load() {
        try {
            Path p = path();
            if (Files.exists(p)) {
                Data d = GSON.fromJson(Files.readString(p), Data.class);
                if (d != null) {
                    menuKey = d.menuKey;
                    actionKey = d.actionKey;
                    speed = Math.max(0f, Math.min(1f, d.speed));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(new Data()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
