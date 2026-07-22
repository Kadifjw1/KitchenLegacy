package ru.theframetrip.kitchenlegacy.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoidPhaseBlock extends Block {
    public VoidPhaseBlock(Properties properties) { super(properties); }
    @Override public VoxelShape getCollisionShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return EMPTY; }
    @Override public VoxelShape getShape(BlockState s, BlockGetter l, BlockPos p, CollisionContext c) { return EMPTY; }
    @Override public VoxelShape getInteractionShape(BlockState s, BlockGetter l, BlockPos p) { return EMPTY; }
    @Override public boolean propagatesSkylightDown(BlockState s, BlockGetter l, BlockPos p) { return true; }
    @Override public float getShadeBrightness(BlockState s, BlockGetter l, BlockPos p) { return 1.0F; }
}
