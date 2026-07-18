package ru.theframetrip.kitchenlegacy.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.entity.HamsterEntity;
import ru.theframetrip.kitchenlegacy.registry.ModEntities;

@Mod.EventBusSubscriber(modid = KitchenLegacyMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityEvents {
    private ModEntityEvents() {}

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.HAMSTER.get(), HamsterEntity.createAttributes().build());
    }
}
