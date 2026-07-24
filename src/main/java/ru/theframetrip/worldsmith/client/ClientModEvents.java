package ru.theframetrip.worldsmith.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.client.particle.KrovotokParticle;
import ru.theframetrip.worldsmith.client.particle.VoidParticle;
import ru.theframetrip.worldsmith.client.render.KrovotokItemRenderer;
import ru.theframetrip.worldsmith.item.KrovotokItem;
import ru.theframetrip.worldsmith.registry.ModItems;
import ru.theframetrip.worldsmith.registry.ModParticleTypes;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.VOID_MOTE.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.VOID_SHARD.get(), VoidParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.VOID_RIFT.get(), VoidParticle.Provider::new);

        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_MIST.get(), KrovotokParticle.MistProvider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_SPARK.get(), KrovotokParticle.SparkProvider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_PULSE.get(), KrovotokParticle.PulseProvider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_BLOOD_BURST.get(), KrovotokParticle.BurstProvider::new);
        event.registerSpriteSet(ModParticleTypes.KROVOTOK_LIFE_DRAIN.get(), KrovotokParticle.LifeDrainProvider::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (int charge = 0; charge <= KrovotokItem.MAX_CHARGE; charge++) {
            event.register(KrovotokItemRenderer.baseModel(charge));
            event.register(KrovotokItemRenderer.glowModel(charge));
        }
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.KROVOTOK.get(),
                new ResourceLocation(WorldsmithMod.MOD_ID, "krovotok_charge"),
                (stack, level, entity, seed) -> KrovotokItem.getCharge(stack) / (float) KrovotokItem.MAX_CHARGE));
    }
}
