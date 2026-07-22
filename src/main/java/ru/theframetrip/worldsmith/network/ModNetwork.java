package ru.theframetrip.worldsmith.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import ru.theframetrip.worldsmith.WorldsmithMod;

public class ModNetwork {
    private static final String PROTOCOL="1";
    public static final SimpleChannel CHANNEL= NetworkRegistry.newSimpleChannel(new ResourceLocation(WorldsmithMod.MOD_ID,"main"),()->PROTOCOL,PROTOCOL::equals,PROTOCOL::equals);
    public static void register(){ CHANNEL.messageBuilder(UsePredelAbilityPacket.class,0).encoder(UsePredelAbilityPacket::encode).decoder(UsePredelAbilityPacket::new).consumerMainThread(UsePredelAbilityPacket::handle).add(); }
    public static void sendPredelUse(){ CHANNEL.sendToServer(new UsePredelAbilityPacket()); }
}
