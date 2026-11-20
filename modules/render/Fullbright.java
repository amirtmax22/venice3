package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;

public class Fullbright extends Module {

    private double old = 0.5;

    public Fullbright() { super("Fullbright", Category.RENDER); }

    @Override
    public void onEnable() {
        old = Trestage.mc.options.getGamma().getValue();
        Trestage.mc.options.getGamma().setValue(1.0);
    }

    @Override
    public void onDisable() { Trestage.mc.options.getGamma().setValue(old); }
}
