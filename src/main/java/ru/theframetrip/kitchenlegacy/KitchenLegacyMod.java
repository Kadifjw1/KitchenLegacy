package ru.theframetrip.kitchenlegacy;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.theframetrip.kitchenlegacy.registry.ModCreativeModeTabs;
import ru.theframetrip.kitchenlegacy.registry.ModItems;
import ru.theframetrip.kitchenlegacy.registry.ModParticleTypes;

@Mod(KitchenLegacyMod.MOD_ID)
public class KitchenLegacyMod {

    public static final String MOD_ID = "kitchenlegacy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KitchenLegacyMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModParticleTypes.register(modEventBus);

        LOGGER.info("Наследие кухни загружается...");
    }
}
