package com.createicbm.content.missile.logic.flight;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Small helpers keep the original code readable.
 */
public final class MotionHelper
{
    private MotionHelper() {}

    public static void setMotionX(Entity entity, double value) {
        final Vec3 m = entity.getDeltaMovement();
        entity.setDeltaMovement(value, m.y, m.z);
    }

    public static void setMotionY(Entity entity, double value) {
        final Vec3 m = entity.getDeltaMovement();
        entity.setDeltaMovement(m.x, value, m.z);
    }

    public static void setMotionZ(Entity entity, double value) {
        final Vec3 m = entity.getDeltaMovement();
        entity.setDeltaMovement(m.x, m.y, value);
    }

    public static void addMotionX(Entity entity, double value) {
        setMotionX(entity, entity.getDeltaMovement().x + value);
    }

    public static void addMotionY(Entity entity, double value) {
        setMotionY(entity, entity.getDeltaMovement().y + value);
    }

    public static void addMotionZ(Entity entity, double value) {
        setMotionZ(entity, entity.getDeltaMovement().z + value);
    }

    public static void setYawAndPrev(Entity entity, float value) {
        entity.setYRot(value);
        entity.yRotO = value;
    }

    public static void setPitchAndPrev(Entity entity, float value) {
        entity.setXRot(value);
        entity.xRotO = value;
    }
}
