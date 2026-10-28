package com.createicbm.content.items;

import com.createicbm.api.events.RemoteTriggerEvent;
import com.createicbm.content.blocks.explosive.TileEntityExplosive;
import com.createicbm.lib.radio.RadioRegistry;
import com.createicbm.lib.radio.messages.TriggerActionMessage;
import com.createicbm.prefab.FakeRadioSender;
import com.createicbm.prefab.item.ItemRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Remotely triggers launchers and wired explosive crates on its radio channel. Right-click an explosive
 * crate to wire it to the channel, or another radio block to copy its channel.
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/26/2016.
 */
public class ItemRemoteDetonator extends ItemRadio
{
    public ItemRemoteDetonator()
    {
        super(new Item.Properties().stacksTo(1).setNoRepair());
        this.setName("remoteDetonator");
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
    {
        // Explosive crates take the detonator's channel, other radio blocks give theirs to the detonator
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TileEntityExplosive explosive)
        {
            if (!context.getLevel().isClientSide())
            {
                String channel = getRadioChannel(stack);
                if (channel == null || channel.trim().isEmpty())
                {
                    channel = RandomStringUtils.random(4, true, true);
                    setRadioChannel(stack, channel);
                }
                explosive.setRadioChannel(channel);

                final Player player = context.getPlayer();
                if (player != null)
                {
                    // Lang entry uses %s for the channel
                    player.displayClientMessage(Component.translatable("chat.explosive.radioLinked", channel), true);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn)
    {
        ItemStack stack = player.getItemInHand(handIn);
        if (!world.isClientSide())
        {
            if(!NeoForge.EVENT_BUS.post(new RemoteTriggerEvent(world, player, stack)).isCanceled()) //event was not canceled
            {
                final String channel = getRadioChannel(stack);
                RadioRegistry.popMessage(world, new FakeRadioSender(player, stack, null), new TriggerActionMessage(channel));
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
    {
        return true;
    }
}
