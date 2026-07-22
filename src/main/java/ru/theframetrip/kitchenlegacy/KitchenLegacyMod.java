package ru.theframetrip.kitchenlegacy;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import ru.theframetrip.kitchenlegacy.network.ModNetwork;
import ru.theframetrip.kitchenlegacy.registry.ModBlocks;
import ru.theframetrip.kitchenlegacy.registry.ModCreativeModeTabs;
import ru.theframetrip.kitchenlegacy.registry.ModEntities;
import ru.theframetrip.kitchenlegacy.registry.ModItems;
import ru.theframetrip.kitchenlegacy.registry.ModParticleTypes;

@Mod(KitchenLegacyMod.MOD_ID)
public class KitchenLegacyMod {

    public static final String MOD_ID = "kitchenlegacy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KitchenLegacyMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModParticleTypes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModNetwork.register();
        modEventBus.addListener(this::addCreativeTabItems);

        LOGGER.info("Наследие кухни загружается...");
    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.HAMSTER_SPAWN_EGG);
        }
    }
}
