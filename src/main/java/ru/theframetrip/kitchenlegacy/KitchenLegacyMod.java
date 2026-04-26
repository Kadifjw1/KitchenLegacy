package ru.theframetrip.kitchenlegacy;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(KitchenLegacyMod.MOD_ID)
public class KitchenLegacyMod {

    public static final String MOD_ID = "kitchenlegacy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KitchenLegacyMod() {
        LOGGER.info("Наследие кухни загружается...");
    }
}
