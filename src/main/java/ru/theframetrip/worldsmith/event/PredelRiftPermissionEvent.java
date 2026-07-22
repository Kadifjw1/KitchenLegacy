package ru.theframetrip.worldsmith.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class PredelRiftPermissionEvent extends Event {
    private final ServerPlayer player; private final Level level; private final BlockPos pos; private final BlockState state;
    public PredelRiftPermissionEvent(ServerPlayer player, Level level, BlockPos pos, BlockState state) { this.player = player; this.level = level; this.pos = pos; this.state = state; }
    public ServerPlayer getPlayer() { return player; } public Level getLevel() { return level; } public BlockPos getPos() { return pos; } public BlockState getState() { return state; }
}
