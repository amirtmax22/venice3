package ai.venice.trestage.core;

import java.util.ArrayList;
import java.util.List;

public class Module {

    public enum Category { COMBAT, MOVEMENT, RENDER, PLAYER }

    public final String name;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();
    public boolean enabled;
    public boolean expanded;

    public Module(String name, Category category) {
        this.name = name; this.category = category;
    }

    protected Setting add(Setting s) { settings.add(s); return s; }
    public void toggle() { enabled = !enabled; if (enabled) onEnable(); else onDisable(); }
    public void onEnable() {}
    public void onDisable() {}
    public void onTickStart() {}
    public void onTick() {}
}
