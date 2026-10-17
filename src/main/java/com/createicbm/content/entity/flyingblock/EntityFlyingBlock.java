package com.createicbm.content.entity.flyingblock;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.config.ConfigFlyingBlocks;
import com.createicbm.content.reg.EntityReg;
import com.createicbm.lib.projectile.EntityProjectile;
import com.createicbm.lib.saving.NbtSaveHandler;
import lombok.AccessLevel;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;

/**
 * Similiar to {@link net.minecraft.world.entity.item.FallingBlockEntity} but specifically as a projectile to be spawned
 * by {@link com.createicbm.content.blast.redmatter.EntityRedmatter} and other sources.
 *
 * Normally created by {@link FlyingBlock} handling logic
 */
public class EntityFlyingBlock extends EntityProjectile<EntityFlyingBlock> implements IEntityWithComplexSpawn
{
    /** Data about the block we are replicating */
    @Setter(value = AccessLevel.PACKAGE)
    private BlockCaptureData blockData;

    public float yawChange = 0;
    public float pitchChange = 0;

    private float gravity = DEFAULT_GRAVITY;

    public EntityFlyingBlock(EntityType<? extends EntityFlyingBlock> type, Level world)
    {
        super(type, world);
        this.tickCount = 0;
        this.blocksBuilding = true;
        this.inGroundKillTime = ICBMConstants.TICKS_SEC;
    }

    public EntityFlyingBlock(Level world)
    {
        this(EntityReg.BLOCK_GRAVITY.get(), world);
    }

    @Override
    public boolean fireImmune()
    {
        return true;
    }

    public float getBlockGravity()
    {
        return gravity;
    }

    public void setGravity(float gravity)
    {
        this.gravity = gravity;
    }

    @Override
    protected double getDefaultGravity()
    {
        return gravity;
    }

    public void restoreGravity()
    {
        gravity = DEFAULT_GRAVITY;
    }

    public BlockCaptureData getBlockData()
    {
        if (this.blockData == null)
        {
            this.blockData = new BlockCaptureData();
        }
        return this.blockData;
    }

    @Nonnull
    @Override
    public Component getName()
    {
        return Component.literal("Flying Block [" + getBlockData().getBlockState() + ", " + hashCode() + "]");
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        super.writeSpawnData(data);
        final CompoundTag blockTag = this.getBlockData().serializeNBT(data.registryAccess());
        blockTag.remove("block_entity");
        blockTag.remove("source_stack");
        data.writeNbt(blockTag);
        data.writeFloat(this.gravity);
        data.writeFloat(yawChange);
        data.writeFloat(pitchChange);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        super.readSpawnData(data);
        final CompoundTag tag = data.readNbt();
        if (tag != null)
        {
            this.getBlockData().deserializeNBT(data.registryAccess(), tag);
        }
        gravity = data.readFloat();
        yawChange = data.readFloat();
        pitchChange = data.readFloat();
    }

    @Override
    public void tick()
    {
        super.tick();

        //Animation
        if (this.yawChange > 0)
        {
            this.setYRot(this.getYRot() + this.yawChange);
            this.yawChange -= 2;
        }

        if (this.pitchChange > 0)
        {
            this.setXRot(this.getXRot() + this.pitchChange);
            this.pitchChange -= 2;
        }
    }

    @Override
    protected boolean shouldExpire() {
        return super.shouldExpire() || this.getY() > 400;
    }

    @Override
    protected void destroy() {
        this.placeBlockIntoWorld(this.blockPosition(), new BlockHitResult(this.position(), Direction.UP, this.blockPosition(), false));
        this.discard();
    }

    @Override
    protected boolean shouldCollideWith(Entity entity) {
        return super.shouldCollideWith(entity) && !(entity instanceof EntityFlyingBlock);
    }

    @Override
    protected void onImpact(@Nonnull HitResult impactLocation) {
        //Grace period to get away from the ground
        if(ticksInAir < 5) return;

        if(!(impactLocation instanceof EntityHitResult)) {
            final BlockPos pos = BlockPos.containing(impactLocation.getLocation());
            final BlockHitResult blockHit = impactLocation instanceof BlockHitResult hit
                ? hit
                : new BlockHitResult(impactLocation.getLocation(), Direction.UP, pos, false);
            this.placeBlockIntoWorld(pos, blockHit);
        }
        else {
            dropSourceStack(getBlockData().getSourceStack().copy());
            this.discard();
        }
    }

    public void placeBlockIntoWorld(BlockPos pos, BlockHitResult hit)
    {
        this.discard();

        if (!this.level().isClientSide())
        {
            if (isServer() && !tryPlacement(pos, hit))
            {
                dropSourceStack(getBlockData().getSourceStack());
            }
        }
    }

    protected boolean tryPlacement(BlockPos pos, BlockHitResult hit) {

        // Attempt to place using item, as it will better handle unique rules
        final ItemStack sourceStack = this.getBlockData().getSourceStack();
        if(!sourceStack.isEmpty()) {

            // Use fake player to handle TE placement or other special placement rules
            final FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) level()); //TODO attempt to get actual player who create the source of this entity
            player.setItemInHand(InteractionHand.MAIN_HAND, sourceStack);

            //TODO pull entityYaw to get placement direction. This way furnace places facing the same way as it renders.
            final BlockHitResult useHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), pos, false);
            final InteractionResult result = sourceStack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, useHit));

            // cleanup fake
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

            if(result.consumesAction()) {
                return true;
            }
        }

        // Backup plan if we can't place via item, this will break in some cases TODO implement a config to disable blocks or change placement rules
        final BlockState blockState = this.getBlockData().getBlockState();
        if (level().getBlockState(pos).canBeReplaced() && blockState.canSurvive(level(), pos)) {

            if (!level().setBlock(pos, blockState, 11)) return false;

            if (this.getBlockData().getTileEntityData() != null)
            {
                final BlockEntity tileentity = this.level().getBlockEntity(pos);

                if (tileentity != null)
                {
                    final CompoundTag currentSave = tileentity.saveWithFullMetadata(level().registryAccess());

                    // Ensure these are the same tile saves, otherwise we can corrupt a block badly
                    if(currentSave.getString("id").equals(this.getBlockData().getTileEntityData().getString("id"))) {
                        currentSave.merge(this.getBlockData().getTileEntityData());
                        currentSave.putInt("x", pos.getX());
                        currentSave.putInt("y", pos.getY());
                        currentSave.putInt("z", pos.getZ());
                        tileentity.loadWithComponents(currentSave, level().registryAccess());
                        tileentity.setChanged();
                    }
                }
            }

            // Play placement audio, RIP headphone users when redmatter spams :D
            SoundType soundtype = blockState.getSoundType(level(), pos, this);
            level().playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

            return true;
        }
        return false;
    }

    protected void dropSourceStack(ItemStack itemStack) {
        // TODO implement this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops")
        if(itemStack != null && !itemStack.isEmpty()) {
            final ItemEntity entityItem = new ItemEntity(level(), getX(), getY(), getZ(), itemStack);
            if(!level().addFreshEntity(entityItem)) {
                CreateICBM.logger().error("EntityFlyingBlock: Failed to drop source stack '{}' at dim[{}] pos[{}]", itemStack, this.level().dimension().location(), this.blockPosition());
            }
        }
    }

    @Override
    protected DamageSource getImpactDamageSource(Entity entityHit, float velocity, HitResult hit) {
        return level().damageSources().fallingBlock(this);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        return Mth.ceil(velocity * ConfigFlyingBlocks.damageScale);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag save)
    {
        super.addAdditionalSaveData(save);
        SAVE_LOGIC.save(this, save);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag save)
    {
       super.readAdditionalSaveData(save);
       SAVE_LOGIC.load(this, save);
    }

    private static final NbtSaveHandler<EntityFlyingBlock> SAVE_LOGIC = new NbtSaveHandler<EntityFlyingBlock>()
        //Stuck in ground data
        .mainRoot()
        .nodeFloat("gravity", EntityFlyingBlock::getBlockGravity, EntityFlyingBlock::setGravity)
        .nodeINBTSerializable("mimic_block", EntityFlyingBlock::getBlockData)
        .base();

    @Override
    public boolean isPushable()
    {
        return true;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.ALL;
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }
}
