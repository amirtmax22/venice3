package ai.venice.trestage.util;

import ai.venice.trestage.Trestage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class MathUtil {

    public static float angleTo(Entity target) {
        var p = Trestage.mc.player;
        double dx = target.getX() - p.getX();
        double dz = target.getZ() - p.getZ();
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        return Math.abs(MathHelper.wrapDegrees(yaw - p.getYaw()));
    }

    public static double rand(double min, double max) { return min + Math.random() * (max - min); }

    public static boolean valid(LivingEntity e, double range) {
        var p = Trestage.mc.player;
        return e != null && e != p && e.isAlive() && e.squaredDistanceTo(p) <= range * range;
    }
}
