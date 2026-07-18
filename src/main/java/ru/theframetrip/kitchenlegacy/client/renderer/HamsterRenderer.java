package ru.theframetrip.kitchenlegacy.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import ru.theframetrip.kitchenlegacy.client.model.HamsterModel;
import ru.theframetrip.kitchenlegacy.entity.HamsterEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class HamsterRenderer extends GeoEntityRenderer<HamsterEntity> {
    public HamsterRenderer(EntityRendererProvider.Context context) {
        super(context, new HamsterModel());
        this.shadowRadius = 0.25F;
    }
}
