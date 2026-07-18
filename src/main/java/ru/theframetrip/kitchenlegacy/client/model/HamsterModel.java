package ru.theframetrip.kitchenlegacy.client.model;

import net.minecraft.resources.ResourceLocation;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.entity.HamsterEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public final class HamsterModel extends DefaultedEntityGeoModel<HamsterEntity> {
    public HamsterModel() {
        super(new ResourceLocation(KitchenLegacyMod.MOD_ID, "hamster"));
    }
}
