package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class HUD extends Module {
    public HUD() { super("HUD", Category.RENDER); }

    public void render(DrawContext ctx) {
        var p = Trestage.mc.player;
        if (p == null) return;
        String line = "Trestage | " + Trestage.mc.getCurrentFps() + " fps | "
            + String.format("%.1f %.1f %.1f", p.getX(), p.getY(), p.getZ());
        RenderUtil.rounded(ctx, 3, 3, RenderUtil.width(line) + 8, 14, 3, 0xC0100A20);
        RenderUtil.text(ctx, line, 7, 6, 0xFFEDE9FF);
    }
}
