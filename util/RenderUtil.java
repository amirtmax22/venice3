package ai.venice.trestage.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class RenderUtil {

    public static void rect(DrawContext ctx, float x, float y, float w, float h, int color) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color);
    }

    public static void rounded(DrawContext ctx, float x, float y, float w, float h, float r, int color) {
        rect(ctx, x + r, y, w - 2 * r, h, color);
        rect(ctx, x, y + r, w, h - 2 * r, color);
        rect(ctx, x + 1, y + 1, r, r, color);
        rect(ctx, x + w - r - 1, y + 1, r, r, color);
        rect(ctx, x + 1, y + h - r - 1, r, r, color);
        rect(ctx, x + w - r - 1, y + h - r - 1, r, r, color);
    }

    public static void gradient(DrawContext ctx, float x, float y, float w, float h, int top, int bottom) {
        ctx.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h), top, bottom);
    }

    public static void text(DrawContext ctx, String s, float x, float y, int color) {
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, s, (int) x, (int) y, color);
    }

    public static int width(String s) {
        return MinecraftClient.getInstance().textRenderer.getWidth(s);
    }
}
