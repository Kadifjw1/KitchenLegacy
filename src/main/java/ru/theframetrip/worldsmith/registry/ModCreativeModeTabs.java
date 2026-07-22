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
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
