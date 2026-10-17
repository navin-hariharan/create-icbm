package com.createicbm.lib.world;

import com.createicbm.CreateICBM;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Portal;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;

/**
 * For internal use or direct mod addons, use events for external interaction
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ProjectileBlockInteraction {

    private static final HashMap<BlockState, IProjectileBlockInteraction> stateToInteraction = new HashMap();
    private static final HashMap<Block, IProjectileBlockInteraction> blockToInteraction = new HashMap();
    private static final HashMap<TagKey<Block>, IProjectileBlockInteraction> materialToInteraction = new HashMap();

    // TODO once on 1.19 use TAG system to easily ID blocks "CollisionBasedPortal"

    public static void addBlockStateInteraction(BlockState state, IProjectileBlockInteraction function) {
        if(stateToInteraction.containsKey(state)) {
            CreateICBM.logger().warn("interaction already exists for " + state + " replacing", new RuntimeException());
        }
        stateToInteraction.put(state, function);
    }

    public static void addBlockInteraction(Block block, IProjectileBlockInteraction function) {
        if(blockToInteraction.containsKey(block)) {
            CreateICBM.logger().warn("interaction already exists for " + block + " replacing", new RuntimeException());
        }
        blockToInteraction.put(block, function);
    }

    public static void addMaterialInteraction(TagKey<Block> material, IProjectileBlockInteraction function) {
        if(materialToInteraction.containsKey(material)) {
            CreateICBM.logger().warn("interaction already exists for " + material + " replacing", new RuntimeException());
        }
        materialToInteraction.put(material, function);
    }

    public static void addCollisionInteraction(Block block) {
        addBlockInteraction(block, (world, pos, hit, side, state, entity) -> IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT);
    }

    public static void breakBlockInteraction(Block block) {
        addBlockInteraction(block, (world, pos, hit, side, state, entity) -> {
            if(breakBlock(world, pos, state, entity)) {
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
        });
    }

    public static void breakMaterialInteraction(TagKey<Block> material) {
        addMaterialInteraction(material, (world, pos, hit, side, state, entity) -> {
            if(breakBlock(world, pos, state, entity)) {
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
        });
    }

    private static boolean breakBlock(Level world, BlockPos pos, BlockState state, Entity entity) {
        final Block block = state.getBlock();
        if (state.canEntityDestroy(world, pos, entity))
        {
            // Projectiles tick on both sides and the client copy can hit a different block than the server, leaving an
            // unsynced ghost hole. Only the server edits the world, the client copy still passes through like the
            // server one does.
            if (!world.isClientSide())
            {
                Block.dropResources(state, world, pos);
                world.removeBlock(pos, false);
            }
            return true;
        }
        return false;
    }

    public static IProjectileBlockInteraction.EnumHitReactions handleSpecialInteraction(Level world, BlockPos pos, Vec3 hit, Direction side, BlockState state, Entity entity) {

        IProjectileBlockInteraction func = stateToInteraction.get(state);
        if(func != null) {
            final IProjectileBlockInteraction.EnumHitReactions result = func.apply(world, pos, hit, side, state, entity);
            if(result != null && result != IProjectileBlockInteraction.EnumHitReactions.PASS) {
                return result;
            }
        }
        func = blockToInteraction.get(state.getBlock());
        if(func != null) {
            final IProjectileBlockInteraction.EnumHitReactions result = func.apply(world, pos, hit, side, state, entity);
            if(result != null && result != IProjectileBlockInteraction.EnumHitReactions.PASS) {
                return result;
            }
        }
        for(java.util.Map.Entry<TagKey<Block>, IProjectileBlockInteraction> entry : materialToInteraction.entrySet()) {
            if(!state.is(entry.getKey())) {
                continue;
            }
            final IProjectileBlockInteraction.EnumHitReactions result = entry.getValue().apply(world, pos, hit, side, state, entity);
            if(result != null && result != IProjectileBlockInteraction.EnumHitReactions.PASS) {
                return result;
            }
        }
        return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
    }

    public static void register() {

        // Portal handling
        addCollisionInteraction(Blocks.NETHER_PORTAL);
        addCollisionInteraction(Blocks.END_PORTAL);
        addBlockInteraction(Blocks.END_GATEWAY, (world, pos, hit, side, state, entity) -> {
            final BlockEntity tile = world.getBlockEntity(pos);
            if(tile instanceof TheEndGatewayBlockEntity gateway && state.getBlock() instanceof Portal portal) {
                if(!gateway.isCoolingDown() && entity.canUsePortal(false)) {
                    if(!world.isClientSide()) {
                        entity.setAsInsidePortal(portal, pos);
                        TheEndGatewayBlockEntity.triggerCooldown(world, pos, state, gateway);
                    }
                    return IProjectileBlockInteraction.EnumHitReactions.TELEPORTED;
                }
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
        });

        breakMaterialInteraction(BlockTags.LEAVES);
        breakBlockInteraction(Blocks.SNOW);
        breakMaterialInteraction(Tags.Blocks.GLASS_BLOCKS);
        breakMaterialInteraction(Tags.Blocks.GLASS_PANES);
        breakBlockInteraction(Blocks.GLOWSTONE);
        breakBlockInteraction(Blocks.SEA_LANTERN);
        breakBlockInteraction(Blocks.BEACON);
    }
}
