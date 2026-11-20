package ai.venice.trestage.core;

import ai.venice.trestage.Trestage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.MathHelper;

public class RotationManager {

    private float lastYaw, lastPitch;

    public float gcd() {
        float f = (float) (Trestage.mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2);
        float g = f * f * f * 1.2F;
        return Math.max(g * 8.0F * 0.15F * 0.15F, 0.0005F);
    }

    public float[] rawTo(LivingEntity target) {
        var p = Trestage.mc.player;
        double dx = target.getX() - p.getX();
        double dy = (target.getY() + target.getEyeHeight(target.getPose()) * 0.9) - p.getEyePos().y;
        double dz = target.getZ() - p.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{ yaw, pitch };
    }

    public boolean step(LivingEntity target, float speed) {
        float[] want = rawTo(target);
        var p = Trestage.mc.player;
        float curYaw = p.getYaw(), curPitch = p.getPitch();

        float dYaw = MathHelper.wrapDegrees(want[0] - curYaw);
        float dPitch = want[1] - curPitch;
        float gcd = gcd();
        float maxStep = Math.max(1.0F, speed);

        float sYaw = MathHelper.clamp(dYaw * 0.55F, -maxStep, maxStep);
        float sPitch = MathHelper.clamp(dPitch * 0.55F, -maxStep, maxStep);

        float nYaw = Math.round((curYaw + sYaw) / gcd) * gcd;
        float nPitch = Math.round(MathHelper.clamp(curPitch + sPitch, -90.0F, 90.0F) / gcd) * gcd;

        p.setYaw(nYaw);
        p.setPitch(nPitch);

        if (nYaw != lastYaw || nPitch != lastPitch) {
            Trestage.mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.LookAndOnGround(nYaw, nPitch, p.isOnGround()));
            lastYaw = nYaw;
            lastPitch = nPitch;
        }

        return Math.abs(MathHelper.wrapDegrees(want[0] - nYaw)) < 1.5F && Math.abs(want[1] - nPitch) < 1.5F;
    }
}
