package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ESP extends Module {

    private final Setting range = add(new Setting("Range", 40.0, 8.0, 128.0, 4.0));
    private final Setting playersOnly = add(new Setting("Players Only", true));
    private final Setting tracer = add(new Setting("Tracer", true));
    private final Setting tracerFromEyes = add(new Setting("Tracer From Eyes", false));

    public ESP() { super("ESP", Category.RENDER); }

    public void render(WorldRenderContext ctx) {
        var mc = Trestage.mc;
        if (mc.world == null || mc.player == null || ctx.consumers() == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack ms = ctx.matrixStack();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = ms.peek();

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity) || e == mc.player) continue;
            if (playersOnly.bool && !(e instanceof PlayerEntity)) continue;
            if (e.squaredDistanceTo(mc.player) > range.num * range.num) continue;

            Box b = e.getBoundingBox().expand(0.05).offset(-cam.x, -cam.y, -cam.z);
            box(vc, entry, b);

            if (tracer.bool) {
                double sx = 0, sy = 0, sz = 0;
                if (tracerFromEyes.bool) {
                    Vec3d eye = mc.player.getEyePos();
                    sx = eye.x - cam.x; sy = eye.y - cam.y; sz = eye.z - cam.z;
                }
                double tx = e.getX() - cam.x;
                double ty = e.getY() + e.getHeight() * 0.5 - cam.y;
                double tz = e.getZ() - cam.z;
                edge(vc, entry, sx, sy, sz, tx, ty, tz, 0.48F, 0.36F, 1.0F, 0.75F);
            }
        }
    }

    private void box(VertexConsumer vc, MatrixStack.Entry entry, Box b) {
        float r = 0.48F, g = 0.36F, bl = 1.0F, a = 0.9F;
        edge(vc, entry, b.minX, b.minY, b.minZ, b.maxX, b.minY, b.minZ, r, g, bl, a);
        edge(vc, entry, b.maxX, b.minY, b.minZ, b.maxX, b.minY, b.maxZ, r, g, bl, a);
        edge(vc, entry, b.maxX, b.minY, b.maxZ, b.minX, b.minY, b.maxZ, r, g, bl, a);
        edge(vc, entry, b.minX, b.minY, b.maxZ, b.minX, b.minY, b.minZ, r, g, bl, a);
        edge(vc, entry, b.minX, b.maxY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, entry, b.maxX, b.maxY, b.minZ, b.maxX, b.maxY, b.maxZ, r, g, bl, a);
        edge(vc, entry, b.maxX, b.maxY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bl, a);
        edge(vc, entry, b.minX, b.maxY, b.maxZ, b.minX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, entry, b.minX, b.minY, b.minZ, b.minX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, entry, b.maxX, b.minY, b.minZ, b.maxX, b.maxY, b.minZ, r, g, bl, a);
        edge(vc, entry, b.maxX, b.minY, b.maxZ, b.maxX, b.maxY, b.maxZ, r, g, bl, a);
        edge(vc, entry, b.minX, b.minY, b.maxZ, b.minX, b.maxY, b.maxZ, r, g, bl, a);
    }

    private void edge(VertexConsumer vc, MatrixStack.Entry entry,
                      double x1, double y1, double z1, double x2, double y2, double z2,
                      float r, float g, float b, float a) {
        float nx = (float) (x2 - x1), ny = (float) (y2 - y1), nz = (float) (z2 - z1);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len == 0) return;
        nx /= len; ny /= len; nz /= len;
        vc.vertex(entry.getPositionMatrix(), (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(entry, nx, ny, nz);
        vc.vertex(entry.getPositionMatrix(), (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(entry, nx, ny, nz);
    }
}
