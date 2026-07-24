package ru.theframetrip.worldsmith.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import ru.theframetrip.worldsmith.ability.prah.PrahAbilityManager;

import java.util.function.Supplier;

public final class UsePrahAbilityPacket {
    public UsePrahAbilityPacket() {
    }

    public UsePrahAbilityPacket(FriendlyByteBuf buffer) {
    }

    public void encode(FriendlyByteBuf buffer) {
    }

    public static void handle(UsePrahAbilityPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                PrahAbilityManager.use(sender);
            }
        });
        context.setPacketHandled(true);
    }
}
