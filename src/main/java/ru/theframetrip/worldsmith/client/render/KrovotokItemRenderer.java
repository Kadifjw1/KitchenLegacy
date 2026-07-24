package ru.theframetrip.worldsmith.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import ru.theframetrip.worldsmith.WorldsmithMod;
import ru.theframetrip.worldsmith.item.KrovotokItem;

/**
 * Renders Krovotok in two passes:
 * 1. the normal charge-stage model using world lighting;
 * 2. an alpha-only emissive model at full brightness.
 *
 * <p>The approved item-display transform is stored on the root
 * {@code worldsmith:item/krovotok} model. Minecraft applies that transform
 * before invoking this custom renderer. The stage models are therefore
 * rendered with {@link ItemDisplayContext#NONE} so the transform is not
 * applied a second time.</p>
 *
 * <p>Vanilla ItemRenderer also translates the pose by {@code -0.5} before
 * entering a custom renderer. Calling ItemRenderer again for the stage model
 * would otherwise apply that centering translation twice. The matching
 * {@code +0.5} correction below removes the outer centering before the nested
 * render call, leaving exactly one normal model-centering translation.</p>
 */
public final class KrovotokItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ModelResourceLocation[] BASE_MODELS = createModels("krovotok_charge_");
    private static final ModelResourceLocation[] GLOW_MODELS = createModels("krovotok_glow_");

    public KrovotokItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private static ModelResourceLocation[] createModels(String prefix) {
        ModelResourceLocation[] models = new ModelResourceLocation[KrovotokItem.MAX_CHARGE + 1];
        for (int charge = 0; charge <= KrovotokItem.MAX_CHARGE; charge++) {
            models[charge] = new ModelResourceLocation(
                    new ResourceLocation(WorldsmithMod.MOD_ID, "item/" + prefix + charge),
                    "inventory"
            );
        }
        return models;
    }

    public static ModelResourceLocation baseModel(int charge) {
        return BASE_MODELS[clampCharge(charge)];
    }

    public static ModelResourceLocation glowModel(int charge) {
        return GLOW_MODELS[clampCharge(charge)];
    }

    private static int clampCharge(int charge) {
        return Math.max(0, Math.min(KrovotokItem.MAX_CHARGE, charge));
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderer itemRenderer = minecraft.getItemRenderer();
        int charge = clampCharge(KrovotokItem.getCharge(stack));

        BakedModel base = itemRenderer.getItemModelShaper()
                .getModelManager()
                .getModel(BASE_MODELS[charge]);
        BakedModel glow = itemRenderer.getItemModelShaper()
                .getModelManager()
                .getModel(GLOW_MODELS[charge]);

        poseStack.pushPose();
        // Undo the centering translation already applied before BEWLR entry.
        // The nested ItemRenderer call below will apply the single required
        // -0.5 model-centering translation itself.
        poseStack.translate(0.5D, 0.5D, 0.5D);

        itemRenderer.render(
                stack,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay,
                base
        );

        itemRenderer.render(
                stack,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                glow
        );

        poseStack.popPose();
    }
}
