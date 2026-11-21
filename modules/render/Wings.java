package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class Wings extends Module {

    private final Setting spread = add(new Setting("Spread", 1.0, 0.5, 2.0, 0.05));
    private final Setting size = add(new Setting("Size", 1.0, 0.5, 2.0, 0.05));

    public Wings() { super("Wings", Category.RENDER); }

    public void render(WorldRenderContext ctx) {
        var mc = Trestage.mc;
        if (mc.player == null || ctx.consumers() == null) return;

        Vec3d cam = ctx.camera().getPos();
        var p = mc.player;
        double bx = p.prevX + (p.getX() - p.prevX) * ctx.tickCounter().getTickDelta(true);
        double by = p.prevY + (p.getY() - p.prevY) * ctx.tickCounter().getTickDelta(true);
        double bz = p.prevZ + (p.getZ() - p.prevZ) * ctx.tickCounter().getTickDelta(true);

        float yaw = (float) Math.toRadians(-p.bodyYaw);
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);
        double rx = -fz, rz = fx;

        double t = (System.currentTimeMillis() % 3000) / 3000.0 * Math.PI * 2;
        double flap = Math.sin(t) * 0.18;

        MatrixStack ms = ctx.matrixStack();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getDebugQuads());

        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 4; i++) {
                double s = size.num * (1.0 - i * 0.18);
                double outward = spread.num * (0.35 + i * 0.42) * s;
                double up = (0.95 - i * 0.12) * s + flap;
                double drop = -0.55 * s;

                double ox = bx + rx * side * outward;
                double oz = bz + rz * side * outward;
                double oy = by + 1.25 + up;

                double ix = bx + rx * side * 0.12;
                double iz = bz + rz * side * 0.12;
                double iy = by + 1.15 + up * 0.5;

                quad(vc, ms,
                    ox - cam.x, oy - cam.y, oz - cam.z,
                    ox + fx * 0.28 * s - cam.x, oy + drop - cam.y, oz + fz * 0.28 * s - cam.z,
                    ix + fx * 0.28 * s - cam.x, iy + drop - cam.y, iz + fz * 0.28 * s - cam.z,
                    ix - cam.x, iy - cam.y, iz - cam.z);
            }
        }
    }

    private void quad(VertexConsumer vc, MatrixStack ms,
                      double x1, double y1, double z1, double x2, double y2, double z2,
                      double x3, double y3, double z3, double x4, double y4, double z4) {
        var m = ms.peek().getPositionMatrix();
        vc.vertex(m, (float) x1, (float) y1, (float) z1).color(0.48F, 0.36F, 1.0F, 0.45F);
        vc.vertex(m, (float) x2, (float) y2, (float) z2).color(0.65F, 0.55F, 1.0F, 0.15F);
        vc.vertex(m, (float) x3, (float) y3, (float) z3).color(0.65F, 0.55F, 1.0F, 0.15F);
        vc.vertex(m, (float) x4, (float) y4, (float) z4).color(0.48F, 0.36F, 1.0F, 0.45F);
    }
}
