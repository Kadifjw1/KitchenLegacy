package ru.theframetrip.kitchenlegacy.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.network.ModNetwork;

@Mod.EventBusSubscriber(modid=KitchenLegacyMod.MOD_ID,value=Dist.CLIENT)
public class ClientAbilityHandler {
    @Mod.EventBusSubscriber(modid=KitchenLegacyMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static class ModBus { @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){ e.register(ModKeyMappings.PREDEL_ABILITY); } }
    @SubscribeEvent public static void clientTick(TickEvent.ClientTickEvent e){ if(e.phase==TickEvent.Phase.END) while(ModKeyMappings.PREDEL_ABILITY.consumeClick()) ModNetwork.sendPredelUse(); }
}
