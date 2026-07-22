package ru.theframetrip.worldsmith.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import ru.theframetrip.worldsmith.ability.predel.PredelRiftManager;
import java.util.function.Supplier;

public class UsePredelAbilityPacket {
    public UsePredelAbilityPacket() {}
    public UsePredelAbilityPacket(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}
    public static void handle(UsePredelAbilityPacket msg, Supplier<NetworkEvent.Context> ctx) { ctx.get().enqueueWork(() -> { if(ctx.get().getSender()!=null) PredelRiftManager.use(ctx.get().getSender()); }); ctx.get().setPacketHandled(true); }
}
