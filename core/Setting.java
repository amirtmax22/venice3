package ai.venice.trestage.core;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public class Setting {

    public enum Type { BOOLEAN, NUMBER, MODE }

    public final String name;
    public final Type type;
    public boolean bool;
    public double num, min, max, step;
    public String mode;
    public List<String> modes;
    public BooleanSupplier visible = () -> true;

    public Setting(String name, boolean value) {
        this.name = name; this.type = Type.BOOLEAN; this.bool = value;
    }

    public Setting(String name, double value, double min, double max, double step) {
        this.name = name; this.type = Type.NUMBER;
        this.num = value; this.min = min; this.max = max; this.step = step;
    }

    public Setting(String name, String value, String... modes) {
        this.name = name; this.type = Type.MODE;
        this.mode = value; this.modes = Arrays.asList(modes);
    }

    public Setting visibleWhen(BooleanSupplier s) { this.visible = s; return this; }
    public boolean isVisible() { return visible.getAsBoolean(); }
    public void toggle() { bool = !bool; }
    public void cycle() { mode = modes.get((modes.indexOf(mode) + 1) % modes.size()); }

    public void drag(double mouseX, double x, double width) {
        double pct = Math.max(0, Math.min(1, (mouseX - x) / width));
        double raw = min + pct * (max - min);
        num = Math.round(raw / step) * step;
        num = Math.max(min, Math.min(max, num));
    }

    public String display() {
        if (type == Type.NUMBER)
            return num == Math.floor(num) ? String.valueOf((int) num) : String.format("%.2f", num);
        return mode;
    }
}
