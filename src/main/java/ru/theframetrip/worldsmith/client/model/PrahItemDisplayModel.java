package ru.theframetrip.worldsmith.client.model;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.joml.Vector3f;

import java.util.Map;

/**
 * Applies the approved Blockbench display transforms to the actual Prah item.
 *
 * <p>The large Prah model uses coordinates far outside the usual 16x16 item
 * workspace. The old generic sword transforms therefore showed the item as a
 * huge flat blade in first person and placed it diagonally across the player's
 * torso in third person. These values are copied from the supplied working
 * model rather than approximated from the particle silhouette.</p>
 */
public final class PrahItemDisplayModel extends BakedModelWrapper<BakedModel> {
    private static final float MODEL_UNIT = 1.0F / 16.0F;

    private static final ItemTransform THIRD_PERSON_RIGHT = transform(
            56.0F, -47.0F, 55.0F,
            -2.75F, 2.75F, 2.0F,
            0.45F
    );

    private static final ItemTransform THIRD_PERSON_LEFT = transform(
            0.0F, 90.0F, -55.0F,
            0.0F, 3.5F, 1.0F,
            0.42F
    );

    private static final ItemTransform FIRST_PERSON_RIGHT = transform(
            -25.0F, -47.0F, 14.0F,
            0.75F, 1.75F, 1.0F,
            0.52F
    );

    private static final ItemTransform FIRST_PERSON_LEFT = transform(
            0.0F, 90.0F, -25.0F,
            1.0F, 3.3F, 1.0F,
            0.52F
    );

    private static final ItemTransform HEAD = transform(
            0.0F, -180.0F, 0.0F,
            0.0F, 10.0F, 0.0F,
            0.58F
    );

    private static final ItemTransform GUI = transform(
            16.0F, -180.0F, 41.0F,
            -0.25F, -0.5F, -1.5F,
            0.48F
    );

    private static final ItemTransform GROUND = transform(
            94.0F, 42.0F, 0.0F,
            0.0F, 1.0F, 0.0F,
            0.28F
    );

    private static final ItemTransform FIXED = transform(
            0.0F, -180.0F, -36.0F,
            0.25F, 0.0F, 0.0F,
            0.55F
    );

    private static final ItemTransforms PRAH_TRANSFORMS = new ItemTransforms(
            THIRD_PERSON_LEFT,
            THIRD_PERSON_RIGHT,
            FIRST_PERSON_LEFT,
            FIRST_PERSON_RIGHT,
            HEAD,
            GUI,
            GROUND,
            FIXED,
            Map.of()
    );

    public PrahItemDisplayModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public ItemTransforms getTransforms() {
        return PRAH_TRANSFORMS;
    }

    private static ItemTransform transform(
            float rotationX,
            float rotationY,
            float rotationZ,
            float translationX,
            float translationY,
            float translationZ,
            float scale
    ) {
        return new ItemTransform(
                new Vector3f(rotationX, rotationY, rotationZ),
                new Vector3f(
                        translationX * MODEL_UNIT,
                        translationY * MODEL_UNIT,
                        translationZ * MODEL_UNIT
                ),
                new Vector3f(scale, scale, scale)
        );
    }
}
