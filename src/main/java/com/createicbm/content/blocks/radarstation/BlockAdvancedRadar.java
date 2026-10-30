package com.createicbm.content.blocks.radarstation;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;

/**
 * Advanced Create-themed radar that coordinates counter-strikes against the launch point of incoming missiles.
 * The existing radar block entity and GUI are intentionally shared to keep channel/range controls consistent.
 */
public class BlockAdvancedRadar extends BlockRadarStation
{
    public static final MapCodec<BlockAdvancedRadar> CODEC = simpleCodec(p -> new BlockAdvancedRadar());

    public BlockAdvancedRadar()
    {
        super("advanced_radar");
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }
}
