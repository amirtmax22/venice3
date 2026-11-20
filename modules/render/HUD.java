
package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class HUD extends Module {

    public HUD() {
        super("HUD", Category.RENDER);
    }

    public void render(DrawContext ctx) {
        if (Trestage.mc.player == null) return;

        RenderUtil.text(
            ctx,
            "Trestage | " + Trestage.mc.getCurrentFps() + " FPS",
            4, 4,
            0xFFFFFFFF
        );
    }
}
