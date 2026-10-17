package com.createicbm.content.actions.emp;

import com.google.common.collect.ImmutableList;
import com.createicbm.ICBMConstants;
import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.data.*;
import com.createicbm.api.data.meta.MetaTag;
import com.createicbm.config.blast.ConfigBlast;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ActionDataEmpArea implements IActionData {
    public static final ImmutableList<MetaTag> ACTION_TAGS = ImmutableList.of(BlockActionTypes.BLOCK_EDIT, EntityActionTypes.ENTITY_EDIT, ActionTypes.DESTRUCTIVE, WorldActionTypes.WORLD_AREA, EntityActionTypes.ENTITY_AREA);
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "emp.area");
    /** @deprecated This is temp until registered */
    public static IActionData INSTANCE = new ActionDataEmpArea();

    @Nonnull
    public Collection<MetaTag> getTypeTags() {
        return ACTION_TAGS;
    }

    @Nonnull
    @Override
    public ActionEmpArea create(Level world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessor) {
        final ActionEmpArea actionEmpArea = new ActionEmpArea(world, new Vec3(x, y, z), source, this);
        if(fieldAccessor != null && fieldAccessor.hasField(ActionFields.AREA_SIZE)) {
            actionEmpArea.setSize((int)Math.floor(fieldAccessor.getValue(ActionFields.AREA_SIZE))); //TODO allow changing x, y, z size
        }
        else {
            actionEmpArea.setSize((int)Math.floor(ConfigBlast.emp.scale));
        }
        return actionEmpArea;
    }

    @Override
    public List<ActionField> getSupportedFields() {
        return ActionEmpArea.SUPPORTED_FIELDS;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }
}
