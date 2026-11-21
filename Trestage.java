package ai.venice.trestage;

import ai.venice.trestage.core.Config;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.core.ModuleManager;
import ai.venice.trestage.core.RotationManager;
import ai.venice.trestage.gui.ClickGUI;
import ai.venice.trestage.modules.render.ESP;
import ai.venice.trestage.modules.render.HUD;
import ai.venice.trestage.modules.render.Wings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Trestage implements ClientModInitializer {

    public static final String VERSION = "1.1.0";
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static ModuleManager modules;
    public static RotationManager rotations;
    public static ClickGUI gui;

    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        rotations = new RotationManager();
        gui = new ClickGUI();
        Config.load();

        openGui = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.trestage.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.trestage"));

        ClientTickEvents.START_CLIENT_TICK.register(c -> {
            while (openGui.wasPressed()) if (c.currentScreen == null) c.setScreen(gui);
            if (c.player == null || c.world == null) return;
            modules.onTickStart();
        });

        ClientTickEvents.END_CLIENT_TICK.register(c -> {
            if (c.player == null || c.world == null) return;
            modules.onTick();
        });

        HudRenderCallback.EVENT.register((ctx, delta) -> {
            Module hud = modules.byName("HUD");
            if (hud != null && hud.enabled) ((HUD) hud).render(ctx);
        });

        WorldRenderEvents.LAST.register(ctx -> {
            Module esp = modules.byName("ESP");
            if (esp != null && esp.enabled) ((ESP) esp).render(ctx);
            Module wings = modules.byName("Wings");
            if (wings != null && wings.enabled) ((Wings) wings).render(ctx);
        });
    }
}
