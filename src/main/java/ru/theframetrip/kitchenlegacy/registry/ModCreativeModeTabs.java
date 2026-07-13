package ru.theframetrip.kitchenlegacy.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KitchenLegacyMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> KITCHEN_LEGACY_TAB = CREATIVE_MODE_TABS.register("kitchenlegacy",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kitchenlegacy"))
                    .icon(() -> new ItemStack(ModItems.TOMATO.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.JERUSALEM_ARTICHOKE.get());
                        output.accept(ModItems.BLACK_RADISH.get());
                        output.accept(ModItems.TURNIP.get());
                        output.accept(ModItems.ONION.get());
                        output.accept(ModItems.GARLIC.get());
                        output.accept(ModItems.CABBAGE.get());
                        output.accept(ModItems.BROCCOLI.get());
                        output.accept(ModItems.CAULIFLOWER.get());
                        output.accept(ModItems.CUCUMBER.get());
                        output.accept(ModItems.TOMATO.get());
                        output.accept(ModItems.SWEET_BELL_PEPPER.get());
                        output.accept(ModItems.HOT_CHILI_PEPPER.get());
                        output.accept(ModItems.EGGPLANT.get());
                        output.accept(ModItems.ZUCCHINI.get());
                        output.accept(ModItems.PATTYPAN_SQUASH.get());
                        output.accept(ModItems.RADISH.get());
                        output.accept(ModItems.WHITE_TURNIP.get());
                        output.accept(ModItems.SWEET_POTATO.get());
                        output.accept(ModItems.PARSNIP.get());
                        output.accept(ModItems.RUTABAGA.get());
                        output.accept(ModItems.DAIKON_RADISH.get());
                        output.accept(ModItems.PEA_POD.get());
                        output.accept(ModItems.GREEN_BEANS.get());
                        output.accept(ModItems.LENTILS.get());
                        output.accept(ModItems.CELERY.get());
                        output.accept(ModItems.ASPARAGUS.get());
                        output.accept(ModItems.ARTICHOKE.get());
                        output.accept(ModItems.SPINACH.get());
                        output.accept(ModItems.LETTUCE.get());
                        output.accept(ModItems.GREEN_ONION.get());
                        output.accept(ModItems.LEEK.get());
                        output.accept(ModItems.GINGER_ROOT.get());
                        output.accept(ModItems.HORSERADISH_ROOT.get());
                        output.accept(ModItems.RHUBARB.get());
                        output.accept(ModItems.SEAWEED_KELP.get());

                        output.accept(ModItems.QUINCE.get());
                        output.accept(ModItems.PEAR.get());
                        output.accept(ModItems.PLUM.get());
                        output.accept(ModItems.CHERRY.get());
                        output.accept(ModItems.PEACH.get());
                        output.accept(ModItems.APRICOT.get());
                        output.accept(ModItems.LEMON.get());
                        output.accept(ModItems.ORANGE.get());
                        output.accept(ModItems.MANDARIN.get());
                        output.accept(ModItems.LIME.get());
                        output.accept(ModItems.POMEGRANATE.get());
                        output.accept(ModItems.FIG.get());
                        output.accept(ModItems.DATE.get());
                        output.accept(ModItems.BANANA.get());
                        output.accept(ModItems.MANGO.get());
                        output.accept(ModItems.PINEAPPLE.get());
                        output.accept(ModItems.COCONUT.get());
                        output.accept(ModItems.AVOCADO.get());
                        output.accept(ModItems.KIWI.get());
                        output.accept(ModItems.PERSIMMON.get());
                        output.accept(ModItems.PAPAYA.get());
                        output.accept(ModItems.PASSION_FRUIT.get());
                        output.accept(ModItems.DRAGON_FRUIT.get());

                        output.accept(ModItems.FANTASY_SWORD_VARIANT_2.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
