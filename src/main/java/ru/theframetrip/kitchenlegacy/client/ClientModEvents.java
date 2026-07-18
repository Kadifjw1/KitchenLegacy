package ru.theframetrip.kitchenlegacy.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.client.particle.FireBurstParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FireChargeParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FireGroundParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FireHitParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FireSlashParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FireTrailParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FireWaveParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FlameEmberParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FlameSmokeParticle;
import ru.theframetrip.kitchenlegacy.client.particle.FlameSparkParticle;
import ru.theframetrip.kitchenlegacy.registry.ModParticleTypes;

@Mod.EventBusSubscriber(modid = KitchenLegacyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.FLAME_SPARK.get(), FlameSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAME_EMBER.get(), FlameEmberParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAME_SMOKE.get(), FlameSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_SLASH.get(), FireSlashParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_HIT.get(), FireHitParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_BURST.get(), FireBurstParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_CHARGE.get(), FireChargeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_TRAIL.get(), FireTrailParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_GROUND.get(), FireGroundParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIRE_WAVE.get(), FireWaveParticle.Provider::new);
    }
}
