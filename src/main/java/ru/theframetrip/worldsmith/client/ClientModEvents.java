package ru.theframetrip.worldsmith.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.theframetrip.worldsmith.item.KrovotokItem;
import ru.theframetrip.worldsmith.registry.ModItems;
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
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_MIST.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_SPARK.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_PULSE.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_BURST.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_LIFE_DRAIN.get(), VoidParticle.Provider::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.KROVOTOK.get(),
                new ResourceLocation(WorldsmithMod.MOD_ID, "krovotok_charge"),
                (stack, level, entity, seed) -> KrovotokItem.getCharge(stack) / (float) KrovotokItem.MAX_CHARGE));
    }
}
