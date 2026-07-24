package ru.theframetrip.worldsmith.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.theframetrip.worldsmith.WorldsmithMod;

/**
 * Isolated packet channel for Prah. Keeping it separate allows the ability to
 * be added without changing the already stable Predel network channel.
 */
@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PrahNetwork {
    private static final String PROTOCOL = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WorldsmithMod.MOD_ID, "prah_ability"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private PrahNetwork() {
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CHANNEL.messageBuilder(UsePrahAbilityPacket.class, 0)
                .encoder(UsePrahAbilityPacket::encode)
                .decoder(UsePrahAbilityPacket::new)
                .consumerMainThread(UsePrahAbilityPacket::handle)
                .add());
    }

    public static void sendUse() {
        CHANNEL.sendToServer(new UsePrahAbilityPacket());
    }
}
