package com.createicbm.content.entity.phantom;

import com.createicbm.lib.EntityTicking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Harmless creeper illusion, see {@link PhantomMob}. It hisses and swells like the real thing, then vanishes just
 * before it would explode.
 */
public class PhantomCreeper extends Creeper implements PhantomMob
{
    /** Share of the creeper's own fuse at which the phantom vanishes, a little before it would go off */
    private static final float POOF_SWELL = 0.85F;
    /** Shortest Fuse tag allowed, anything below would go off before the swell check could catch it */
    private static final short MIN_FUSE = 3;

    private final PhantomLogic phantom = new PhantomLogic(this);

    public PhantomCreeper(EntityType<? extends PhantomCreeper> type, Level level)
    {
        super(type, level);
        EntityTicking.keepTicking(this);
    }

    @Override
    public PhantomLogic phantom()
    {
        return phantom;
    }

    @Override
    protected void registerGoals()
    {
        PhantomLogic.registerGoals(this, goalSelector);
    }

    @Override
    public void tick()
    {
        if (!level().isClientSide() && getSwelling(1F) >= POOF_SWELL)
        {
            phantom.poof();
            return;
        }
        if (phantom.tick())
        {
            super.tick();
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt)
    {
        // The creeper's fuse is private, so a too short one is fixed in the tag before it's read
        if (nbt.contains("Fuse", Tag.TAG_ANY_NUMERIC) && nbt.getShort("Fuse") < MIN_FUSE)
        {
            nbt = nbt.copy();
            nbt.putShort("Fuse", MIN_FUSE);
        }
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        return phantom.hurt(source);
    }

    @Override
    public boolean doHurtTarget(Entity target)
    {
        return false;
    }

    @Override
    protected void doPush(Entity entity)
    {
        if (PhantomLogic.canPush(entity))
        {
            super.doPush(entity);
        }
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning)
    {
        phantom.poof();
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance)
    {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers()
    {
        return true;
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force)
    {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful()
    {
        return false;
    }

    @Override
    protected Component getTypeName()
    {
        return EntityType.CREEPER.getDescription();
    }
}
