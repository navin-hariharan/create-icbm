package com.createicbm.content.items;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.createicbm.api.reg.IExplosiveData;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Create style tooltip ("Hold [Shift] for Summary") for the items that carry an explosive: crate, missile and bomb
 * cart. Create's own {@link ItemDescription.Modifier} reads one description per item, so every variant would
 * show the same text. This one builds it per stack:
 * <ul>
 *     <li>summary and any extra conditions or controls from {@code explosive.<explosive id>.effect.*}, what the
 *     explosive does</li>
 *     <li>then the conditions and controls of the carrier from {@code <item description id>.tooltip.*}, how it goes off</li>
 * </ul>
 * A stack with a full description of its own ({@code <stack description id>.tooltip.summary}, the missile module)
 * uses that instead. An explosive without effect text (from an addon) falls back to the carrier's summary.
 */
public class ExplosiveTooltipModifier implements TooltipModifier
{
    private final String carrierKey;
    private final FontHelper.Palette palette;

    /** Built descriptions by stack description id, cleared when the language changes */
    private final Map<String, Optional<ItemDescription>> cache = new HashMap<>();
    private String cachedLanguage;

    public ExplosiveTooltipModifier(Item carrier, FontHelper.Palette palette)
    {
        this.carrierKey = carrier.getDescriptionId() + ".tooltip";
        this.palette = palette;
    }

    @Override
    public void modify(ItemTooltipEvent context)
    {
        final String language = Minecraft.getInstance().getLanguageManager().getSelected();
        if (!language.equals(cachedLanguage))
        {
            cachedLanguage = language;
            cache.clear();
        }

        final ItemStack stack = context.getItemStack();
        final IExplosiveData data = ExplosiveItemHelper.getExplosiveData(stack);
        final ItemDescription description = cache
            .computeIfAbsent(stack.getDescriptionId(), key -> Optional.ofNullable(create(key, data)))
            .orElse(null);
        if (description != null)
        {
            context.getToolTip().addAll(1, description.getCurrentLines());
        }
    }

    @Nullable
    private ItemDescription create(String stackKey, @Nullable IExplosiveData data)
    {
        if (ItemDescription.canFillBuilder(stackKey + ".tooltip.summary"))
        {
            return ItemDescription.create(stackKey + ".tooltip", palette);
        }

        final ItemDescription.Builder builder = new ItemDescription.Builder(palette);
        final String effectKey = data != null ? "explosive." + data.getRegistryKey() + ".effect" : null;
        if (effectKey != null && I18n.exists(effectKey + ".summary"))
        {
            ItemDescription.fillBuilder(builder, effectKey);
        }
        else if (I18n.exists(carrierKey + ".summary"))
        {
            builder.addSummary(I18n.get(carrierKey + ".summary"));
        }
        else
        {
            return null;
        }

        // How the carrier sets it off, after anything specific to the explosive
        for (int i = 1; I18n.exists(carrierKey + ".condition" + i); i++)
        {
            builder.addBehaviour(I18n.get(carrierKey + ".condition" + i), I18n.get(carrierKey + ".behaviour" + i));
        }
        for (int i = 1; I18n.exists(carrierKey + ".control" + i); i++)
        {
            builder.addAction(I18n.get(carrierKey + ".control" + i), I18n.get(carrierKey + ".action" + i));
        }
        return builder.build();
    }
}
