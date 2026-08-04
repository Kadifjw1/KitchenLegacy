package ru.theframetrip.worldsmith.registry;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.item.KrovotokItem;
import ru.theframetrip.worldsmith.item.NakalItem;
import ru.theframetrip.worldsmith.item.PredelItem;
import ru.theframetrip.worldsmith.item.SavvaSpawnerItem;
import ru.theframetrip.worldsmith.item.WorldsmithSwordItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WorldsmithMod.MOD_ID);

    public static final RegistryObject<Item> KOLOSS = registerSword("koloss", 9, -3.1F);
    public static final RegistryObject<Item> SHIP = registerSword("ship", 5, -2.4F);
    public static final RegistryObject<Item> STRAZH = registerSword("strazh", 6, -2.6F);
    public static final RegistryObject<Item> YADRO = registerSword("yadro", 6, -2.5F);
    public static final RegistryObject<Item> BEZDNA = registerSword("bezdna", 8, -3.0F);
    public static final RegistryObject<Item> RAZLOM = registerSword("razlom", 7, -2.8F);
    public static final RegistryObject<Item> SERP = registerSword("serp", 4, -2.2F);
    public static final RegistryObject<Item> PALACH = registerSword("palach", 10, -3.2F);
    public static final RegistryObject<Item> MONOLIT = registerSword("monolit", 11, -3.3F);
    public static final RegistryObject<Item> OSKOLOK = registerSword("oskolok", 7, -2.7F);
    public static final RegistryObject<Item> PREDEL = ITEMS.register("predel",
            () -> new PredelItem(Tiers.IRON, 8, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> KROVOTOK = ITEMS.register("krovotok",
            () -> new KrovotokItem(Tiers.IRON, 7, -2.6F, new Item.Properties()));
    public static final RegistryObject<Item> PRAH = registerSword("prah", 7, -2.7F);
    public static final RegistryObject<Item> NAKAL = ITEMS.register("nakal",
            () -> new NakalItem(Tiers.IRON, 7, -2.7F, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> SAVVA_COIN = ITEMS.register("savva_coin",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TOMATO = ITEMS.register("tomato",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).build()
            )));
    public static final RegistryObject<Item> LETTUCE = ITEMS.register("lettuce",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(1).saturationMod(0.2F).build()
            )));
    public static final RegistryObject<Item> CUCUMBER = ITEMS.register("cucumber",
            () -> new Item(new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(2).saturationMod(0.25F).build()
            )));
    public static final RegistryObject<Item> SAVVA_SPAWNER = ITEMS.register("savva_spawner",
            () -> new SavvaSpawnerItem(new Item.Properties().stacksTo(16)));

    private static RegistryObject<Item> registerSword(String name, int attackDamageModifier, float attackSpeedModifier) {
        return ITEMS.register(name, () -> new WorldsmithSwordItem(Tiers.IRON, attackDamageModifier, attackSpeedModifier, new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
