package ru.theframetrip.worldsmith.ability.predel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import java.util.*;

public class PredelRiftSavedData extends SavedData {
    public static final String NAME = "worldsmith_predel_rifts";
    public final List<RiftInstance> rifts = new ArrayList<>();
    public static PredelRiftSavedData load(CompoundTag tag) {
        PredelRiftSavedData data = new PredelRiftSavedData();
        ListTag list = tag.getList("Rifts", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag r = (CompoundTag)t;
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(r.getString("Dimension")));
            List<RiftBlockSnapshot> blocks = new ArrayList<>();
            for (Tag bt : r.getList("Blocks", Tag.TAG_COMPOUND)) {
                CompoundTag b=(CompoundTag)bt;
                blocks.add(new RiftBlockSnapshot(BlockPos.of(b.getLong("Pos")), NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), b.getCompound("State"))));
            }
            data.rifts.add(new RiftInstance(r.getUUID("Id"), r.getUUID("Owner"), dim, blocks, r.getLong("Created"), 0));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (RiftInstance ri : rifts) {
            CompoundTag r = new CompoundTag(); r.putUUID("Id", ri.riftId()); r.putUUID("Owner", ri.ownerPlayerId()); r.putString("Dimension", ri.dimension().location().toString()); r.putLong("Created", ri.createdGameTime()); r.putLong("Restore", ri.restoreGameTime());
            ListTag bl = new ListTag();
            for (RiftBlockSnapshot s: ri.blocks()) { CompoundTag b=new CompoundTag(); b.putLong("Pos", s.position().asLong()); b.put("State", NbtUtils.writeBlockState(s.originalState())); bl.add(b); }
            r.put("Blocks", bl); list.add(r);
        }
        tag.put("Rifts", list); return tag;
    }
}
