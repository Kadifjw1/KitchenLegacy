package ru.theframetrip.kitchenlegacy.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.item.BlankSwordItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KitchenLegacyMod.MOD_ID);

    public static final RegistryObject<Item> JERUSALEM_ARTICHOKE = registerItem("jerusalem_artichoke");
    public static final RegistryObject<Item> BLACK_RADISH = registerItem("black_radish");
    public static final RegistryObject<Item> TURNIP = registerItem("turnip");
    public static final RegistryObject<Item> ONION = registerItem("onion");
    public static final RegistryObject<Item> GARLIC = registerItem("garlic");
    public static final RegistryObject<Item> CABBAGE = registerItem("cabbage");
    public static final RegistryObject<Item> BROCCOLI = registerItem("broccoli");
    public static final RegistryObject<Item> CAULIFLOWER = registerItem("cauliflower");
    public static final RegistryObject<Item> CUCUMBER = registerItem("cucumber");
    public static final RegistryObject<Item> TOMATO = registerItem("tomato");
    public static final RegistryObject<Item> SWEET_BELL_PEPPER = registerItem("sweet_bell_pepper");
    public static final RegistryObject<Item> HOT_CHILI_PEPPER = registerItem("hot_chili_pepper");
    public static final RegistryObject<Item> EGGPLANT = registerItem("eggplant");
    public static final RegistryObject<Item> ZUCCHINI = registerItem("zucchini");
    public static final RegistryObject<Item> PATTYPAN_SQUASH = registerItem("pattypan_squash");
    public static final RegistryObject<Item> RADISH = registerItem("radish");
    public static final RegistryObject<Item> WHITE_TURNIP = registerItem("white_turnip");
    public static final RegistryObject<Item> SWEET_POTATO = registerItem("sweet_potato");
    public static final RegistryObject<Item> PARSNIP = registerItem("parsnip");
    public static final RegistryObject<Item> RUTABAGA = registerItem("rutabaga");
    public static final RegistryObject<Item> DAIKON_RADISH = registerItem("daikon_radish");
    public static final RegistryObject<Item> PEA_POD = registerItem("pea_pod");
    public static final RegistryObject<Item> GREEN_BEANS = registerItem("green_beans");
    public static final RegistryObject<Item> LENTILS = registerItem("lentils");
    public static final RegistryObject<Item> CELERY = registerItem("celery");
    public static final RegistryObject<Item> ASPARAGUS = registerItem("asparagus");
    public static final RegistryObject<Item> ARTICHOKE = registerItem("artichoke");
    public static final RegistryObject<Item> SPINACH = registerItem("spinach");
    public static final RegistryObject<Item> LETTUCE = registerItem("lettuce");
    public static final RegistryObject<Item> GREEN_ONION = registerItem("green_onion");
    public static final RegistryObject<Item> LEEK = registerItem("leek");
    public static final RegistryObject<Item> GINGER_ROOT = registerItem("ginger_root");
    public static final RegistryObject<Item> HORSERADISH_ROOT = registerItem("horseradish_root");
    public static final RegistryObject<Item> RHUBARB = registerItem("rhubarb");
    public static final RegistryObject<Item> SEAWEED_KELP = registerItem("seaweed_kelp");

    public static final RegistryObject<Item> QUINCE = registerItem("quince");
    public static final RegistryObject<Item> PEAR = registerItem("pear");
    public static final RegistryObject<Item> PLUM = registerItem("plum");
    public static final RegistryObject<Item> CHERRY = registerItem("cherry");
    public static final RegistryObject<Item> PEACH = registerItem("peach");
    public static final RegistryObject<Item> APRICOT = registerItem("apricot");
    public static final RegistryObject<Item> LEMON = registerItem("lemon");
    public static final RegistryObject<Item> ORANGE = registerItem("orange");
    public static final RegistryObject<Item> MANDARIN = registerItem("mandarin");
    public static final RegistryObject<Item> LIME = registerItem("lime");
    public static final RegistryObject<Item> POMEGRANATE = registerSimpleItem("pomegranate");
    public static final RegistryObject<Item> FIG = registerSimpleItem("fig");
    public static final RegistryObject<Item> DATE = registerSimpleItem("date");
    public static final RegistryObject<Item> BANANA = registerSimpleItem("banana");
    public static final RegistryObject<Item> MANGO = registerSimpleItem("mango");
    public static final RegistryObject<Item> PINEAPPLE = registerSimpleItem("pineapple");
    public static final RegistryObject<Item> COCONUT = registerSimpleItem("coconut");
    public static final RegistryObject<Item> AVOCADO = registerSimpleItem("avocado");
    public static final RegistryObject<Item> KIWI = registerSimpleItem("kiwi");
    public static final RegistryObject<Item> PERSIMMON = registerSimpleItem("persimmon");
    public static final RegistryObject<Item> PAPAYA = registerSimpleItem("papaya");
    public static final RegistryObject<Item> PASSION_FRUIT = registerSimpleItem("passion_fruit");
    public static final RegistryObject<Item> DRAGON_FRUIT = registerSimpleItem("dragon_fruit");

    public static final RegistryObject<Item> KOLOSS = registerBlankSword("koloss", 9, -3.1F);
    public static final RegistryObject<Item> SHIP = registerBlankSword("ship", 5, -2.4F);
    public static final RegistryObject<Item> STRAZH = registerBlankSword("strazh", 6, -2.6F);
    public static final RegistryObject<Item> YADRO = registerBlankSword("yadro", 6, -2.5F);
    public static final RegistryObject<Item> BEZDNA = registerBlankSword("bezdna", 8, -3.0F);
    public static final RegistryObject<Item> RAZLOM = registerBlankSword("razlom", 7, -2.8F);
    public static final RegistryObject<Item> SERP = registerBlankSword("serp", 4, -2.2F);
    public static final RegistryObject<Item> PALACH = registerBlankSword("palach", 10, -3.2F);
    public static final RegistryObject<Item> MONOLIT = registerBlankSword("monolit", 11, -3.3F);
    public static final RegistryObject<Item> OSKOLOK = registerBlankSword("oskolok", 7, -2.7F);

    private static RegistryObject<Item> registerItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> registerSimpleItem(String name) {
        return registerItem(name);
    }

    private static RegistryObject<Item> registerBlankSword(String name, int attackDamageModifier, float attackSpeedModifier) {
        return ITEMS.register(name, () -> new BlankSwordItem(Tiers.IRON, attackDamageModifier, attackSpeedModifier, new Item.Properties()));
    }
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
