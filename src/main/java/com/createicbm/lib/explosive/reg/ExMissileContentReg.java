package com.createicbm.lib.explosive.reg;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.data.EntityInteractionFunction;
import com.createicbm.api.events.MissileEvent;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.reg.content.IExMissileRegistry;
import com.createicbm.content.reg.ItemReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import com.createicbm.lib.explosive.ExplosiveStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExMissileContentReg extends ExplosiveContentRegistry implements IExMissileRegistry
{

    public ExMissileContentReg()
    {
        super(CreateICBMAPI.EX_MISSILE);
    }

    @Override
    public void setLaunchListener(ResourceLocation exName, Consumer<IMissile> eventCallback)
    {

    }

    @Override
    public void setMissileUpdateListener(ResourceLocation exName, Consumer<IMissile> eventCallback)
    {

    }

    @Override
    public void setInteractionListener(ResourceLocation exName, EntityInteractionFunction function)
    {

    }

    @Override
    public void triggerLaunch(IMissile missile)
    {
        NeoForge.EVENT_BUS.post(new MissileEvent.PostLaunch(missile, missile.getMissileEntity()));
    }

    @Override
    public void triggerFlightUpdate(IMissile missile)
    {

    }

    @Override
    public boolean onInteraction(Entity entity, Player player, InteractionHand hand)
    {
        return false;
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        return ExplosiveStacks.create(ItemReg.itemExplosiveMissile.get(), CreateICBMHelpers.getExplosive(regName, false));
    }
}
