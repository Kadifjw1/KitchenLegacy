package ru.theframetrip.worldsmith.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.worldsmith.WorldsmithMod;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, WorldsmithMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> VOID_MOTE =
            PARTICLE_TYPES.register("void_mote", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> VOID_SHARD =
            PARTICLE_TYPES.register("void_shard", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> VOID_RIFT =
            PARTICLE_TYPES.register("void_rift", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
