package com.createicbm.content.missile.entity.anti.item;

import com.createicbm.ICBMConstants;
import com.createicbm.api.missiles.ICapabilityMissileStack;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.content.missile.entity.anti.EntitySurfaceToAirMissile;
import net.minecraft.world.level.Level;

public class CapabilitySAMStack implements ICapabilityMissileStack
{
    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile.sam";
    }

    @Override
    public IMissile newMissile(Level world)
    {
        return new EntitySurfaceToAirMissile(world).getMissileCapability();
    }
}
