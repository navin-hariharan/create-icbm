package com.createicbm.api;

import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.IPotentialAction;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.conditions.ICondition;
import com.createicbm.api.actions.listener.IActionListenerHandler;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.caps.IEMPReceiver;
import com.createicbm.api.caps.IExplosive;
import com.createicbm.api.caps.IGPSData;
import com.createicbm.api.caps.IMissileHolder;
import com.createicbm.api.explosion.IBlast;
import com.createicbm.api.explosion.redmatter.IBlastVelocity;
import com.createicbm.api.launcher.IMissileLauncher;
import com.createicbm.api.missiles.ICapabilityMissileStack;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.api.radio.IRadio;
import com.createicbm.api.reg.IExplosiveCustomization;
import com.createicbm.api.reg.IExplosiveRegistry;
import com.createicbm.api.reg.content.IExBlockRegistry;
import com.createicbm.api.reg.content.IExMinecartRegistry;
import com.createicbm.api.reg.content.IExMissileRegistry;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

public final class CreateICBMAPI
{

    //=========================
    //=== Registry ============
    //=========================

    /** Registry for target data save/load in missiles */
    public static IBuilderRegistry<IMissileTarget> MISSILE_TARGET_DATA_REGISTRY;
    /** Registry for flight logic save/load in missiles */
    public static IBuilderRegistry<IMissileFlightLogic> MISSILE_FLIGHT_LOGIC_REGISTRY;

    /** Registry for conditional logic, often used for actions */
    public static IBuilderRegistry<ICondition> CONDITION_REGISTRY;

    /** Registry for actions that can be run */
    public static IBuilderRegistry<IActionData> ACTION_REGISTRY;
    /** Registry for actions with conditionals that may be run */
    public static IBuilderRegistry<IPotentialAction> ACTION_POTENTIAL_REGISTRY;
    /** Registry for status messages produced by machines, items, and entities in the mod */
    public static IBuilderRegistry<IActionStatus> ACTION_STATUS_REGISTRY;
    /** Registry for missile cause save/load in missiles */
    public static IBuilderRegistry<IActionCause> ACTION_CAUSE_REGISTRY;
    /** Event system for actions */
    public static IActionListenerHandler ACTION_LISTENER;

    //=========================
    //=== References ==========
    //=========================
    public static final String ID = "create_icbm";

    //=========================
    //=== Capabilities ========
    //=========================

    private static ResourceLocation cap(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(ID, name);
    }

    /** EMP handling for entities */
    public static final EntityCapability<IEMPReceiver, Void> EMP_ENTITY_CAPABILITY = EntityCapability.createVoid(cap("emp"), IEMPReceiver.class);
    /** EMP handling for block entities */
    public static final BlockCapability<IEMPReceiver, @Nullable Direction> EMP_BLOCK_CAPABILITY = BlockCapability.createSided(cap("emp"), IEMPReceiver.class);
    /** EMP handling for item stacks */
    public static final ItemCapability<IEMPReceiver, Void> EMP_ITEM_CAPABILITY = ItemCapability.createVoid(cap("emp"), IEMPReceiver.class);

    /** Only applies to entities */
    public static final EntityCapability<IMissile, Void> MISSILE_CAPABILITY = EntityCapability.createVoid(cap("missile"), IMissile.class);

    public static final BlockCapability<IMissileHolder, @Nullable Direction> MISSILE_HOLDER_CAPABILITY = BlockCapability.createSided(cap("missile_holder"), IMissileHolder.class);

    public static final BlockCapability<IMissileLauncher, @Nullable Direction> MISSILE_LAUNCHER_CAPABILITY = BlockCapability.createSided(cap("missile_launcher"), IMissileLauncher.class);

    public static final BlockCapability<IRadio, @Nullable Direction> RADIO_CAPABILITY = BlockCapability.createSided(cap("radio"), IRadio.class);
    public static final ItemCapability<IRadio, Void> RADIO_ITEM_CAPABILITY = ItemCapability.createVoid(cap("radio"), IRadio.class);

    public static final ItemCapability<IGPSData, Void> GPS_CAPABILITY = ItemCapability.createVoid(cap("gps"), IGPSData.class);

    //=========================
    //=== Will be removed =====
    //=========================
    /**
     * Main handler for explosives, do not override as this will break the mod
     * @deprecated replaced with {@link #ACTION_REGISTRY}
     */
    @Deprecated
    public static IExplosiveRegistry EXPLOSIVE_REGISTRY;

    @Deprecated
    public static IExMissileRegistry EX_MISSILE_REGISTRY;
    @Deprecated
    public static IExBlockRegistry EX_BLOCK_REGISTRY;
    @Deprecated
    public static IExMinecartRegistry EX_MINECART_REGISTRY;

    /** Registry for explosive customizations
     * @deprecated replaced with {@link com.createicbm.api.actions.data.IActionFieldProvider}
     * */
    @Deprecated
    public static IBuilderRegistry<IExplosiveCustomization> EXPLOSIVE_CUSTOMIZATION_REGISTRY;

    @Deprecated
    public static final ResourceLocation EX_MISSILE = ResourceLocation.fromNamespaceAndPath(ID, "missile");
    @Deprecated
    public static final ResourceLocation EX_BLOCK = ResourceLocation.fromNamespaceAndPath(ID, "block");
    @Deprecated
    public static final ResourceLocation EX_MINECART = ResourceLocation.fromNamespaceAndPath(ID, "minecart");

    @Deprecated
    public static final ItemCapability<IExplosive, Void> EXPLOSIVE_ITEM_CAPABILITY = ItemCapability.createVoid(cap("explosive"), IExplosive.class);
    @Deprecated
    public static final EntityCapability<IExplosive, Void> EXPLOSIVE_ENTITY_CAPABILITY = EntityCapability.createVoid(cap("explosive"), IExplosive.class);
    @Deprecated
    public static final BlockCapability<IExplosive, @Nullable Direction> EXPLOSIVE_BLOCK_CAPABILITY = BlockCapability.createSided(cap("explosive"), IExplosive.class);

    /** Only applies to ItemStack */
    @Deprecated
    public static final ItemCapability<ICapabilityMissileStack, Void> MISSILE_STACK_CAPABILITY = ItemCapability.createVoid(cap("missile_stack"), ICapabilityMissileStack.class);

    @Deprecated
    public static final EntityCapability<IBlastVelocity, Void> BLAST_VELOCITY_CAPABILITY = EntityCapability.createVoid(cap("blast_velocity"), IBlastVelocity.class);

    @Deprecated
    public static final EntityCapability<IBlast, Void> BLAST_CAPABILITY = EntityCapability.createVoid(cap("blast"), IBlast.class);

}
