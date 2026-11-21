package ai.venice.trestage.core;

import ai.venice.trestage.modules.combat.KillAura;
import ai.venice.trestage.modules.movement.Sprint;
import ai.venice.trestage.modules.player.Velocity;
import ai.venice.trestage.modules.render.ESP;
import ai.venice.trestage.modules.render.Fullbright;
import ai.venice.trestage.modules.render.HUD;
import ai.venice.trestage.modules.render.Wings;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    public final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        modules.add(new KillAura());
        modules.add(new Sprint());
        modules.add(new Velocity());
        modules.add(new ESP());
        modules.add(new Wings());
        modules.add(new Fullbright());
        modules.add(new HUD());
    }

    public void onTickStart() { for (Module m : modules) if (m.enabled) m.onTickStart(); }
    public void onTick() { for (Module m : modules) if (m.enabled) m.onTick(); }

    public Module byName(String n) {
        for (Module m : modules) if (m.name.equalsIgnoreCase(n)) return m;
        return null;
    }

    public List<Module> in(Module.Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.category == c) out.add(m);
        return out;
    }
}
