package ru.theframetrip.worldsmith.forgemind.savva.shop;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.worldsmith.WorldsmithMod;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SavvaShopRegistry {
    public static final ResourceLocation ID = new ResourceLocation(
            WorldsmithMod.MOD_ID,
            "savva_shop"
    );
    public static final RegistryObject<MenuType<SavvaShopMenu>> SAVVA_SHOP =
            RegistryObject.create(ID, ForgeRegistries.MENU_TYPES);

    private SavvaShopRegistry() {
    }

    @SubscribeEvent
    public static void registerMenus(RegisterEvent event) {
        event.register(
                ForgeRegistries.Keys.MENU_TYPES,
                helper -> helper.register(ID, IForgeMenuType.create(SavvaShopMenu::new))
        );
    }
}
