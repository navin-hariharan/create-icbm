package com.createicbm.content.reg;

import com.createicbm.content.blocks.mail.BlockMail;
import com.createicbm.content.blocks.mail.TileMail;
import com.createicbm.ICBMConstants;
import com.createicbm.content.blocks.explosive.BlockExplosive;
import com.createicbm.content.blocks.explosive.TileEntityExplosive;
import com.createicbm.content.blocks.launcher.base.BlockLauncherBase;
import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.blocks.launcher.connector.BlockLaunchConnector;
import com.createicbm.content.blocks.launcher.connector.TileLauncherConnector;
import com.createicbm.content.blocks.launcher.cruise.BlockCruiseLauncher;
import com.createicbm.content.blocks.launcher.cruise.TileCruiseLauncher;
import com.createicbm.content.blocks.launcher.frame.BlockLaunchFrame;
import com.createicbm.content.blocks.launcher.frame.TileLauncherFrame;
import com.createicbm.content.blocks.launcher.screen.BlockLaunchScreen;
import com.createicbm.content.blocks.launcher.screen.TileLauncherScreen;
import com.createicbm.content.blocks.radarstation.BlockRadarStation;
import com.createicbm.content.blocks.radarstation.BlockAdvancedRadar;
import com.createicbm.content.blocks.radarstation.TileRadarStation;
import com.createicbm.content.radioactive.BlockRadioactive;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * Block and block entity registry.
 * Meta variants were flattened into their own blocks:
 * <ul>
 *     <li>radioactive:0/1 -> radioactive_dirt, radioactive_stone</li>
 * </ul>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class BlockReg
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ICBMConstants.DOMAIN);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ICBMConstants.DOMAIN);

    public static final DeferredBlock<BlockExplosive> blockExplosive = BLOCKS.register("explosives", BlockExplosive::new);

    public static final DeferredBlock<BlockLauncherBase> blockLaunchBase = BLOCKS.register("launcherbase", BlockLauncherBase::new);
    public static final DeferredBlock<BlockLaunchScreen> blockLaunchScreen = BLOCKS.register("launcherscreen", BlockLaunchScreen::new);
    public static final DeferredBlock<BlockLaunchFrame> blockLaunchSupport = BLOCKS.register("launcherframe", BlockLaunchFrame::new);
    public static final DeferredBlock<BlockLaunchConnector> blockLaunchConnector = BLOCKS.register("launcher_connector", BlockLaunchConnector::new);
    public static final DeferredBlock<BlockRadarStation> blockRadarStation = BLOCKS.register("radarstation", BlockRadarStation::new);
    public static final DeferredBlock<BlockAdvancedRadar> blockAdvancedRadar = BLOCKS.register("advanced_radar", BlockAdvancedRadar::new);
    public static final DeferredBlock<BlockCruiseLauncher> blockCruiseLauncher = BLOCKS.register("cruiselauncher", BlockCruiseLauncher::new);

    /** Parcel left by delivery missiles, has no item on purpose */
    public static final DeferredBlock<BlockMail> blockMail = BLOCKS.register("mail", BlockMail::new);

    public static final DeferredBlock<BlockRadioactive> blockRadioactiveDirt = BLOCKS.register("radioactive_dirt", () -> new BlockRadioactive(BlockRadioactive.EnumType.DIRT));
    public static final DeferredBlock<BlockRadioactive> blockRadioactiveStone = BLOCKS.register("radioactive_stone", () -> new BlockRadioactive(BlockRadioactive.EnumType.STONE));

    // Block entities, constructors are (BlockPos, BlockState)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityExplosive>> TILE_EXPLOSIVE = tile("explosive", TileEntityExplosive::new, blockExplosive);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRadarStation>> TILE_RADAR_STATION = tile("radarstation", TileRadarStation::new, blockRadarStation, blockAdvancedRadar);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherFrame>> TILE_LAUNCHER_FRAME = tile("launcherframe", TileLauncherFrame::new, blockLaunchSupport);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherConnector>> TILE_LAUNCHER_CONNECTOR = tile("launcher_connector", TileLauncherConnector::new, blockLaunchConnector);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherBase>> TILE_LAUNCHER_BASE = tile("launcherbase", TileLauncherBase::new, blockLaunchBase);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherScreen>> TILE_LAUNCHER_SCREEN = tile("launcherscreen", TileLauncherScreen::new, blockLaunchScreen);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCruiseLauncher>> TILE_CRUISE_LAUNCHER = tile("cruiselauncher", TileCruiseLauncher::new, blockCruiseLauncher);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileMail>> TILE_MAIL = tile("mail", TileMail::new, blockMail);

    /** Every Create: ICBM block entity, shared by optional client-render compatibility hooks. */
    public static final List<Supplier<? extends BlockEntityType<?>>> ALL_BLOCK_ENTITY_TYPES = List.of(
        TILE_EXPLOSIVE, TILE_RADAR_STATION, TILE_LAUNCHER_FRAME, TILE_LAUNCHER_CONNECTOR,
        TILE_LAUNCHER_BASE, TILE_LAUNCHER_SCREEN, TILE_CRUISE_LAUNCHER, TILE_MAIL
    );

    @SafeVarargs
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> tile(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block>... blocks)
    {
        return BLOCK_ENTITIES.register(name, () -> {
            final Block[] validBlocks = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++)
            {
                validBlocks[i] = blocks[i].get();
            }
            //noinspection DataFlowIssue
            return BlockEntityType.Builder.of(factory, validBlocks).build(null);
        });
    }

    public static void register(IEventBus modBus)
    {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
