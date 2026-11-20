package ai.venice.trestage.modules.combat;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.Setting;
import ai.venice.trestage.util.MathUtil;
import ai.venice.trestage.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {

    private final Setting range     = add(new Setting("Range", 3.1, 2.5, 6.0, 0.05));
    private final Setting cpsMin    = add(new Setting("CPS Min", 9.0, 1.0, 20.0, 0.5));
    private final Setting cpsMax    = add(new Setting("CPS Max", 13.0, 1.0, 20.0, 0.5));
    private final Setting cpsJitter = add(new Setting("CPS Jitter", 1.6, 0.0, 4.0, 0.1));
    private final Setting cooldown  = add(new Setting("Wait Cooldown", true));
    private final Setting aimAngle  = add(new Setting("Aim Angle", 30.0, 5.0, 90.0, 1.0));
    private final Setting fov       = add(new Setting("FOV", 120.0, 30.0, 360.0, 5.0));
    private final Setting speed     = add(new Setting("Rot Speed", 34.0, 5.0, 180.0, 1.0));
    private final Setting sort      = add(new Setting("Sort", "Angle", "Angle", "Distance", "Health"));
    private final Setting raycast   = add(new Setting("Raycast", true));
    private final Setting miss      = add(new Setting("Miss Chance", 7.0, 0.0, 30.0, 1.0));
    private final Setting players   = add(new Setting("Players", true));
    private final Setting mobs      = add(new Setting("Mobs", false));
    private final Setting animals   = add(new Setting("Animals", false));
    private final Setting invisible = add(new Setting("Invisible", true));
    private final Setting bots      = add(new Setting("Ignore Bots", true));
    private final Setting walls     = add(new Setting("Through Walls", false));
    private final Setting keepSprint= add(new Setting("Keep Sprint", true));
    private final Setting swing     = add(new Setting("Swing", true));
    private final Setting onlyKey   = add(new Setting("Only On Attack", false));
    private final Setting switchCd  = add(new Setting("Switch Cooldown", 120.0, 0.0, 600.0, 10.0));

    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    private LivingEntity target;
    private long nextDelay = 100L;
    private int ticksSinceAttack = 12;

    public KillAura() { super("KillAura", Category.COMBAT); }

    @Override public void onEnable() { target = null; nextDelay = 100L; ticksSinceAttack = 12; }
    @Override public void onDisable() { target = null; }

    @Override
    public void onTick() {
        var mc = Trestage.mc;
        if (mc.currentScreen != null) return;
        if (ticksSinceAttack < 40) ticksSinceAttack++;
        if (onlyKey.bool && !mc.options.attackKey.isPressed()) return;

        LivingEntity candidate = findTarget();

        if (target == null || !MathUtil.valid(target, range.num)
                || (candidate != null && candidate != target && switchTimer.passed((long) switchCd.num))) {
            target = candidate;
            switchTimer.reset();
        }
        if (target == null) return;

        if (!Trestage.rotations.step(target, (float) speed.num)) return;
        if (MathUtil.angleTo(target) > aimAngle.num) return;
        if (cooldown.bool && ticksSinceAttack < 12) return;
        if (!attackTimer.passed(nextDelay)) return;

        if (Math.random() * 100.0 < miss.num) {
            nextDelay = (long) MathUtil.rand(1000.0 / cpsMax.num, 1000.0 / cpsMin.num);
            attackTimer.reset();
            return;
        }

        mc.interactionManager.attackEntity(mc.player, target);
        if (swing.bool) mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.setSprinting(keepSprint.bool);

        double base = 1000.0 / MathUtil.rand(cpsMin.num, cpsMax.num);
        nextDelay = (long) Math.max(45.0, base + MathUtil.rand(-cpsJitter.num, cpsJitter.num));
        attackTimer.reset();
        ticksSinceAttack = 0;
    }

    private LivingEntity findTarget() {
        var mc = Trestage.mc;
        Vec3d eyes = mc.player.getEyePos();
        Box box = mc.player.getBoundingBox().expand(range.num);
        List<LivingEntity> found = new ArrayList<>();

        for (Entity e : mc.world.getOtherEntities(mc.player, box)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (!MathUtil.valid(le, range.num)) continue;
            if (!invisible.bool && le.isInvisible()) continue;
            if (le instanceof ArmorStandEntity) continue;
            if (le instanceof PlayerEntity) {
                if (!players.bool) continue;
                if (bots.bool && isBot(le)) continue;
            } else if (le instanceof HostileEntity) {
                if (!mobs.bool) continue;
            } else if (le instanceof AnimalEntity) {
                if (!animals.bool) continue;
            } else continue;
            if (MathUtil.angleTo(le) > fov.num / 2.0F) continue;
            if (raycast.bool && !walls.bool && !canSee(eyes, le)) continue;
            found.add(le);
        }

        if (found.isEmpty()) return null;
        if (sort.mode.equals("Distance")) found.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));
        else if (sort.mode.equals("Health")) found.sort(Comparator.comparingDouble(LivingEntity::getHealth));
        else found.sort(Comparator.comparingDouble(MathUtil::angleTo));
        return found.get(0);
    }

    private boolean canSee(Vec3d eyes, Entity e) {
        Vec3d point = new Vec3d(e.getX(), e.getY() + e.getHeight() * 0.85, e.getZ());
        HitResult hit = Trestage.mc.world.raycast(new RaycastContext(
            eyes, point, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, Trestage.mc.player));
        if (hit.getType() == HitResult.Type.MISS) return true;
        if (hit instanceof BlockHitResult bhr)
            return bhr.getPos().distanceTo(eyes) >= eyes.distanceTo(point) - 0.05;
        return true;
    }

    private boolean isBot(LivingEntity e) {
        String n = e.getName().getString();
        return n.startsWith("[NPC]") || n.equals("Citizen") || n.matches(".*\\d{4,}.*");
    }
}
