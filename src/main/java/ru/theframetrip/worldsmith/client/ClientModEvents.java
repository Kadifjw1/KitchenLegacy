package ru.theframetrip.worldsmith.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.client.particle.VoidParticle;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.VOID_MOTE.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.VOID_SHARD.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.VOID_RIFT.get(), VoidParticle.Provider::new);
    }
}
