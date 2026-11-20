package ai.venice.trestage.core;

import ai.venice.trestage.Trestage;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Config {

    private static File file() {
        return new File(MinecraftClient.getInstance().runDirectory, "config/trestage.json");
    }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : Trestage.modules.modules) {
                JsonObject mo = new JsonObject();
                mo.addProperty("enabled", m.enabled);
                for (Setting s : m.settings) {
                    switch (s.type) {
                        case BOOLEAN: mo.addProperty(s.name, s.bool); break;
                        case NUMBER: mo.addProperty(s.name, s.num); break;
                        case MODE: mo.addProperty(s.name, s.mode); break;
                    }
                }
                root.add(m.name, mo);
            }
            File f = file();
            if (f.getParentFile() != null) f.getParentFile().mkdirs();
            try (PrintWriter w = new PrintWriter(f, "UTF-8")) {
                w.print(new GsonBuilder().setPrettyPrinting().create().toJson(root));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        try {
            File f = file();
            if (!f.exists()) return;
            JsonObject root = JsonParser.parseString(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
            for (Module m : Trestage.modules.modules) {
                if (!root.has(m.name)) continue;
                JsonObject mo = root.getAsJsonObject(m.name);
                for (Setting s : m.settings) {
                    if (!mo.has(s.name)) continue;
                    JsonElement el = mo.get(s.name);
                    switch (s.type) {
                        case BOOLEAN: s.bool = el.getAsBoolean(); break;
                        case NUMBER: s.num = el.getAsDouble(); break;
                        case MODE: if (s.modes.contains(el.getAsString())) s.mode = el.getAsString(); break;
                    }
                }
                if (mo.has("enabled") && mo.get("enabled").getAsBoolean() && !m.enabled) m.toggle();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
