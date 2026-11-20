package ai.venice.trestage.modules.movement;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = Trestage.mc.player;
        if (p == null) return;
        if (p.forwardSpeed > 0 && !p.horizontalCollision) p.setSprinting(true);
    }
}
