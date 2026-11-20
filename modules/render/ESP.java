package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class ESP extends Module {

    private final Setting range = add(new Setting("Range", 40.0, 8.0, 128.0, 4.0));
    private final Setting playersOnly = add(new Setting("Players Only", true));

    public ESP() { super("ESP", Category.RENDER); }

    public void render(WorldRenderContext ctx) {
        var mc = Trestage.mc;
        if (mc.world == null || mc.player == null) return;

        Vec3d cam = ctx.camera().getPos();
        MatrixStack ms = ctx.matrixStack();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderLayer.getLines());
        ms.push();
        ms.translate(-cam.x, -cam.y, -cam.z);

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity) || e == mc.player) continue;
            if (playersOnly.bool && !(e instanceof PlayerEntity)) continue;
            if (e.squaredDistanceTo(mc.player) > range.num * range.num) continue;
            WorldRenderer.drawBox(ms, vc, e.getBoundingBox().expand(0.05), 0.48F, 0.36F, 1.0F, 0.9F);
        }

        ms.pop();
    }
}
