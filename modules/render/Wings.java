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

    private final Setting spread =
            add(new Setting("Spread", 1.0, 0.5, 2.0, 0.05));

    private final Setting size =
            add(new Setting("Size", 1.0, 0.5, 2.0, 0.05));

    public Wings() {
        super("Wings", Category.RENDER);
    }

    public void render(WorldRenderContext ctx) {
        var mc = Trestage.mc;

        if (mc.player == null || ctx.consumers() == null) return;

        var p = mc.player;
        Vec3d cam = ctx.camera().getPos();

        double delta = ctx.tickCounter().getTickDelta(true);

        double bx = p.prevX + (p.getX() - p.prevX) * delta;
        double by = p.prevY + (p.getY() - p.prevY) * delta;
        double bz = p.prevZ + (p.getZ() - p.prevZ) * delta;

        double yaw = Math.toRadians(-p.bodyYaw);

        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);

        double rx = -fz;
        double rz = fx;

        double t = (System.currentTimeMillis() % 3000) / 3000.0;
        double flap = Math.sin(t * Math.PI * 2) * 0.12;

        MatrixStack ms = ctx.matrixStack();

        VertexConsumer vc = ctx.consumers()
                .getBuffer(RenderLayer.getDebugQuads());

        for (int side : new int[]{-1, 1}) {

            double anchorX = bx + rx * side * 0.12;
            double anchorY = by + 1.55;
            double anchorZ = bz + rz * side * 0.12;

            // زاویه بال هنگام بال زدن
            double wingAngle = flap * side;

            // لایه اصلی بال
            for (int i = 0; i < 5; i++) {

                double progress = i / 4.0;

                double rootOut = 0.05 + progress * 0.12;
                double tipOut = spread.num * (0.48 + progress * 0.48);

                double rootY = 0.10 - progress * 0.08;
                double tipY = 0.35 - progress * 0.42 + wingAngle;

                double width = size.num * (0.19 - progress * 0.025);

                double x1 = anchorX + rx * side * rootOut;
                double z1 = anchorZ + rz * side * rootOut;

                double x2 = anchorX + rx * side * tipOut;
                double z2 = anchorZ + rz * side * tipOut;

                double y1 = anchorY + rootY;
                double y2 = anchorY + tipY;

                // هر پر یک قطعه مستقل دارد
                quad(vc, ms,
                        x1 - fx * width - cam.x,
                        y1 - cam.y,
                        z1 - fz * width - cam.z,

                        x2 - fx * width * 0.55 - cam.x,
                        y2 - cam.y,
                        z2 - fz * width * 0.55 - cam.z,

                        x2 + fx * width * 0.55 - cam.x,
                        y2 - cam.y,
                        z2 + fz * width * 0.55 - cam.z,

                        x1 + fx * width - cam.x,
                        y1 - cam.y,
                        z1 + fz * width - cam.z,

                        0.38F, 0.25F, 0.95F, 0.85F
                );
            }

            // پرهای داخلی و کوتاه‌تر
            for (int i = 0; i < 4; i++) {

                double progress = i / 3.0;

                double rootOut = 0.02 + progress * 0.10;
                double tipOut = spread.num * (0.30 + progress * 0.34);

                double rootY = -0.05;
                double tipY = -0.10 - progress * 0.22 + wingAngle;

                double width = size.num * 0.13;

                double x1 = anchorX + rx * side * rootOut;
                double z1 = anchorZ + rz * side * rootOut;

                double x2 = anchorX + rx * side * tipOut;
                double z2 = anchorZ + rz * side * tipOut;

                quad(vc, ms,
                        x1 - fx * width - cam.x,
                        anchorY + rootY - cam.y,
                        z1 - fz * width - cam.z,

                        x2 - fx * width * 0.5 - cam.x,
                        anchorY + tipY - cam.y,
                        z2 - fz * width * 0.5 - cam.z,

                        x2 + fx * width * 0.5 - cam.x,
                        anchorY + tipY - cam.y,
                        z2 + fz * width * 0.5 - cam.z,

                        x1 + fx * width - cam.x,
                        anchorY + rootY - cam.y,
                        z1 + fz * width - cam.z,

                        0.60F, 0.45F, 1.0F, 0.95F
                );
            }
        }
    }

    private void quad(
            VertexConsumer vc,
            MatrixStack ms,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3,
            double x4, double y4, double z4,
            float r, float g, float b, float a
    ) {
        var m = ms.peek().getPositionMatrix();

        vc.vertex(m, (float) x1, (float) y1, (float) z1)
                .color(r, g, b, a);

        vc.vertex(m, (float) x2, (float) y2, (float) z2)
                .color(r, g, b, a);

        vc.vertex(m, (float) x3, (float) y3, (float) z3)
                .color(r, g, b, a);

        vc.vertex(m, (float) x4, (float) y4, (float) z4)
                .color(r, g, b, a);
    }
}
