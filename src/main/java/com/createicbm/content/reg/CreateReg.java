package com.createicbm.content.reg;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.createicbm.config.machines.ConfigLauncher;
import com.createicbm.config.machines.ConfigRadar;
import com.createicbm.content.items.ExplosiveTooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

/**
 * Hooks ICBM into Create: stress impacts of the shaft driven machines and Create style item tooltips
 * ("Hold [Shift] for Summary", read from {@code <item description id>.tooltip.*} lang keys) for every item,
 * per explosive for the items that carry one.
 */
public final class CreateReg
{
    /** Items whose stacks each carry an explosive, see {@link ExplosiveTooltipModifier} */
    private static final List<DeferredHolder<Item, ? extends Item>> EXPLOSIVE_CARRIERS = List.of(
        ItemReg.itemBlockExplosive, ItemReg.itemExplosiveMissile, ItemReg.itemBombCart);

    private CreateReg()
    {
    }

    /** Common setup, after registries are filled */
    public static void setup()
    {
        // Stress impact is SU per RPM, read from our config each time Create asks so config edits apply live
        BlockStressValues.IMPACTS.register(BlockReg.blockLaunchBase.get(), () -> ConfigLauncher.LAUNCH_PAD_STRESS);
        BlockStressValues.IMPACTS.register(BlockReg.blockCruiseLauncher.get(), () -> ConfigLauncher.CRUISE_TURRET_STRESS);
        BlockStressValues.IMPACTS.register(BlockReg.blockRadarStation.get(), () -> ConfigRadar.STRESS_IMPACT);

        for (DeferredHolder<Item, ? extends Item> holder : ItemReg.ITEMS.getEntries())
        {
            final Item item = holder.get();
            if (EXPLOSIVE_CARRIERS.contains(holder))
            {
                // One description per explosive instead of one per item
                TooltipModifier.REGISTRY.register(item, new ExplosiveTooltipModifier(item, FontHelper.Palette.STANDARD_CREATE));
                continue;
            }
            final TooltipModifier stats = TooltipModifier.mapNull(KineticStats.create(item));
            TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE).andThen(stats));
        }
    }
}
