package ai.venice.trestage.modules.player;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;

public class Velocity extends Module {

    private final Setting horizontal = add(new Setting("Horizontal %", 0.0, 0.0, 100.0, 5.0));
    private int lastHurt;

    public Velocity() { super("Velocity", Category.PLAYER); }

    @Override
    public void onTick() {
        var p = Trestage.mc.player;
        if (p == null) return;
        if (p.hurtTime > 0 && lastHurt <= 0) {
            double f = horizontal.num / 100.0;
            p.setVelocity(p.getVelocity().x * f, p.getVelocity().y, p.getVelocity().z * f);
        }
        lastHurt = p.hurtTime;
    }
}
