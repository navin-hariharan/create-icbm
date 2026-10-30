package com.createicbm.content.reg;

import com.createicbm.ICBMConstants;
import com.createicbm.content.blocks.explosive.ItemBlockExplosive;
import com.createicbm.content.items.ItemAntidote;
import com.createicbm.content.items.ItemBombCart;
import com.createicbm.content.items.ItemLaserDetonator;
import com.createicbm.content.items.ItemMissile;
import com.createicbm.content.items.ItemRadarGun;
import com.createicbm.content.items.ItemRemoteDetonator;
import com.createicbm.content.items.ItemRocketLauncher;
import com.createicbm.content.missile.entity.anti.item.ItemSurfaceToAirMissile;
import com.createicbm.content.missile.entity.delivery.ItemDeliveryMissile;
import com.createicbm.prefab.item.ItemBase;
import com.createicbm.prefab.item.ItemBlockAbstract;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry.
 * ICBM's own crafting parts (steel, plates, circuits, wires, saltpeter, sulfur) and the battery were removed for the
 * Create addon: recipes use Create and vanilla items.
 * <p>
 * Explosive variants (missiles, carts, explosive blocks) are NOT flattened, the
 * explosive is stored in {@link ComponentReg#EXPLOSIVE}.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ItemReg
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ICBMConstants.DOMAIN);

    public static final DeferredItem<ItemBombCart> itemBombCart = ITEMS.register("bombcart", () -> (ItemBombCart) new ItemBombCart().setName("bombcart"));
    public static final DeferredItem<Item> itemPoisonPowder = ITEMS.register("poisonpowder", () -> new ItemBase().setName("poisonPowder"));
    public static final DeferredItem<ItemAntidote> itemAntidote = ITEMS.register("antidote", () -> (ItemAntidote) new ItemAntidote().setName("antidote"));
    public static final DeferredItem<ItemRadarGun> itemRadarGun = ITEMS.register("radargun", ItemRadarGun::new);
    public static final DeferredItem<ItemRemoteDetonator> itemRemoteDetonator = ITEMS.register("remotedetonator", ItemRemoteDetonator::new);
    public static final DeferredItem<ItemLaserDetonator> itemLaserDetonator = ITEMS.register("laserdetonator", ItemLaserDetonator::new);
    public static final DeferredItem<ItemRocketLauncher> itemRocketLauncher = ITEMS.register("rocketlauncher", () -> new ItemRocketLauncher(false));
    public static final DeferredItem<ItemRocketLauncher> itemBallisticLauncher = ITEMS.register("ballisticlauncher", () -> new ItemRocketLauncher(true));
    public static final DeferredItem<ItemMissile> itemExplosiveMissile = ITEMS.register("explosive_missile", () -> (ItemMissile) new ItemMissile().setName("explosive_missile"));
    public static final DeferredItem<ItemSurfaceToAirMissile> itemSAM = ITEMS.register("surface_to_air_missile", ItemSurfaceToAirMissile::new);
    /** Carries one stack of items to the target */
    public static final DeferredItem<ItemDeliveryMissile> itemDeliveryMissile = ITEMS.register("delivery_missile", ItemDeliveryMissile::new);

    //Block items
    public static final DeferredItem<ItemBlockExplosive> itemBlockExplosive = ITEMS.register("explosives", () -> new ItemBlockExplosive(BlockReg.blockExplosive.get()));
    public static final DeferredItem<BlockItem> itemRadarStation = blockItem(BlockReg.blockRadarStation);
    public static final DeferredItem<BlockItem> itemAdvancedRadar = blockItem(BlockReg.blockAdvancedRadar);
    public static final DeferredItem<BlockItem> itemLaunchSupport = blockItem(BlockReg.blockLaunchSupport);
    public static final DeferredItem<BlockItem> itemLaunchBase = blockItem(BlockReg.blockLaunchBase);
    public static final DeferredItem<BlockItem> itemLaunchConnector = blockItem(BlockReg.blockLaunchConnector);
    public static final DeferredItem<BlockItem> itemLaunchScreen = blockItem(BlockReg.blockLaunchScreen);
    public static final DeferredItem<BlockItem> itemCruiseLauncher = blockItem(BlockReg.blockCruiseLauncher);
    public static final DeferredItem<BlockItem> itemRadioactiveDirt = blockItem(BlockReg.blockRadioactiveDirt);
    public static final DeferredItem<BlockItem> itemRadioactiveStone = blockItem(BlockReg.blockRadioactiveStone);

    //Crafting parts for Create recipes
    /** Rocket body, the input of the missile module recipes */
    public static final DeferredItem<Item> itemMissileCasing = ITEMS.register("missile_casing", () -> new Item(new Item.Properties()));

    private static DeferredItem<BlockItem> blockItem(DeferredBlock<? extends Block> block)
    {
        return ITEMS.register(block.getId().getPath(), () -> new ItemBlockAbstract(block.get()));
    }

    public static void register(IEventBus modBus)
    {
        ITEMS.register(modBus);
    }
}
