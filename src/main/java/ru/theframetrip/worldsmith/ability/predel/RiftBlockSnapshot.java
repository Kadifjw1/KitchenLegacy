package ru.theframetrip.worldsmith.ability.predel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record RiftBlockSnapshot(BlockPos position, BlockState originalState) {}
