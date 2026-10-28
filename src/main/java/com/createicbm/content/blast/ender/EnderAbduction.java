package com.createicbm.content.blast.ender;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.blast.types.ConfigEnder;
import com.createicbm.lib.network.packet.PacketEnderAbduction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The ender missile cutscene: a giant enderman looms over a player pulled into the vortex, stares at them, tears
 * reality open and drags them to a random spot far away where it stands over them for a moment before it vanishes.
 * <p>
 * Timeline (ticks are server ticks, the client follows the packets in {@link PacketEnderAbduction}):
 * <ol>
 *     <li>STARE: a giant enderman appears in front of the player, screams and steps closer. The destination chunk is
 *     picked and loaded in the background meanwhile (a chunk ticket, no server stall).</li>
 *     <li>WARP: the player is lifted into the air while the screen turns into a purple tunnel, lasts at least the
 *     configured time and as long as the destination needs to be ready.</li>
 *     <li>ARRIVED: the player is teleported, a second giant enderman is standing over them. Then it poofs away.</li>
 * </ol>
 * Everything here runs on the server thread. A player in a cutscene is skipped by the blast so nothing pulls them
 * around, and a player that leaves or dies ends the cutscene on the spot.
 */
@EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public final class EnderAbduction
{
    /** Entity tag of the giant endermen, one found in a loaded save is stale and removed */
    public static final String TAG = ICBMConstants.DOMAIN + ":abductor";

    private static final TicketType<Long> TICKET = TicketType.create(ICBMConstants.DOMAIN + ":abduction", Long::compare, 100);
    /** Distance 2 is a ticking chunk with the ring around it loaded, so the destination is safe to stand in */
    private static final int TICKET_DISTANCE = 2;
    private static final int TICKET_RENEW_TICKS = 40;
    /** Longest the warp waits for the destination chunk, then it is loaded the blocking way */
    private static final int MAX_LOAD_WAIT_TICKS = 160;
    private static final int MAX_TARGET_RETRIES = 6;
    private static final double START_DISTANCE = 7.0;
    private static final double CLOSE_DISTANCE = 4.5;
    private static final double ARRIVAL_DISTANCE = 6.5;
    /** Ticks after the teleport until the client is told, the new enderman needs to reach it first */
    private static final int ARRIVE_PACKET_DELAY = 2;

    private enum Phase
    {
        STARE, WARP, ARRIVED
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Set<UUID> ABDUCTORS = new HashSet<>();
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    private EnderAbduction()
    {
    }

    private static final class Session
    {
        final ServerPlayer player;
        final ServerLevel level;
        Phase phase = Phase.STARE;
        int ticks = 0;
        EnderMan abductor;
        /** Direction from the player to the giant, horizontal and normalized */
        double dirX, dirZ;
        double heightOffset;

        int targetX, targetZ;
        ChunkPos targetChunk;
        Vec3 target;
        int retries = 0;
        int loadWait = 0;
        int sinceTicketRenew = 0;
        int arrivePacketIn = -1;
        EnderMan arrival;

        Session(ServerPlayer player, ServerLevel level)
        {
            this.player = player;
            this.level = level;
        }
    }

    // ------------------------------------------------------------------
    // API
    // ------------------------------------------------------------------

    /**
     * @return true when the player is taken by a giant enderman now or already was (or just was, the cooldown), so the
     * caller leaves them alone. False when the player should be handled the old way (feature off, spectator...).
     */
    public static boolean start(ServerPlayer player)
    {
        final ConfigEnder config = ConfigBlast.ender;
        if (!config.abductPlayers || player.isSpectator() || !player.isAlive())
        {
            return false;
        }
        if (isParticipant(player))
        {
            return true;
        }

        final ServerLevel level = player.serverLevel();
        final Session session = new Session(player, level);

        // The giant stands in front of the player, looking at them
        final double yaw = Math.toRadians(player.getYRot());
        session.dirX = -Math.sin(yaw);
        session.dirZ = Math.cos(yaw);
        final double ax = player.getX() + session.dirX * START_DISTANCE;
        final double az = player.getZ() + session.dirZ * START_DISTANCE;
        final double ay = groundNear(level, ax, az, player.getY());
        session.heightOffset = ay - player.getY();
        session.abductor = spawnGiant(level, player, ax, ay, az);
        if (session.abductor == null)
        {
            return false;
        }

        pickTarget(session);
        SESSIONS.put(player.getUUID(), session);

        ICBMSounds.ENDER_SCREAM.play(level, ax, ay + 4, az, 6.0F, 0.9F + level.random.nextFloat() * 0.2F, false);
        player.hurtMarked = true; // stops a running player's momentum on the client too
        PacketEnderAbduction.send(player, PacketEnderAbduction.START, session.abductor.getId());
        return true;
    }

    /**
     * @return true for a player in the cutscene (or just out of it) and the giant endermen, blasts must not move these
     */
    public static boolean isParticipant(Entity entity)
    {
        if (entity instanceof ServerPlayer player)
        {
            if (SESSIONS.containsKey(player.getUUID()))
            {
                return true;
            }
            final Long until = COOLDOWN.get(player.getUUID());
            if (until != null)
            {
                if (player.level().getGameTime() < until)
                {
                    return true;
                }
                COOLDOWN.remove(player.getUUID());
            }
            return false;
        }
        return !ABDUCTORS.isEmpty() && ABDUCTORS.contains(entity.getUUID());
    }

    // ------------------------------------------------------------------
    // Events
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        if (SESSIONS.isEmpty() || !(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }
        final Iterator<Session> it = SESSIONS.values().iterator();
        while (it.hasNext())
        {
            final Session session = it.next();
            if (session.level != level)
            {
                continue;
            }
            boolean keep;
            try
            {
                keep = tick(session);
            }
            catch (Exception e)
            {
                CreateICBM.logger().error("Ender abduction failed, ending it.", e);
                keep = false;
            }
            if (!keep)
            {
                finish(session);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event)
    {
        final Session session = SESSIONS.remove(event.getEntity().getUUID());
        if (session != null)
        {
            finish(session);
        }
        COOLDOWN.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event)
    {
        SESSIONS.clear();
        ABDUCTORS.clear();
        COOLDOWN.clear();
    }

    /**
     * A giant that was saved with a chunk (crash during the cutscene) has nobody to remove it
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event)
    {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof EnderMan man
            && man.getTags().contains(TAG) && !ABDUCTORS.contains(man.getUUID()))
        {
            event.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------
    // Tick
    // ------------------------------------------------------------------

    /**
     * @return false when the cutscene is over
     */
    private static boolean tick(Session s)
    {
        final ServerPlayer player = s.player;
        final ConfigEnder config = ConfigBlast.ender;
        if (!player.isAlive() || player.isRemoved() || player.level() != s.level)
        {
            return false;
        }

        // Keep the destination loaded and look for a place to stand there
        if (s.target == null)
        {
            prepareTarget(s);
        }
        if (s.targetChunk != null && ++s.sinceTicketRenew >= TICKET_RENEW_TICKS)
        {
            s.sinceTicketRenew = 0;
            addTicket(s);
        }

        s.ticks++;
        switch (s.phase)
        {
            case STARE ->
            {
                if (s.abductor == null || s.abductor.isRemoved())
                {
                    return false;
                }
                final float progress = Math.min(1F, s.ticks / (float) config.stareTicks);
                final double eased = progress * progress * (3 - 2 * progress);
                final double distance = Mth.lerp(eased, START_DISTANCE, CLOSE_DISTANCE);
                moveGiant(s, distance);
                giantParticles(s.level, s.abductor, 8);

                if (s.ticks >= config.stareTicks)
                {
                    s.phase = Phase.WARP;
                    s.ticks = 0;
                    ICBMSounds.ENDER_WARP.play(s.level, player.getX(), player.getY(), player.getZ(), 5.0F, 1.0F, false);
                    // Lifts the player slowly, the client controls are locked so nothing else moves them
                    player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20 * 30, 2, false, false, false));
                    player.hurtMarked = true;
                    PacketEnderAbduction.send(player, PacketEnderAbduction.WARP, s.abductor.getId());
                }
            }
            case WARP ->
            {
                if (s.abductor == null || s.abductor.isRemoved())
                {
                    return false;
                }
                // Keeps the giant hanging in front of the player while they rise
                moveGiant(s, CLOSE_DISTANCE);
                giantParticles(s.level, s.abductor, 5);

                if (s.ticks >= config.warpTicks)
                {
                    if (s.target == null)
                    {
                        // Wait for the chunk to load, then give up and load it the blocking way
                        if (++s.loadWait >= MAX_LOAD_WAIT_TICKS)
                        {
                            forceTarget(s);
                        }
                    }
                    if (s.target != null)
                    {
                        arrive(s);
                    }
                }
            }
            case ARRIVED ->
            {
                if (s.arrivePacketIn >= 0 && --s.arrivePacketIn < 0)
                {
                    PacketEnderAbduction.send(player, PacketEnderAbduction.ARRIVE, s.arrival != null ? s.arrival.getId() : -1);
                }
                if (s.arrival != null && !s.arrival.isRemoved())
                {
                    giantParticles(s.level, s.arrival, 4);
                }
                if (s.ticks >= config.arrivalTicks + ARRIVE_PACKET_DELAY)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static void moveGiant(Session s, double distance)
    {
        final EnderMan giant = s.abductor;
        final double x = s.player.getX() + s.dirX * distance;
        final double z = s.player.getZ() + s.dirZ * distance;
        final double y = s.player.getY() + s.heightOffset;
        giant.setPos(x, y, z);
    }

    private static void giantParticles(ServerLevel level, EnderMan giant, int count)
    {
        level.sendParticles(ParticleTypes.PORTAL, giant.getX(), giant.getY() + giant.getBbHeight() * 0.5, giant.getZ(),
            count, 0.9, giant.getBbHeight() * 0.4, 0.9, 0.6);
    }

    // ------------------------------------------------------------------
    // Destination
    // ------------------------------------------------------------------

    private static void pickTarget(Session s)
    {
        final ConfigEnder config = ConfigBlast.ender;
        final int min = config.minDistance;
        final int max = Math.max(min + 1, config.maxDistance);
        final ServerPlayer player = s.player;
        int x = Mth.floor(player.getX()) + min;
        int z = Mth.floor(player.getZ());
        for (int i = 0; i < 8; i++)
        {
            final double angle = s.level.random.nextDouble() * Math.PI * 2;
            final double distance = min + s.level.random.nextDouble() * (max - min);
            final int tx = Mth.floor(player.getX() + Math.cos(angle) * distance);
            final int tz = Mth.floor(player.getZ() + Math.sin(angle) * distance);
            x = tx;
            z = tz;
            if (s.level.getWorldBorder().isWithinBounds(tx + 0.5, tz + 0.5))
            {
                break;
            }
        }
        if (s.targetChunk != null)
        {
            removeTicket(s);
        }
        s.targetX = x;
        s.targetZ = z;
        s.targetChunk = new ChunkPos(x >> 4, z >> 4);
        s.target = null;
        s.sinceTicketRenew = 0;
        addTicket(s);
    }

    private static void addTicket(Session s)
    {
        s.level.getChunkSource().addRegionTicket(TICKET, s.targetChunk, TICKET_DISTANCE, s.targetChunk.toLong());
    }

    private static void removeTicket(Session s)
    {
        s.level.getChunkSource().removeRegionTicket(TICKET, s.targetChunk, TICKET_DISTANCE, s.targetChunk.toLong());
    }

    /**
     * Looks every tick whether the destination chunk finished loading, nothing here waits for it
     */
    private static void prepareTarget(Session s)
    {
        final LevelChunk chunk = s.level.getChunkSource().getChunkNow(s.targetChunk.x, s.targetChunk.z);
        if (chunk == null)
        {
            return;
        }
        final boolean strict = s.retries < MAX_TARGET_RETRIES;
        final Vec3 spot = EnderTargets.findStandingSpot(s.level, chunk, s.targetX, s.targetZ, s.player.getBlockY(), strict);
        if (spot == null)
        {
            // Lava or no floor, try somewhere else
            s.retries++;
            pickTarget(s);
            return;
        }
        s.target = spot;
    }

    /**
     * The chunk did not load in time, loads it right now (a one time hitch) so the player is not stuck in the tunnel
     */
    private static void forceTarget(Session s)
    {
        final LevelChunk chunk = s.level.getChunk(s.targetChunk.x, s.targetChunk.z);
        s.target = EnderTargets.findStandingSpot(s.level, chunk, s.targetX, s.targetZ, s.player.getBlockY(), false);
    }

    private static void arrive(Session s)
    {
        final ServerPlayer player = s.player;
        final ServerLevel level = s.level;
        final Vec3 target = s.target;

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 3.0F, 0.6F);
        poof(level, s.abductor);
        ABDUCTORS.remove(s.abductor.getUUID());
        s.abductor.discard();
        s.abductor = null;

        player.removeEffect(MobEffects.LEVITATION);
        player.resetFallDistance();

        // Lands facing a random way with the giant standing over them in front
        final float yaw = level.random.nextFloat() * 360F;
        final double radians = Math.toRadians(yaw);
        s.dirX = -Math.sin(radians);
        s.dirZ = Math.cos(radians);
        player.connection.teleport(target.x, target.y, target.z, yaw, 0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        level.playSound(null, target.x, target.y, target.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 3.0F, 0.6F);

        final double ax = target.x + s.dirX * ARRIVAL_DISTANCE;
        final double az = target.z + s.dirZ * ARRIVAL_DISTANCE;
        final double ay = level.hasChunkAt(BlockPos.containing(ax, target.y, az)) ? groundNear(level, ax, az, target.y) : target.y;
        s.arrival = spawnGiant(level, player, ax, ay, az);
        if (s.arrival != null)
        {
            ICBMSounds.ENDER_SCREAM.play(level, ax, ay + 4, az, 5.0F, 0.75F, false);
        }

        s.phase = Phase.ARRIVED;
        s.ticks = 0;
        s.arrivePacketIn = ARRIVE_PACKET_DELAY;
    }

    // ------------------------------------------------------------------
    // Cleanup
    // ------------------------------------------------------------------

    private static void finish(Session s)
    {
        for (EnderMan giant : new EnderMan[]{s.abductor, s.arrival})
        {
            if (giant != null)
            {
                if (!giant.isRemoved())
                {
                    poof(s.level, giant);
                    giant.discard();
                }
                ABDUCTORS.remove(giant.getUUID());
            }
        }
        if (s.targetChunk != null)
        {
            removeTicket(s);
        }
        if (s.player.isAlive() && !s.player.isRemoved())
        {
            s.player.removeEffect(MobEffects.LEVITATION);
            s.player.resetFallDistance();
            PacketEnderAbduction.send(s.player, PacketEnderAbduction.END, -1);
        }
        COOLDOWN.put(s.player.getUUID(), s.level.getGameTime() + ConfigBlast.ender.cooldownTicks);
    }

    private static void poof(ServerLevel level, EnderMan giant)
    {
        if (giant != null && !giant.isRemoved())
        {
            level.sendParticles(ParticleTypes.PORTAL, giant.getX(), giant.getY() + giant.getBbHeight() * 0.5, giant.getZ(),
                80, 1.0, giant.getBbHeight() * 0.5, 1.0, 1.2);
            level.playSound(null, giant.getX(), giant.getY(), giant.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 4.0F, 0.5F);
        }
    }

    // ------------------------------------------------------------------
    // Giant
    // ------------------------------------------------------------------

    private static EnderMan spawnGiant(ServerLevel level, ServerPlayer player, double x, double y, double z)
    {
        final EnderMan giant = EntityType.ENDERMAN.create(level);
        if (giant == null)
        {
            return null;
        }
        final AttributeInstance scale = giant.getAttribute(Attributes.SCALE);
        if (scale != null)
        {
            scale.setBaseValue(ConfigBlast.ender.giantScale);
            giant.refreshDimensions();
        }
        final float yaw = (float) (Mth.atan2(player.getZ() - z, player.getX() - x) * Mth.RAD_TO_DEG) - 90F;
        giant.moveTo(x, y, z, yaw, 0F);
        giant.setYHeadRot(yaw);
        giant.setYBodyRot(yaw);
        giant.setNoAi(true);
        giant.setNoGravity(true);
        giant.noPhysics = true;
        giant.setInvulnerable(true);
        giant.setSilent(true);
        giant.setPersistenceRequired();
        giant.addTag(TAG);
        // Arms out in front, and the open mouthed screaming face that goes with having a target
        giant.setCarriedBlock(Blocks.AIR.defaultBlockState());
        giant.setTarget(player);

        ABDUCTORS.add(giant.getUUID());
        level.addFreshEntity(giant);
        return giant;
    }

    /**
     * Height of the ground at the spot when it is close to the given height, otherwise the given height (inside a
     * cave or under a roof the height map is the surface above)
     */
    private static double groundNear(ServerLevel level, double x, double z, double y)
    {
        final int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(x), Mth.floor(z));
        return Math.abs(h - y) <= 6 ? h : y;
    }
}
