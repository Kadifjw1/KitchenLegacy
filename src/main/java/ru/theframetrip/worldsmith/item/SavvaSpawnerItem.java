package ru.theframetrip.worldsmith.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import ru.theframetrip.worldsmith.forgemind.savva.SavvaProduceVendorFeature;

public final class SavvaSpawnerItem extends Item {
    public SavvaSpawnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        boolean spawned = SavvaProduceVendorFeature.spawnSavva(
                level,
                Vec3.atBottomCenterOf(spawnPos)
        );
        if (!spawned) {
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }
}
