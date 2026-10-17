package com.createicbm.content.radioactive;

import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.util.BlockReplacementData;
import com.createicbm.config.util.BlockStateConfigList;
import com.createicbm.content.blast.helpers.BlastBlockHelpers;
import com.createicbm.content.reg.BlockReg;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RadioactiveHandler {
    public static final BlockStateConfigList.BlockChanceOut radioactiveBlockSwaps = new BlockStateConfigList.BlockChanceOut("[RadioactiveReplacements]", (configList) -> {
        configList.setDefault(BuiltInRegistries.BLOCK.getKey(Blocks.STONE), new BlockReplacementData().setChance(0.2f).setBlock(BlockReg.blockRadioactiveStone.get().defaultBlockState()), 0);

        final BlockState radDirt = BlockReg.blockRadioactiveDirt.get().defaultBlockState();
        configList.setDefault(BuiltInRegistries.BLOCK.getKey(Blocks.DIRT), new BlockReplacementData().setChance(0.2f).setBlock(radDirt), 0);
        configList.setDefault(BuiltInRegistries.BLOCK.getKey(Blocks.FARMLAND), new BlockReplacementData().setChance(0.2f).setBlock(radDirt), 0);
        configList.setDefault(BuiltInRegistries.BLOCK.getKey(Blocks.DIRT_PATH), new BlockReplacementData().setChance(0.2f).setBlock(radDirt), 0);
        configList.setDefault(BuiltInRegistries.BLOCK.getKey(Blocks.GRASS_BLOCK), new BlockReplacementData().setChance(0.2f).setBlock(radDirt), 0);
        configList.setDefault(BuiltInRegistries.BLOCK.getKey(Blocks.MYCELIUM), new BlockReplacementData().setChance(0.2f).setBlock(radDirt), 0);

        for(Block block: BuiltInRegistries.BLOCK) {
            if(block instanceof LeavesBlock || BlastBlockHelpers.isPlantMaterial(block.defaultBlockState())) {
                configList.setDefault(BuiltInRegistries.BLOCK.getKey(block), new BlockReplacementData().setBlock(Blocks.AIR.defaultBlockState()), 0);
            }
        }

        configList.load("create_icbm/blast/nuclear/radioactive_replacements/list", ConfigBlast.nuclear.radiationReplacements.blockStates);
    });

    public static void loadFromConfig() {
        radioactiveBlockSwaps.reload();
    }

    public static void setup() {
        loadFromConfig();
    }
}
