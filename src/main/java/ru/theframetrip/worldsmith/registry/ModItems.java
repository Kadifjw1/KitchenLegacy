package ru.theframetrip.worldsmith.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.item.PredelItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WorldsmithMod.MOD_ID);

    public static final RegistryObject<Item> PREDEL = ITEMS.register("predel",
            () -> new PredelItem(Tiers.IRON, 8, -2.8F, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
