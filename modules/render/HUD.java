package ai.venice.trestage.modules.render;

import ai.venice.trestage.Trestage;
import ai.venice.trestage.core.Module;
import ai.venice.trestage.util.RenderUtil;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HUD extends Module {

    public HUD() { super("HUD", Category.RENDER); }

    @Override
    public void onEnable() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this); }

    @Override
    public void onDisable() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this); }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text e) {
        RenderUtil.text("\u00A75Trestage \u00A77| \u00A7f" + Trestage.mc.debug.split(" fps")[0] + " fps", 4, 4, 0xFFFFFFFF);
    }
}
