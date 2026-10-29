package com.createicbm.content.entity.phantom;

import com.createicbm.lib.EntityTicking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Harmless spider illusion, see {@link PhantomMob}
 */
public class PhantomSpider extends Spider implements PhantomMob
{
    private final PhantomLogic phantom = new PhantomLogic(this);

    public PhantomSpider(EntityType<? extends PhantomSpider> type, Level level)
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
        if (phantom.tick())
        {
            super.tick();
        }
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
        return EntityType.SPIDER.getDescription();
    }
}
