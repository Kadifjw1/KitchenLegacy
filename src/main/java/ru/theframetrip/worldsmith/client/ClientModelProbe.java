package ru.theframetrip.worldsmith.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.theframetrip.worldsmith.WorldsmithMod;

@Mod.EventBusSubscriber(modid = WorldsmithMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModelProbe {
    private static final String ENVIRONMENT_FLAG = "WORLDSMITH_MODEL_PROBE";

    private ClientModelProbe() {
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        if (!"1".equals(System.getenv(ENVIRONMENT_FLAG))) {
            return;
        }

        int krovotokQuads = inspect(event, "krovotok");
        int prahQuads = inspect(event, "prah");

        if (krovotokQuads <= 0 || prahQuads <= 0) {
            throw new IllegalStateException(
                    "WORLDSMITH_MODEL_PROBE_FAILED krovotok=" + krovotokQuads + " prah=" + prahQuads
            );
        }

        throw new IllegalStateException(
                "WORLDSMITH_MODEL_PROBE_COMPLETE krovotok=" + krovotokQuads + " prah=" + prahQuads
        );
    }

    private static int inspect(ModelEvent.BakingCompleted event, String itemName) {
        ModelResourceLocation modelLocation = new ModelResourceLocation(
                new ResourceLocation(WorldsmithMod.MOD_ID, itemName),
                "inventory"
        );
        BakedModel model = event.getModels().get(modelLocation);
        if (model == null) {
            WorldsmithMod.LOGGER.error("WORLDSMITH_MODEL_PROBE item={} model=null", itemName);
            return -1;
        }

        boolean missing = model == event.getModelManager().getMissingModel();
        RandomSource random = RandomSource.create(0L);
        int quads = model.getQuads(null, null, random).size();
        for (Direction direction : Direction.values()) {
            quads += model.getQuads(null, direction, random).size();
        }

        WorldsmithMod.LOGGER.error(
                "WORLDSMITH_MODEL_PROBE item={} missing={} customRenderer={} quads={} modelClass={}",
                itemName,
                missing,
                model.isCustomRenderer(),
                quads,
                model.getClass().getName()
        );
        return missing ? -1 : quads;
    }
}
