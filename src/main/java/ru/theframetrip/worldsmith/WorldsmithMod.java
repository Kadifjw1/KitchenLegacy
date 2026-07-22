package ru.theframetrip.worldsmith;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.theframetrip.worldsmith.network.ModNetwork;
import ru.theframetrip.worldsmith.registry.ModBlocks;
import ru.theframetrip.worldsmith.registry.ModCreativeModeTabs;
import ru.theframetrip.worldsmith.registry.ModItems;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

@Mod(WorldsmithMod.MOD_ID)
public class WorldsmithMod {
    public static final String MOD_ID = "worldsmith";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WorldsmithMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModParticleTypes.register(modEventBus);
        ModNetwork.register();
        LOGGER.info("Worldsmith loading...");
    }
}
