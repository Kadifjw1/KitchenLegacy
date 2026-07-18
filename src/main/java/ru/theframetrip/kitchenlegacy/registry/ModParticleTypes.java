package ru.theframetrip.kitchenlegacy.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;

public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, KitchenLegacyMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> FLAME_SPARK =
            PARTICLE_TYPES.register("flame_spark", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FLAME_EMBER =
            PARTICLE_TYPES.register("flame_ember", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FLAME_SMOKE =
            PARTICLE_TYPES.register("flame_smoke", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_SLASH =
            PARTICLE_TYPES.register("fire_slash", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_HIT =
            PARTICLE_TYPES.register("fire_hit", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_BURST =
            PARTICLE_TYPES.register("fire_burst", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_CHARGE =
            PARTICLE_TYPES.register("fire_charge", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_TRAIL =
            PARTICLE_TYPES.register("fire_trail", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_GROUND =
            PARTICLE_TYPES.register("fire_ground", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_WAVE =
            PARTICLE_TYPES.register("fire_wave", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
