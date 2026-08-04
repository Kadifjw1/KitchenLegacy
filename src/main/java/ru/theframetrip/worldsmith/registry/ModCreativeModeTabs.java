package ru.theframetrip.worldsmith.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.worldsmith.WorldsmithMod;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WorldsmithMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WORLDSMITH_TAB = CREATIVE_MODE_TABS.register("worldsmith",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.worldsmith"))
                    .icon(() -> new ItemStack(ModItems.PREDEL.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.KOLOSS.get());
                        output.accept(ModItems.SHIP.get());
                        output.accept(ModItems.STRAZH.get());
                        output.accept(ModItems.YADRO.get());
                        output.accept(ModItems.BEZDNA.get());
                        output.accept(ModItems.RAZLOM.get());
                        output.accept(ModItems.SERP.get());
                        output.accept(ModItems.PALACH.get());
                        output.accept(ModItems.MONOLIT.get());
                        output.accept(ModItems.OSKOLOK.get());
                        output.accept(ModItems.PREDEL.get());
                        output.accept(ModItems.KROVOTOK.get());
                        output.accept(ModItems.PRAH.get());
                        output.accept(ModItems.NAKAL.get());
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> SAVVA_TAB = CREATIVE_MODE_TABS.register("savva",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.worldsmith.savva"))
                    .icon(() -> new ItemStack(ModItems.SAVVA_COIN.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.SAVVA_SPAWNER.get());
                        output.accept(ModItems.SAVVA_COIN.get());
                        output.accept(ModItems.TOMATO.get());
                        output.accept(ModItems.LETTUCE.get());
                        output.accept(ModItems.CUCUMBER.get());
                        output.accept(ModItems.STRAWBERRY.get());
                        output.accept(ModItems.BLUEBERRY.get());
                        output.accept(ModItems.RASPBERRY.get());
                        output.accept(ModItems.PEAR.get());
                        output.accept(ModItems.PEACH.get());
                        output.accept(ModItems.ORANGE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
