package com.createicbm.mixin.compat.storagedrawers;

import com.createicbm.mods.sable.SableProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Storage Drawers draws the items and counts on a drawer's front only when the player is close to the drawer and in
 * front of it, measured from the drawer's block position. A drawer built into a Create Aeronautics ship is stored in
 * the ship's plot, millions of blocks from the player, so its front showed empty while the ship was assembled. The
 * player's position is moved onto the ship for both checks. Drawers on land are not changed.
 * <p>
 * Only applied when Storage Drawers and Sable are installed, see {@link com.createicbm.mods.mixin.CompatMixinPlugin}.
 * Every injection is optional, a changed Storage Drawers only loses this fix.
 */
@Pseudo
@Mixin(targets = "com.jaquadro.minecraft.storagedrawers.client.renderer.BlockEntityDrawersRenderer")
public abstract class DrawersRendererMixin
{
    @Redirect(method = "render", require = 0,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(Lnet/minecraft/core/Position;)D"))
    private double create_icbm$distanceOnShip(BlockPos drawer, Position player)
    {
        if (player instanceof Vec3 position && SableProxy.INSTANCE.isOnShip(Minecraft.getInstance().level, drawer))
        {
            return drawer.distToCenterSqr(SableProxy.INSTANCE.toShipOf(Minecraft.getInstance().level, drawer, position));
        }
        return drawer.distToCenterSqr(player);
    }

    @Inject(method = "playerBehindBlock", require = 0, cancellable = true, at = @At("HEAD"))
    private void create_icbm$behindOnShip(BlockPos drawer, Direction facing, CallbackInfoReturnable<Boolean> cir)
    {
        final Player player = Minecraft.getInstance().player;
        if (player == null || !SableProxy.INSTANCE.isOnShip(player.level(), drawer))
        {
            return;
        }
        // Same test as Storage Drawers, with the player seen from the ship
        final BlockPos at = BlockPos.containing(SableProxy.INSTANCE.toShipOf(player.level(), drawer, player.position()));
        cir.setReturnValue(switch (facing)
        {
            case NORTH -> at.getZ() > drawer.getZ();
            case SOUTH -> at.getZ() < drawer.getZ();
            case WEST -> at.getX() > drawer.getX();
            case EAST -> at.getX() < drawer.getX();
            default -> false;
        });
    }
}
