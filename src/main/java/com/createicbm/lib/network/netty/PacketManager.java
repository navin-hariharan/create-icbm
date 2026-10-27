package com.createicbm.lib.network.netty;

import com.createicbm.CreateICBM;
import com.createicbm.api.data.IWorldPosition;
import com.createicbm.lib.network.IPacket;
import com.createicbm.lib.network.TargetPoint;
import com.createicbm.lib.network.lambda.entity.PacketLambdaEntity;
import com.createicbm.lib.network.lambda.tile.PacketLambdaTile;
import com.createicbm.lib.network.packet.PacketEntityPos;
import com.createicbm.lib.network.packet.PacketPlayerItem;
import com.createicbm.lib.network.packet.PacketSpawnAirParticle;
import com.createicbm.lib.network.packet.PacketSpawnBlockExplosion;
import com.createicbm.lib.network.packet.PacketScreenFlash;
import com.createicbm.lib.network.packet.PacketTrackedSound;
import com.createicbm.jlib.data.vector.IPos3D;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Sends and receives ICBM packets. All packets travel inside {@link ICBMPayload}.
 *
 * @author tgame14
 * @since 26/05/14
 */
public class PacketManager
{
    private static final List<Supplier<IPacket<?>>> FACTORIES = new ArrayList<>();
    private static final Map<Class<?>, Integer> IDS = new HashMap<>();
    /** Registered packet classes in id order, part of the protocol version */
    private static final List<String> NAMES = new ArrayList<>();

    /**
     * Bump when the data a packet writes changes. The list of packets is added on its own, so a client and server with
     * a different set or order of packets get NeoForge's clean "incompatible" message instead of silently mixing ids.
     */
    private static final int PROTOCOL_REVISION = 2;

    /** Errors handling packets are logged at most this often (ms), a bad client could otherwise flood the log */
    private static final long ERROR_LOG_INTERVAL = 10_000;
    private static long lastErrorLog = 0;
    private static int suppressedErrors = 0;

    static
    {
        addPacket(PacketLambdaTile.class, PacketLambdaTile::new);
        addPacket(PacketLambdaEntity.class, PacketLambdaEntity::new);

        addPacket(PacketPlayerItem.class, PacketPlayerItem::new);
        addPacket(PacketSpawnAirParticle.class, PacketSpawnAirParticle::new);
        addPacket(PacketSpawnBlockExplosion.class, PacketSpawnBlockExplosion::new);
        addPacket(PacketEntityPos.class, PacketEntityPos::new);
        addPacket(PacketTrackedSound.class, PacketTrackedSound::new);
        addPacket(PacketScreenFlash.class, PacketScreenFlash::new);
    }

    public final String channel;

    public PacketManager(String channel)
    {
        this.channel = channel;
    }

    /**
     * Registers a packet type. Must be called in the same order on both sides, before the network is set up.
     */
    public static <P extends IPacket<?>> void addPacket(Class<P> clazz, Supplier<P> factory)
    {
        IDS.put(clazz, FACTORIES.size());
        FACTORIES.add((Supplier) factory);
        NAMES.add(clazz.getName());
    }

    static int getDiscriminator(IPacket<?> packet)
    {
        final Integer id = IDS.get(packet.getClass());
        if (id == null)
        {
            throw new IllegalArgumentException("Packet " + packet.getClass() + " is not registered");
        }
        return id;
    }

    static IPacket<?> createPacket(int id)
    {
        if (id < 0 || id >= FACTORIES.size())
        {
            throw new IllegalArgumentException("Unknown ICBM packet id " + id);
        }
        return FACTORIES.get(id).get();
    }

    /** Mod bus listener, registers the payload */
    public static void registerPayloads(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar(protocolVersion());
        registrar.playBidirectional(ICBMPayload.TYPE, ICBMPayload.STREAM_CODEC, PacketManager::handle);
    }

    /**
     * @return protocol version both sides must match, see {@link #PROTOCOL_REVISION}
     */
    static String protocolVersion()
    {
        return PROTOCOL_REVISION + "-" + Integer.toHexString(String.join(",", NAMES).hashCode());
    }

    private static void handle(ICBMPayload payload, IPayloadContext context)
    {
        // Payload handlers run on the main thread by default in NeoForge 21
        IPacket<?> packet = payload.packet();
        try
        {
            final boolean toClient = context.flow().isClientbound();

            // In 1.21 memory connections (singleplayer and the LAN host) hand the sender's payload object over without
            // running the codec, so decodeInto never ran and the packets had no data to apply. Run it through the codec
            // here so both sides get a decoded copy.
            if (!payload.decoded())
            {
                packet = copyThroughCodec(payload, toClient, context.player());
            }

            if (toClient)
            {
                if (FMLEnvironment.dist.isClient())
                {
                    ClientHandler.handle(packet, context.player());
                }
            }
            else
            {
                packet.handleServerSide(context.player());
            }
        }
        catch (Exception e)
        {
            logError(packet, context, e);
        }
    }

    private static void logError(IPacket<?> packet, IPayloadContext context, Exception e)
    {
        final long now = System.currentTimeMillis();
        if (now - lastErrorLog < ERROR_LOG_INTERVAL)
        {
            suppressedErrors++;
            return;
        }
        lastErrorLog = now;
        final String from = context.flow().isClientbound() ? "the server" : "player " + context.player().getName().getString();
        CreateICBM.logger().error("Failed to handle packet {} from {} ({} more errors were not logged)", packet, from, suppressedErrors, e);
        suppressedErrors = 0;
    }

    /**
     * Encodes the payload the same way the network would and decodes a fresh packet from the bytes.
     *
     * @param payload  as sent, holding the sender's packet instance
     * @param toClient true if the server sent it to the client
     * @param receiver player on the receiving side
     * @return decoded packet
     */
    private static IPacket<?> copyThroughCodec(ICBMPayload payload, boolean toClient, Player receiver)
    {
        // Each side encodes and decodes against its own registries, data driven holders (enchantments...) differ
        final RegistryAccess decodeAccess = receiver.registryAccess();
        RegistryAccess encodeAccess = null;
        if (toClient)
        {
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null)
            {
                encodeAccess = server.registryAccess();
            }
        }
        else if (FMLEnvironment.dist.isClient())
        {
            encodeAccess = ClientHandler.registryAccess();
        }
        if (encodeAccess == null)
        {
            encodeAccess = decodeAccess;
        }

        final ByteBuf data = Unpooled.buffer();
        try
        {
            // Both wrappers share the reader/writer index of the same buffer
            ICBMPayload.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(data, encodeAccess), payload);
            return ICBMPayload.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(data, decodeAccess)).packet();
        }
        finally
        {
            data.release();
        }
    }

    /** Isolated so {@link Minecraft} is never class loaded on a dedicated server */
    private static final class ClientHandler
    {
        static void handle(IPacket<?> packet, Player player)
        {
            packet.handleClientSide(Minecraft.getInstance(), player);
        }

        static RegistryAccess registryAccess()
        {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null)
            {
                return mc.getConnection().registryAccess();
            }
            return mc.level != null ? mc.level.registryAccess() : null;
        }
    }

    public void init()
    {
        // Registration happens through RegisterPayloadHandlersEvent, kept for API compatibility
    }

    /**
     * @param packet the packet to send to the player
     * @param player the player MP object
     */
    public void sendToPlayer(IPacket packet, ServerPlayer player)
    {
        PacketDistributor.sendToPlayer(player, new ICBMPayload(packet));
    }

    /**
     * @param packet the packet to send to the players in the dimension
     * @param dimension  the dimension to send to.
     */
    public void sendToAllInDimension(IPacket packet, ResourceKey<Level> dimension)
    {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null)
        {
            final ServerLevel level = server.getLevel(dimension);
            if (level != null)
            {
                PacketDistributor.sendToPlayersInDimension(level, new ICBMPayload(packet));
            }
        }
        else
        {
            CreateICBM.logger().error("Packet sent to dim[" + dimension + "] without a server");
        }
    }

    public void sendToAllInDimension(IPacket packet, Level world)
    {
        if (world instanceof ServerLevel serverLevel)
        {
            PacketDistributor.sendToPlayersInDimension(serverLevel, new ICBMPayload(packet));
        }
    }

    /**
     * sends to all clients connected to the server
     *
     * @param packet the packet to send.
     */
    public void sendToAll(IPacket packet)
    {
        PacketDistributor.sendToAllPlayers(new ICBMPayload(packet));
    }

    public void sendToAllAround(IPacket message, TargetPoint point)
    {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null)
        {
            CreateICBM.logger().error("Packet sent to target point: " + point + " without a server");
            return;
        }
        final ServerLevel level = server.getLevel(point.dimension());
        if (level != null)
        {
            PacketDistributor.sendToPlayersNear(level, null, point.x(), point.y(), point.z(), point.range(), new ICBMPayload(message));
        }
    }

    public void sendToAllAround(IPacket message, IWorldPosition point, double range)
    {
        sendToAllAround(message, point.world(), point.x(), point.y(), point.z(), range);
    }

    public void sendToAllAround(IPacket message, Level world, IPos3D point, double range)
    {
        sendToAllAround(message, world, point.x(), point.y(), point.z(), range);
    }

    public void sendToAllAround(IPacket message, BlockEntity tile)
    {
        sendToAllAround(message, tile, 64);
    }

    public void sendToAllAround(IPacket message, BlockEntity tile, double range)
    {
        sendToAllAround(message, tile.getLevel(), tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ(), range);
    }

    public void sendToAllAround(IPacket message, Level world, double x, double y, double z, double range)
    {
        if (world instanceof ServerLevel serverLevel)
        {
            PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, range, new ICBMPayload(message));
        }
    }

    /** Only call from the client */
    public void sendToServer(IPacket packet)
    {
        PacketDistributor.sendToServer(new ICBMPayload(packet));
    }
}
