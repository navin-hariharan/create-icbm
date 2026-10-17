package com.createicbm.content.actions.emp;

import com.createicbm.api.CreateICBMHelpers;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.actions.IActionData;
import com.createicbm.api.actions.cause.IActionSource;
import com.createicbm.api.actions.data.ActionField;
import com.createicbm.api.actions.data.ActionFields;
import com.createicbm.api.actions.status.IActionStatus;
import com.createicbm.api.caps.IEMPReceiver;
import com.createicbm.api.events.EmpEvent;
import com.createicbm.client.ICBMSounds;
import com.createicbm.config.blast.ConfigBlast;
import com.createicbm.config.util.BlockReplacementData;
import com.createicbm.content.radioactive.RadioactiveHandler;
import com.createicbm.lib.actions.ActionBase;
import com.createicbm.lib.actions.status.ActionResponses;
import com.createicbm.lib.capability.emp.CapabilityEMP;
import com.createicbm.lib.capability.emp.CapabilityEmpInventory;
import com.createicbm.lib.energy.system.EnergySystem;
import com.createicbm.lib.energy.system.IEnergySystem;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ActionEmpArea extends ActionBase {

    static final List<ActionField> SUPPORTED_FIELDS = new ArrayList<>(Collections.singleton(ActionFields.AREA_SIZE));

    @Accessors(chain = true)
    private int size = 1;

    public ActionEmpArea(Level world, Vec3 vec3d, IActionSource source, IActionData actionData) {
        super(world, vec3d, source, actionData);
    }

    @Override
    public <VALUE, TAG extends Tag> VALUE getValue(ActionField<VALUE, TAG> key) {
        if (key == ActionFields.AREA_SIZE) {
            return (VALUE) ActionFields.AREA_SIZE.cast((float) size);
        }
        return null;
    }

    @Override
    public List<ActionField> getFields() {
        return SUPPORTED_FIELDS;
    }

    @Override
    public <VALUE, TAG extends Tag> boolean hasField(ActionField<VALUE, TAG> key) {
        return key == ActionFields.AREA_SIZE;
    }

    @Nonnull
    @Override
    public IActionStatus doAction() {
        if (!getWorld().isClientSide()) {
            this.empTiles();
            this.empEntities();

            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 1, 3);
            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 3, 3);
            //TODO VEProviderShockWave.spawnEffect(world(), position.x(), position.y(), position.z(), 0, 0, 0, 0, 0, 255, 5, 3);
            ICBMSounds.EMP.play(getWorld(), getPosition().x, getPosition().y, getPosition().z, 4.0F, (1.0F + (getWorld().random.nextFloat() - getWorld().random.nextFloat()) * 0.2F) * 0.7F, true);
        }
        return ActionResponses.COMPLETED;
    }

    protected void empTiles() {
        if (!ConfigBlast.emp.ALLOW_TILES) {
            return;
        }
        final Level world = getWorld();
        final BlockPos center = this.getBlockPos();

        // PERF: the cube is 2 * size wide (a million blocks at the default scale) with two events per block. Air has
        // nothing to EMP so it is skipped, whole empty chunk sections at once, unless a replacement is configured for
        // air. Loaded chunks are looked up once per column instead of once per block.
        final boolean skipAir = !hasAirReplacement();
        final int minY = Math.max(center.getY() - this.size, world.getMinBuildHeight());
        final int maxY = Math.min(center.getY() + this.size - 1, world.getMaxBuildHeight() - 1);

        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        //Loop through cube to effect blocks TODO replace with ray trace system
        for (int x = -this.size; x < this.size; x++) {
            for (int z = -this.size; z < this.size; z++) {
                final int blockX = center.getX() + x;
                final int blockZ = center.getZ() + z;

                //Apply action on block if loaded
                final LevelChunk chunk = world.getChunkSource().getChunkNow(blockX >> 4, blockZ >> 4);
                if (chunk == null) {
                    continue;
                }

                for (int blockY = minY; blockY <= maxY; blockY++) {
                    if (skipAir && chunk.getSection(chunk.getSectionIndex(blockY)).hasOnlyAir()) {
                        blockY |= 15; // jump to the top of this section
                        continue;
                    }
                    currentPos.set(blockX, blockY, blockZ);
                    if (!skipAir || !chunk.getBlockState(currentPos).isAir()) {
                        this.empTile(currentPos);
                    }
                }
            }
        }
    }

    /** True if the EMP replacement list has an entry for air, then air blocks can not be skipped */
    private static boolean hasAirReplacement() {
        return EmpHandler.empBlockSwaps.getValue(Blocks.AIR.defaultBlockState()) != null
            || EmpHandler.empBlockSwaps.getValue(Blocks.CAVE_AIR.defaultBlockState()) != null
            || EmpHandler.empBlockSwaps.getValue(Blocks.VOID_AIR.defaultBlockState()) != null;
    }

    protected void empTile(BlockPos blockPos) {
        //Generate some effects
        if (blockPos.getY() == this.getBlockPos().getY()) {
            getWorld().addParticle(ParticleTypes.LARGE_SMOKE, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 0, 0, 0);
        }

        BlockState iBlockState = getWorld().getBlockState(blockPos);
        float powerEntity = 1f;

        //Fire event to allow canceling action on entity
        if (!NeoForge.EVENT_BUS.post(new EmpEvent.BlockPre(this, getWorld(), blockPos, iBlockState)).isCanceled()) {

            // TODO allow scaling replacement by emp energy key=value@nbt({"energy":@energy(min, max)}) with @energy being a replacement target
            // TODO allow gating replacement by emp energy @if(energy>30, replacement)
            final BlockState blockState = getWorld().getBlockState(blockPos);
            final BlockReplacementData replacement = EmpHandler.empBlockSwaps.getValue(blockState);
            if(replacement != null) {
                replacement.apply(getWorld(), blockPos);
                return;
            }

            // TODO Oct 15, 2024 - run action handler per block
            //                     add mod compatability for common mods using NBT editing and reflection

            final BlockEntity tileEntity = getWorld().getBlockEntity(blockPos);
            if (tileEntity != null) {
                boolean doInventory = true;
                final IEMPReceiver tileReceiver = getWorld().getCapability(CreateICBMAPI.EMP_BLOCK_CAPABILITY, blockPos, null);
                if (tileReceiver != null) {
                    IEMPReceiver receiver = tileReceiver;
                    if (receiver != null) {
                        powerEntity = empEntity(tileEntity, powerEntity, receiver);
                        doInventory = receiver.shouldEmpSubObjects(getWorld(), tileEntity.getBlockPos().getX(), tileEntity.getBlockPos().getY(), tileEntity.getBlockPos().getZ());
                    }
                } else if (ConfigBlast.emp.DRAIN_ENERGY_TILES) {
                    IEnergySystem energySystem = EnergySystem.getSystem(tileEntity, null);
                    if (energySystem.canSetEnergyDirectly(tileEntity, null)) {
                        energySystem.setEnergy(tileEntity, null, 0, false);
                    } else {
                        //TODO Spawn tick based effect to drain as much energy as possible over several ticks
                    }
                }

                if (doInventory && CapabilityEmpInventory.getItemHandler(tileEntity) != null) {
                    powerEntity = empEntity(tileEntity, powerEntity, new CapabilityEmpInventory.TileInv(tileEntity));
                }
            }

        }

        //Fire post event to allow hooking EMP action
        NeoForge.EVENT_BUS.post(new EmpEvent.BlockPost(this, getWorld(), blockPos, iBlockState));
    }

    protected void empEntities() {
        if (!ConfigBlast.emp.ALLOW_ENTITY) {
            return;
        }
        //Calculate bounds
        AABB bounds = new AABB(
            getPos().getX() - this.size, getPos().getY() - this.size, getPos().getZ() - this.size,
            getPos().getX() + this.size, getPos().getY() + this.size, getPos().getZ() + this.size);

        //Get entities in bounds
        List<Entity> entities = getWorld().getEntitiesOfClass(Entity.class, bounds);

        //Loop entities to apply effects
        for (Entity entity : entities) {
            float powerEntity = 1f;
            //Fire event to allow canceling action on entity
            if (!NeoForge.EVENT_BUS.post(new EmpEvent.EntityPre(this, entity)).isCanceled()) {
                boolean doInventory = true;
                final IEMPReceiver entityReceiver = entity.getCapability(CreateICBMAPI.EMP_ENTITY_CAPABILITY);
                if (entityReceiver != null) {
                    IEMPReceiver receiver = entityReceiver;
                    if (receiver != null) {
                        powerEntity = empEntity(entity, powerEntity, receiver);
                        doInventory = receiver.shouldEmpSubObjects(getWorld(), entity.getX(), entity.getY(), entity.getZ());
                    }
                } else if (ConfigBlast.emp.DRAIN_ENERGY_ENTITY) {
                    IEnergySystem energySystem = EnergySystem.getSystem(entity, null);
                    if (energySystem.canSetEnergyDirectly(entity, null)) {
                        energySystem.setEnergy(entity, null, 0, false);
                    } else {
                        //TODO Spawn tick based effect to drain as much energy as possible over several ticks
                    }
                }

                if (doInventory && CapabilityEmpInventory.getItemHandler(entity) != null) {
                    powerEntity = empEntity(entity, powerEntity, new CapabilityEmpInventory.EntityInv(entity));
                }

                //Fire post event to allow hooking EMP action
                NeoForge.EVENT_BUS.post(new EmpEvent.EntityPost(this, entity));
            }
        }
    }

    protected float empEntity(Entity entity, float powerEntity, IEMPReceiver receiver) {
        if (receiver != null) {
            powerEntity = receiver.applyEmpAction(getWorld(), entity.getX(), entity.getY(), entity.getZ(), this, powerEntity, true);
            //TODO spawn effects on entity if items were effected
            //TODO CreateICBM.proxy.spawnShock(this.oldWorld(), this.position, new Pos(entity), 20);
        }
        return powerEntity;
    }

    protected float empEntity(BlockEntity entity, float powerEntity, IEMPReceiver receiver) {
        if (receiver != null) {
            powerEntity = receiver.applyEmpAction(getWorld(), entity.getBlockPos().getX(), entity.getBlockPos().getY(), entity.getBlockPos().getZ(), this, powerEntity, true);
            //TODO spawn effects on entity if items were effected
            //TODO CreateICBM.proxy.spawnShock(this.oldWorld(), this.position, new Pos(entity), 20);
        }
        return powerEntity;
    }
}
