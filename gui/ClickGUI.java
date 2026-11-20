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

    private static final int BG = 0xF00E0A18, PANEL = 0xFF16112B;
    private static final int H1 = 0xFF7A5CFF, H2 = 0xFF3B2A8C;
    private static final int TEXT = 0xFFEDE9FF, DIM = 0xFF8B82B8;
    private static final int ACCENT = 0xFF7A5CFF, ROW = 0xFF1E1838, HOVER = 0xFF2A2350;

    private final Module.Category[] cats = Module.Category.values();
    private Setting dragging;
    private int dragX;
    private int px, py;

    public ClickGUI() { super(Text.literal("Trestage")); }

    @Override
    protected void init() {
        px = (width - 430) / 2;
        py = (height - 300) / 2;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int w = 430, h = 300;
        RenderUtil.rounded(ctx, px - 2, py - 2, w + 4, h + 4, 10, 0x60000000);
        RenderUtil.rounded(ctx, px, py, w, h, 8, BG);
        RenderUtil.gradient(ctx, px, py, w, 30, H1, H2);
        RenderUtil.text(ctx, "TRESTAGE", px + 14, py + 11, 0xFFFFFFFF);
        RenderUtil.text(ctx, "v" + Trestage.VERSION, px + w - 14 - RenderUtil.width("v" + Trestage.VERSION), py + 11, 0xFFE4DCFF);
        RenderUtil.text(ctx, "right shift to close", px + 14, py + 34, DIM);

        int colW = (w - 24) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int cx = px + 12 + i * colW, cy = py + 50;
            RenderUtil.rounded(ctx, cx, cy, colW - 6, 12, 4, 0x40FFFFFF);
            RenderUtil.text(ctx, cats[i].name(), cx + 6, cy + 2, TEXT);

            int ry = cy + 18;
            for (Module m : Trestage.modules.in(cats[i])) {
                boolean hover = in(mouseX, mouseY, cx, ry, colW - 6, 14);
                RenderUtil.rounded(ctx, cx, ry, colW - 6, 14, 4, m.enabled ? ACCENT : (hover ? HOVER : ROW));
                RenderUtil.text(ctx, m.name, cx + 6, ry + 3, m.enabled ? 0xFFFFFFFF : TEXT);

                if (m.expanded) {
                    int sy = ry + 18;
                    for (Setting s : m.settings) {
                        if (!s.isVisible()) continue;
                        if (s.type == Setting.Type.BOOLEAN) {
                            RenderUtil.text(ctx, s.name, cx + 6, sy, DIM);
                            RenderUtil.rounded(ctx, cx + colW - 18, sy - 1, 10, 10, 3, s.bool ? ACCENT : HOVER);
                            sy += 13;
                        } else if (s.type == Setting.Type.MODE) {
                            RenderUtil.text(ctx, s.name + ": " + s.mode, cx + 6, sy, DIM);
                            sy += 12;
                        } else {
                            RenderUtil.text(ctx, s.name + " " + s.display(), cx + 6, sy, DIM);
                            RenderUtil.rect(ctx, cx + 6, sy + 10, colW - 18, 2, 0xFF2A2350);
                            float pct = (float) ((s.num - s.min) / (s.max - s.min));
                            RenderUtil.rect(ctx, cx + 6, sy + 10, (colW - 18) * pct, 2, ACCENT);
                            sy += 20;
                        }
                    }
                    ry = sy;
                }
                ry += 16;
            }
        }

        RenderUtil.rounded(ctx, px, py + h - 18, w, 18, 8, 0x40FFFFFF);
        RenderUtil.text(ctx, "range 3.0-3.3 | rot 25-45 | aim 25-35 | cps 9-13", px + 12, py + h - 13, DIM);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int colW = (430 - 24) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            int cx = px + 12 + i * colW, cy = py + 50;
            int ry = cy + 18;
            for (Module m : Trestage.modules.in(cats[i])) {
                if (in((int) mx, (int) my, cx, ry, colW - 6, 14)) {
                    if (button == 0) { m.toggle(); Config.save(); }
                    else if (button == 1) m.expanded = !m.expanded;
                    return true;
                }
                if (m.expanded) {
                    int sy = ry + 18;
                    for (Setting s : m.settings) {
                        if (!s.isVisible()) continue;
                        if (s.type == Setting.Type.BOOLEAN) {
                            if (in((int) mx, (int) my, cx + 6, sy - 2, colW - 12, 12)) { s.toggle(); Config.save(); return true; }
                            sy += 13;
                        } else if (s.type == Setting.Type.MODE) {
                            if (in((int) mx, (int) my, cx + 6, sy - 2, colW - 12, 12)) { s.cycle(); Config.save(); return true; }
                            sy += 12;
                        } else {
                            if (in((int) mx, (int) my, cx + 6, sy + 8, colW - 18, 6)) {
                                dragging = s; dragX = cx + 6; s.drag(mx, dragX, colW - 18); return true;
                            }
                            sy += 20;
                        }
                    }
                    ry = sy;
                }
                ry += 16;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != null) {
            dragging.drag(mx, dragX, (430 - 24) / cats.length - 18);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != null) { dragging = null; Config.save(); }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            Config.save();
            close();
            return true;
        }
        return super.keyPressed(key, scancode, mods);
    }

    private boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
