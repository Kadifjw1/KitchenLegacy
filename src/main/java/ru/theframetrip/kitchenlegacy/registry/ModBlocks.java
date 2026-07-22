package ru.theframetrip.kitchenlegacy.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import ru.theframetrip.kitchenlegacy.KitchenLegacyMod;
import ru.theframetrip.kitchenlegacy.block.VoidPhaseBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KitchenLegacyMod.MOD_ID);
    public static final RegistryObject<Block> VOID_PHASE_BLOCK = BLOCKS.register("void_phase_block", () -> new VoidPhaseBlock(
            BlockBehaviour.Properties.copy(Blocks.BARRIER).noCollission().noOcclusion().strength(-1.0F, 3600000.0F).noLootTable().sound(SoundType.EMPTY)));
    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
