package com.createicbm.content.missile.tracker;

import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.lib.NBTConstants;
import com.createicbm.lib.transform.vector.Pos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;

import java.util.UUID;

/**
 * Stores missile simulation Data
 *
 * Created by GHXX on 8/4/2018.
 */

public class MissileTrackerData
{
    public int preLoadChunkTimer;   //Seconds before the missiles spawns in the loaded chunk
    public int spawnRetries;

    public int ticksLeftToTarget;   //Seconds left before the missile reaches the target area (1 Tick = 1 Second)
    public Pos targetPos;           //Target coordinates

    public CompoundTag missileData;  //Additional missile data

    // Only used to show the missile on web maps while it is simulated
    public UUID flightId;           //Flight id of the missile, see CapabilityMissile#getFlightId
    public double startX, startZ;   //Where the simulation started
    public int totalTicks;          //Simulation length in seconds

    //Constructors
    public MissileTrackerData(EntityMissile<?> missile)
    {
        targetPos = new Pos(missile.getMissileCapability().getTargetData().getPosition()); //TODO switch to storing targeting data
        flightId = missile.getMissileCapability().getFlightId();
        startX = missile.getX();
        startZ = missile.getZ();
        missileData = new CompoundTag();
        missile.saveWithoutId(missileData);
        missileData.putString("id", EntityType.getKey(missile.getType()).toString());
        missileData.remove("Pos");
        missileData.remove("UUID");
    }

    public MissileTrackerData(CompoundTag tagCompound)
    {
        readFromNBT(tagCompound);
    }

    //Helper methods for saving and loading
    public void readFromNBT(CompoundTag nbt)
    {
        ticksLeftToTarget = nbt.getInt(NBTConstants.TICKS);
        targetPos = new Pos(nbt.getCompound(NBTConstants.TARGET));

        missileData = nbt.getCompound(NBTConstants.DATA);

        // Saves from before web map support start at the target
        flightId = nbt.hasUUID("flight_id") ? nbt.getUUID("flight_id") : UUID.randomUUID();
        startX = nbt.contains("start_x") ? nbt.getDouble("start_x") : targetPos.x();
        startZ = nbt.contains("start_z") ? nbt.getDouble("start_z") : targetPos.z();
        totalTicks = nbt.contains("total_ticks") ? nbt.getInt("total_ticks") : ticksLeftToTarget;

        // Fix old saves, [< 4.2.0] didn't include id and is using the pre-missile rewrite data
        if(!missileData.contains("id")) {
            missileData.putString("id", "create_icbm:missile");
        }
    }

    public CompoundTag writeToNBT(CompoundTag nbt)
    {
        nbt.putInt(NBTConstants.TICKS, ticksLeftToTarget);
        nbt.put(NBTConstants.TARGET, targetPos.writeNBT(new CompoundTag()));
        nbt.put(NBTConstants.DATA, missileData);
        nbt.putUUID("flight_id", flightId);
        nbt.putDouble("start_x", startX);
        nbt.putDouble("start_z", startZ);
        nbt.putInt("total_ticks", totalTicks);
        return nbt;
    }
}
