package ru.theframetrip.worldsmith.forgemind.savva.shop;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.theframetrip.worldsmith.WorldsmithMod;

@Mod.EventBusSubscriber(
        modid = WorldsmithMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class SavvaShopClientEvents {
    private SavvaShopClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
                SavvaShopRegistry.SAVVA_SHOP.get(),
                SavvaShopScreen::new
        ));
    }
}
