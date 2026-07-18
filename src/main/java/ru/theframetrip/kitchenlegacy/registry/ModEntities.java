package ru.theframetrip.kitchenlegacy.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.entity.HamsterEntity;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, KitchenLegacyMod.MOD_ID);

    public static final RegistryObject<EntityType<HamsterEntity>> HAMSTER =
            ENTITY_TYPES.register("hamster", () -> EntityType.Builder
                    .of(HamsterEntity::new, MobCategory.CREATURE)
                    .sized(0.72F, 0.68F)
                    .clientTrackingRange(8)
                    .build(KitchenLegacyMod.MOD_ID + ":hamster"));

    private ModEntities() {}

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
