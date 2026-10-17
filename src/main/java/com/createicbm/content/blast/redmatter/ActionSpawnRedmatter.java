package com.createicbm.content.blast.redmatter;

import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.data.ActionField;
import com.createicbm.api.actions.data.ActionFields;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.lib.actions.ActionBase;
import com.createicbm.lib.actions.status.ActionResponses;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Action to spawn a redmatter entity
 */
public class ActionSpawnRedmatter extends ActionBase
{
    private Float size = null;

    public ActionSpawnRedmatter(Level world, Vec3 position, IActionSource source, IActionData actionData) {
        super(world, position, source, actionData);
    }

    @Override
    public <VALUE, TAG extends Tag> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
        if(key == ActionFields.AREA_SIZE) {
            size = ActionFields.AREA_SIZE.cast(value);
        }
    }

    @Nonnull
    @Override
    public IActionStatus doAction() {
        //Build entity
        final EntityRedmatter entityRedmatter = new EntityRedmatter(getWorld());
        entityRedmatter.setPos(getPosition().x, getPosition().y, getPosition().z);
        entityRedmatter.setBlastSize(ConfigBlast.redmatter.DEFAULT_SIZE);
        entityRedmatter.setBlastMaxSize(ConfigBlast.redmatter.MAX_SIZE);

        if(size != null) {
            entityRedmatter.setBlastSize(size);
        }

        //Attempt to spawn
        if (getWorld().addFreshEntity(entityRedmatter))
        {
            return ActionResponses.COMPLETED;
        }
        return ActionResponses.ENTITY_SPAWN_FAILED;
    }
}
