package com.createicbm.lib.actions;

import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.IPotentialAction;
import com.createicbm.api.actions.cause.IActionCause;
import com.createicbm.api.actions.conditions.ICondition;
import com.createicbm.api.actions.data.ActionField;
import com.createicbm.api.actions.data.ActionFields;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.reg.events.*;
import com.createicbm.content.actions.ActionProvider;
import com.createicbm.content.actions.emp.ActionDataEmpArea;
import com.createicbm.content.blocks.launcher.screen.BlockScreenCause;
import com.createicbm.content.blocks.launcher.status.LauncherStatus;
import com.createicbm.content.missile.logic.source.cause.CausedByBlock;
import com.createicbm.content.missile.logic.source.cause.EntityCause;
import com.createicbm.content.missile.logic.source.cause.RedstoneCause;
import com.createicbm.lib.actions.listners.ActionListenerHandler;
import com.createicbm.lib.actions.status.ActionResponses;
import com.createicbm.lib.actions.status.MissingFieldStatus;
import com.createicbm.lib.buildable.BuildableObjectRegistry;
import com.createicbm.lib.saving.nodes.SaveNodeBoolean;
import com.createicbm.lib.saving.nodes.SaveNodeFacing;
import com.createicbm.lib.saving.nodes.SaveNodeResourceLocation;
import com.createicbm.lib.saving.nodes.SaveNodeVec3d;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ActionSystem {
    public static void setup()
    {
        CreateICBMAPI.ACTION_LISTENER = new ActionListenerHandler();
        setupActionFields();
        setupConditionalRegistry();
        setupCauseRegistry();
        setupStatusRegistry();
        setupActionRegistry();
        setupActionPotentialRegistry();
        ActionProvider.register();
    }

    private static void setupActionFields() {
        ActionFields.AREA_SIZE = ActionField.getOrCreate("area.size", Float.class, FloatTag::valueOf, FloatTag::getAsFloat);
        ActionFields.HOST_ENTITY = ActionField.getOrCreate("host.entity", Entity.class, null, null);
        ActionFields.HOST_POSITION = ActionField.getOrCreate("host.vec3d", Vec3.class, SaveNodeVec3d::save, SaveNodeVec3d::load);
        ActionFields.HOST_DIRECTION = ActionField.getOrCreate("host.direction", Direction.class, SaveNodeFacing::save, SaveNodeFacing::load);
        ActionFields.IMPACTED = ActionField.getOrCreate("impacted", Boolean.class, SaveNodeBoolean::save, SaveNodeBoolean::load);
        ActionFields.TARGET_POSITION = ActionField.getOrCreate("target.vec3d", Vec3.class, SaveNodeVec3d::save, SaveNodeVec3d::load);
        ActionFields.MOTION_VECTOR = ActionField.getOrCreate("motion.vec3d", Vec3.class, SaveNodeVec3d::save, SaveNodeVec3d::load);
        ActionFields.YAW = ActionField.getOrCreate("yaw", Float.class, FloatTag::valueOf, FloatTag::getAsFloat);
        ActionFields.PITCH = ActionField.getOrCreate("pitch", Float.class, FloatTag::valueOf, FloatTag::getAsFloat);
        ActionFields.ENTITY_REG_NAME = ActionField.getOrCreate("entity.regName", ResourceLocation.class, SaveNodeResourceLocation::save, SaveNodeResourceLocation::load);
        ActionFields.ENTITY_DATA = ActionField.getOrCreate("entity.data", CompoundTag.class, (v) -> v, (t) -> t);
    }

    private static void setupConditionalRegistry() {
        CreateICBMAPI.CONDITION_REGISTRY =  new BuildableObjectRegistry<ICondition>("CONDITIONALS", "conditionals");

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ConditionalRegistryEvent(CreateICBMAPI.CONDITION_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)CreateICBMAPI.CONDITION_REGISTRY).lock();
    }

    private static void setupStatusRegistry() {
        CreateICBMAPI.ACTION_STATUS_REGISTRY =  new BuildableObjectRegistry<IActionStatus>("ACTION_STATUS", "action.status");

        // Register defaults
        LauncherStatus.registerTypes();
        ActionResponses.registerTypes();
        CreateICBMAPI.ACTION_STATUS_REGISTRY.register(MissingFieldStatus.REG_NAME, MissingFieldStatus::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionStatusRegistryEvent(CreateICBMAPI.ACTION_STATUS_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)CreateICBMAPI.ACTION_STATUS_REGISTRY).lock();
    }

    private static void setupActionRegistry() {
        CreateICBMAPI.ACTION_REGISTRY =  new BuildableObjectRegistry<>("ACTION_DATA", "action.data");

        // Register defaults
        new ActionDataEmpArea().register();

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionRegistryEvent(CreateICBMAPI.ACTION_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)CreateICBMAPI.ACTION_REGISTRY).lock();
    }

    private static void setupCauseRegistry()
    {
        CreateICBMAPI.ACTION_CAUSE_REGISTRY =  new BuildableObjectRegistry<IActionCause>("ACTION_CAUSE", "action.cause");

        // Register defaults
        CreateICBMAPI.ACTION_CAUSE_REGISTRY.register(EntityCause.REG_NAME, EntityCause::new);
        CreateICBMAPI.ACTION_CAUSE_REGISTRY.register(CausedByBlock.REG_NAME, CausedByBlock::new);
        CreateICBMAPI.ACTION_CAUSE_REGISTRY.register(BlockScreenCause.REG_NAME, BlockScreenCause::new);
        CreateICBMAPI.ACTION_CAUSE_REGISTRY.register(RedstoneCause.REG_NAME, RedstoneCause::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionCauseRegistryEvent(CreateICBMAPI.ACTION_CAUSE_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)CreateICBMAPI.ACTION_CAUSE_REGISTRY).lock();
    }

    private static void setupActionPotentialRegistry()
    {
        CreateICBMAPI.ACTION_POTENTIAL_REGISTRY =  new BuildableObjectRegistry<IPotentialAction>("ACTION_POTENTIAL", "action.potential");

        // Register defaults
        CreateICBMAPI.ACTION_POTENTIAL_REGISTRY.register(PotentialAction.REG_NAME, PotentialAction::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionPotentialRegistryEvent(CreateICBMAPI.ACTION_POTENTIAL_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)CreateICBMAPI.ACTION_POTENTIAL_REGISTRY).lock();
    }
}
