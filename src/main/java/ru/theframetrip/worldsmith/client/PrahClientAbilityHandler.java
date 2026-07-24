package ru.theframetrip.worldsmith.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.network.PrahNetwork;
import ru.theframetrip.worldsmith.registry.ModItems;

/**
 * Uses the same R key as the other active Worldsmith sword ability. The old
 * Predel packet may also be sent by its existing handler, but its server-side
 * validation ignores the packet while Prah is held.
 */
@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, value = Dist.CLIENT)
public final class PrahClientAbilityHandler {
    private PrahClientAbilityHandler() {
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (event.getKey() != GLFW.GLFW_KEY_R || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.player == null) {
            return;
        }
        if (!minecraft.player.getMainHandItem().is(ModItems.PRAH.get())) {
            return;
        }

        PrahNetwork.sendUse();
    }
}
