package ru.theframetrip.kitchenlegacy.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.client.particle.FlameSparkParticle;
import ru.theframetrip.kitchenlegacy.registry.ModParticleTypes;

@Mod.EventBusSubscriber(modid = KitchenLegacyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.FLAME_SPARK.get(), FlameSparkParticle.Provider::new);
    }
}
