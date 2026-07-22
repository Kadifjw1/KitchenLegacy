package ru.theframetrip.worldsmith.ability.predel;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.*;

public record RiftInstance(UUID riftId, UUID ownerPlayerId, ResourceKey<Level> dimension, List<RiftBlockSnapshot> blocks, long createdGameTime, long restoreGameTime) {}
