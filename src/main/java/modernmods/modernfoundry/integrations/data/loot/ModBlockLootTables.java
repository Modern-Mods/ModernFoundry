package modernmods.modernfoundry.integrations.data.loot;

import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import modernmods.modernfoundry.integrations.items.TciItems;

public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.dropSelf(TciItems.BRONZE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ImmutableList.of(TciItems.BRONZE.get());
    }

}
