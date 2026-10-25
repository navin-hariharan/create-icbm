package com.createicbm.content.items;

import com.createicbm.CreateICBM;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.caps.IGPSData;
import com.createicbm.api.events.RadarGunTraceEvent;
import com.createicbm.lib.LanguageUtility;
import com.createicbm.lib.StackNbt;
import com.createicbm.lib.capability.gps.CapabilityGPSDataItem;
import com.createicbm.lib.capability.holder.ICapabilityHolderItem;
import com.createicbm.lib.network.IPacket;
import com.createicbm.lib.network.IPacketIDReceiver;
import com.createicbm.lib.network.packet.PacketPlayerItem;
import com.createicbm.mods.sable.SableProxy;
import com.createicbm.prefab.item.ItemBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robin) on 6/13/2016.
 */
public class ItemRadarGun extends ItemBase implements IPacketIDReceiver, ICapabilityHolderItem
{
    public static final double MAX_RANGE = 200; //TODO config

    public ItemRadarGun()
    {
        super(new Item.Properties().stacksTo(1));
        this.setName("radarGun");
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == CreateICBMAPI.GPS_CAPABILITY)
        {
            return (T) new CapabilityGPSDataItem(stack);
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flagIn)
    {
        // Stored data
        final IGPSData gpsData = CreateICBMHelpers.getGPSData(stack);
        if (gpsData != null && gpsData.getPosition() != null)
        {
            final Vec3 pos = gpsData.getPosition();
            final ResourceKey<Level> worldId = gpsData.getWorldId();

            final String x = String.format("%.1f", pos.x);
            final String y = String.format("%.1f", pos.y);
            final String z = String.format("%.1f", pos.z);

            if (worldId != null)
            {
                final Level world = gpsData.getWorld();
                final String name = world != null && world.getServer() != null ? world.getServer().getWorldData().getLevelName() : "--";
                final String worldName = String.format("(%s)%s", worldId.location(), name);
                final Component output = Component.translatable(getDescriptionId() + ".data.all", x, y, z, worldName);
                outputLines(output, lines);
            }
            else
            {
                final Component output = Component.translatable(getDescriptionId() + ".data.pos", x, y, z);
                outputLines(output, lines);
            }
        }
        else
        {
            outputLines(Component.translatable(getDescriptionId() + ".data.empty"), lines);
        }

        // General information
        final Component output = Component.translatable(getDescriptionId() + ".info", MAX_RANGE);
        outputLines(output, lines);
    }

    private static void outputLines(Component component, List<Component> lines)
    {
        LanguageUtility.splitByLine(component.getString(), LanguageUtility.toolTipLineLength, line -> lines.add(Component.literal(line)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn)
    {
        if (player.isShiftKeyDown()) // also clear the gps coord if the play is shift-rightclicking in the air
        {
            if (!world.isClientSide())
            {
                clear(player, player.getItemInHand(handIn));
            }
            return InteractionResultHolder.success(player.getItemInHand(handIn));
        }

        if (world.isClientSide())
        {
            final HitResult objectMouseOver = SableProxy.INSTANCE.toWorld(world, player.pick(MAX_RANGE, 1, false));
            if (objectMouseOver.getType() != HitResult.Type.MISS && objectMouseOver instanceof BlockHitResult blockHit)
            { // TODO add message saying that the gps target is out of range.
                final BlockEntity tileEntity = world.getBlockEntity(blockHit.getBlockPos());
                if (!(CreateICBMHelpers.isLauncher(tileEntity, null)))
                {
                    sendToServer(player, handIn, objectMouseOver.getLocation());
                }
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(handIn));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        final Player player = context.getPlayer();
        final ItemStack stack = context.getItemInHand();
        if (context.getLevel().isClientSide() || player == null)
        {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown())
        {
            clear(player, stack);
            return InteractionResult.SUCCESS;
        }
        else if (onTrace(context.getClickLocation(), player, stack))
        {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void clear(Player player, ItemStack stack)
    {
        StackNbt.set(stack, null);
        player.sendSystemMessage(Component.translatable("gps.cleared.name"));
        player.inventoryMenu.broadcastChanges();
    }

    public void sendToServer(Player player, InteractionHand hand, Vec3 hit)
    {
        final int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
        CreateICBM.packetHandler.sendToServer(new PacketPlayerItem(slot).addData(hand == InteractionHand.MAIN_HAND).addData(hit));
    }

    @Override
    public boolean read(ByteBuf buf, int id, Player player, IPacket packet)
    {
        final InteractionHand hand = buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        final Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());

        if (!Double.isFinite(pos.x) || !Double.isFinite(pos.y) || !Double.isFinite(pos.z)
            || player.getEyePosition().distanceToSqr(pos) > (MAX_RANGE + 8) * (MAX_RANGE + 8))
        {
            return true;
        }

        if (!player.level().isClientSide())
        {
            // Packet handlers already run on the main thread
            onTrace(pos, player, player.getItemInHand(hand));
        }
        return true;
    }

    public boolean onTrace(final Vec3 posIn, Player player, ItemStack stack)
    {
        if (stack.getItem() == this)
        {
            final RadarGunTraceEvent event = new RadarGunTraceEvent(player.level(), posIn, player);

            if (NeoForge.EVENT_BUS.post(event).isCanceled() || event.pos == null)
            {
                //event was canceled
                return false; // TODO give user feedback
            }

            final IGPSData gpsData = CreateICBMHelpers.getGPSData(stack);
            if (gpsData != null)
            {
                gpsData.setPosition(posIn);
                gpsData.setWorld(player.level());
                player.sendSystemMessage(Component.translatable("gps.pos.set.name"));
            }
            // TODO give user feedback that something broke
        }
        return true;
    }
}
