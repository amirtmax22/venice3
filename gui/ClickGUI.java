package ai.venice.trestage.gui;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Config;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import ai.venice.trestage.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {

    private static final int BG = 0xFF0B0A10, SIDE = 0xFF12111A, PANEL = 0xFF15141E;
    private static final int H1 = 0xFF8A6BFF, H2 = 0xFF5B3FD6;
    private static final int TXT = 0xFFF2EFFF, DIM = 0xFF7C7690;
    private static final int ACC = 0xFF8A6BFF, ROW = 0xFF1A1926, HOV = 0xFF221F33;

    private final Module.Category[] cats = Module.Category.values();
    private int selected = 0;
    private Setting dragging;
    private int dragX;
    private int px, py, scroll;

    public ClickGUI() { super(Text.literal("Trestage")); }

    @Override
    protected void init() {
        px = (width - 460) / 2;
        py = (height - 300) / 2;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xC8050408);

        int w = 460, h = 300;
        RenderUtil.rounded(ctx, px - 1, py - 1, w + 2, h + 2, 11, 0x40FFFFFF);
        RenderUtil.rounded(ctx, px, py, w, h, 10, BG);

        RenderUtil.gradient(ctx, px, py, w, 34, H1, H2);
        RenderUtil.rounded(ctx, px, py, w, 34, 10, 0x00FFFFFF);
        RenderUtil.text(ctx, "TRESTAGE", px + 16, py + 13, 0xFFFFFFFF);
        String v = "v" + Trestage.VERSION;
        RenderUtil.text(ctx, v, px + w - 16 - RenderUtil.width(v), py + 13, 0xFFEDE6FF);

        RenderUtil.rounded(ctx, px, py + 34, 96, h - 34, 0, SIDE);
        int cy = py + 44;
        for (int i = 0; i < cats.length; i++) {
            boolean sel = i == selected;
            boolean hov = in(mouseX, mouseY, px + 6, cy, 84, 20);
            if (sel || hov) RenderUtil.rounded(ctx, px + 6, cy, 84, 20, 5, sel ? ACC : HOV);
            RenderUtil.text(ctx, cats[i].name(), px + 14, cy + 6, sel ? 0xFFFFFFFF : DIM);
            cy += 24;
        }
        RenderUtil.text(ctx, "R-SHIFT", px + 14, py + h - 22, 0xFF4C4760);

        int cx = px + 108, cyy = py + 44;
        RenderUtil.rounded(ctx, cx - 4, cyy - 4, w - 116, h - 56, 6, PANEL);
        int ry = cyy - scroll;

        for (Module m : Trestage.modules.in(cats[selected])) {
            boolean hov = in(mouseX, mouseY, cx, ry, w - 132, 18);
            RenderUtil.rounded(ctx, cx, ry, w - 132, 18, 4, hov ? HOV : ROW);
            RenderUtil.text(ctx, m.name, cx + 8, ry + 5, m.enabled ? ACC : TXT);

            int tx = cx + w - 152;
            RenderUtil.rounded(ctx, tx, ry + 5, 18, 8, 4, m.enabled ? ACC : 0xFF2C2942);
            RenderUtil.rect(ctx, (int) (tx + (m.enabled ? 10 : 2)), ry + 6, 6, 6, 0xFFFFFFFF);

            if (m.expanded) {
                int sy = ry + 22;
                for (Setting s : m.settings) {
                    if (!s.isVisible()) continue;
                    if (s.type == Setting.Type.BOOLEAN) {
                        RenderUtil.text(ctx, s.name, cx + 12, sy, DIM);
                        RenderUtil.rounded(ctx, cx + w - 152, sy - 1, 12, 9, 4, s.bool ? ACC : 0xFF2C2942);
                        sy += 14;
                    } else if (s.type == Setting.Type.MODE) {
                        RenderUtil.text(ctx, s.name + ": " + s.mode, cx + 12, sy, DIM);
                        sy += 13;
                    } else {
                        RenderUtil.text(ctx, s.name + " " + s.display(), cx + 12, sy, DIM);
                        RenderUtil.rect(ctx, cx + 12, sy + 10, w - 160, 2, 0xFF2C2942);
                        float pct = (float) ((s.num - s.min) / (s.max - s.min));
                        RenderUtil.rect(ctx, cx + 12, sy + 10, (w - 160) * pct, 2, ACC);
                        sy += 21;
                    }
                }
                ry = sy + 2;
            }
            ry += 22;
        }

        RenderUtil.text(ctx, "left: toggle   right: settings", px + 108, py + h - 16, 0xFF4C4760);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (int i = 0; i < cats.length; i++) {
            if (in((int) mx, (int) my, px + 6, py + 44 + i * 24, 84, 20)) {
                selected = i; scroll = 0;
                return true;
            }
        }

        int cx = px + 108;
        int ry = py + 44 - scroll;
        for (Module m : Trestage.modules.in(cats[selected])) {
            if (in((int) mx, (int) my, cx, ry, 460 - 132, 18)) {
                if (button == 0) { m.toggle(); Config.save(); }
                else if (button == 1) m.expanded = !m.expanded;
                return true;
            }
            if (m.expanded) {
                int sy = ry + 22;
                for (Setting s : m.settings) {
                    if (!s.isVisible()) continue;
                    if (s.type == Setting.Type.BOOLEAN) {
                        if (in((int) mx, (int) my, cx + 12, sy - 2, 460 - 160, 12)) { s.toggle(); Config.save(); return true; }
                        sy += 14;
                    } else if (s.type == Setting.Type.MODE) {
                        if (in((int) mx, (int) my, cx + 12, sy - 2, 460 - 160, 12)) { s.cycle(); Config.save(); return true; }
                        sy += 13;
                    } else {
                        if (in((int) mx, (int) my, cx + 12, sy + 8, 460 - 160, 6)) {
                            dragging = s; dragX = cx + 12; s.drag(mx, dragX, 460 - 160); return true;
                        }
                        sy += 21;
                    }
                }
                ry = sy + 2;
            }
            ry += 22;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) { dragging.drag(mx, dragX, 460 - 160); return true; }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) { dragging = null; Config.save(); }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scroll = (int) Math.max(0, scroll - vAmount * 18);
        return true;
    }

    @Override
    public boolean keyPressed(int key, int sc, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Config.save();
            close();
            return true;
        }
        return super.keyPressed(key, sc, mods);
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
