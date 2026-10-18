package com.createicbm.content.entity.phantom;

import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.content.reg.EntityReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * State and behaviour shared by all the phantom mobs
 */
public final class PhantomLogic
{
    /** Phantoms further than this from their victim fade away */
    private static final double LEASH = 48;

    private final Mob mob;
    @Nullable
    private UUID victimId;
    private int age;

    public PhantomLogic(Mob mob)
    {
        this.mob = mob;
    }

    public static <T extends PathfinderMob & PhantomMob> void registerGoals(T mob, GoalSelector goals)
    {
        goals.addGoal(0, new FloatGoal(mob));
        goals.addGoal(1, new PhantomChaseGoal<>(mob));
    }

    public void setVictim(Player player)
    {
        victimId = player.getUUID();
    }

    /**
     * Player this phantom is after, null once they are gone (dead, logged out, in another dimension, spectating or too
     * far). The phantom then fades away instead of going after someone else.
     */
    @Nullable
    public Player victim()
    {
        final Player victim = victimId != null ? mob.level().getPlayerByUUID(victimId) : null;
        return isValidVictim(victim) ? victim : null;
    }

    private boolean isValidVictim(@Nullable Player player)
    {
        return player != null && player.isAlive() && !player.isSpectator() && player.level() == mob.level()
            && player.distanceToSqr(mob) < LEASH * LEASH;
    }

    /**
     * Runs before the mob's own tick on the server
     *
     * @return false once the phantom has faded and the mob's tick should be skipped
     */
    public boolean tick()
    {
        if (mob.level().isClientSide())
        {
            return true;
        }
        if (++age >= ConfigBlast.phantom.lifetime || victim() == null)
        {
            poof();
            return false;
        }
        return true;
    }

    /**
     * Any hit at all breaks the illusion
     */
    public boolean hurt(DamageSource source)
    {
        if (mob.level().isClientSide() || mob.isRemoved() || mob.isInvulnerableTo(source))
        {
            return false;
        }
        poof();
        return true;
    }

    /** Game bus listener, phantoms never break, pick up or trample anything */
    public static void onMobGriefing(EntityMobGriefingEvent event)
    {
        if (event.getEntity() instanceof PhantomMob)
        {
            event.setCanGrief(false);
        }
    }

    /** Phantoms never shove players around, that could knock someone off a ledge */
    public static boolean canPush(Entity entity)
    {
        return !(entity instanceof Player);
    }

    /**
     * Vanishes in a puff of smoke with the poof sound
     */
    public void poof()
    {
        if (mob.isRemoved())
        {
            return;
        }
        if (mob.level() instanceof ServerLevel server)
        {
            final double x = mob.getX();
            final double y = mob.getY() + mob.getBbHeight() / 2;
            final double z = mob.getZ();
            final double width = mob.getBbWidth() / 2;
            final double height = mob.getBbHeight() / 3;
            server.sendParticles(ParticleTypes.POOF, x, y, z, 24, width, height, width, 0.03);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 8, width, height, width, 0.01);
            server.sendParticles(ParticleTypes.WITCH, x, y, z, 12, width, height, width, 0.5);
            server.playSound(null, x, y, z, ICBMSounds.PHANTOM_POOF.getSound(), SoundSource.HOSTILE, 1.0F, 0.9F + server.random.nextFloat() * 0.2F);
        }
        mob.discard();
    }

    /**
     * Spawns phantoms on the ground around the player, out of their sight where there's room
     */
    public static void haunt(ServerLevel level, Player victim, int count)
    {
        for (int i = 0; i < count; i++)
        {
            final Mob mob = createRandom(level);
            if (mob == null)
            {
                continue;
            }
            final Vec3 pos = findSpawn(level, victim, mob.getType());
            if (pos == null)
            {
                continue;
            }
            final double dx = victim.getX() - pos.x;
            final double dz = victim.getZ() - pos.z;
            final float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90F;
            mob.moveTo(pos.x, pos.y, pos.z, yaw, 0);
            mob.setYHeadRot(yaw);
            mob.setYBodyRot(yaw);
            ((PhantomMob) mob).phantom().setVictim(victim);
            level.addFreshEntity(mob);
        }
    }

    @Nullable
    private static Mob createRandom(ServerLevel level)
    {
        final int roll = level.random.nextInt(100);
        final EntityType<? extends Mob> type;
        if (roll < 35)
        {
            type = EntityReg.PHANTOM_ZOMBIE.get();
        }
        else if (roll < 55)
        {
            type = EntityReg.PHANTOM_HUSK.get();
        }
        else if (roll < 80)
        {
            type = EntityReg.PHANTOM_SPIDER.get();
        }
        else
        {
            type = EntityReg.PHANTOM_CREEPER.get();
        }
        return type.create(level);
    }

    @Nullable
    private static Vec3 findSpawn(ServerLevel level, Player victim, EntityType<?> type)
    {
        final Vec3 look = victim.getViewVector(1);
        for (int attempt = 0; attempt < 16; attempt++)
        {
            final double angle = level.random.nextDouble() * Math.PI * 2;
            final double dirX = Math.cos(angle);
            final double dirZ = Math.sin(angle);
            // First tries stay behind and to the sides of the player, so they seem to come from somewhere
            if (attempt < 10 && dirX * look.x + dirZ * look.z > 0.2)
            {
                continue;
            }
            final double distance = 6 + level.random.nextDouble() * 6;
            final int x = Mth.floor(victim.getX() + dirX * distance);
            final int z = Mth.floor(victim.getZ() + dirZ * distance);
            for (int dy = 4; dy >= -6; dy--)
            {
                final BlockPos pos = BlockPos.containing(x, victim.getY() + dy, z);
                final BlockPos below = pos.below();
                if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                    && level.getFluidState(pos).isEmpty()
                    && level.noCollision(type.getSpawnAABB(x + 0.5, pos.getY(), z + 0.5)))
                {
                    return new Vec3(x + 0.5, pos.getY(), z + 0.5);
                }
            }
        }
        return null;
    }
}
