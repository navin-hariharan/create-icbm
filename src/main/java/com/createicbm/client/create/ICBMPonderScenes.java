package com.createicbm.client.create;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.createicbm.api.refs.ICBMExplosives;
import com.createicbm.content.blocks.launcher.base.TileLauncherBase;
import com.createicbm.content.reg.ItemReg;
import com.createicbm.lib.explosive.ExplosiveStacks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * Ponder storyboards for Create: ICBM. The machine schematics share the same drive: a creative motor at (2, 1, 4), a
 * shaft and a gearbox under the machine at (2, 2, 2). The arming scene has a deployer at (2, 3, 2) over a depot.
 * <p>
 * Scene text is looked up from the lang file by order: the n-th {@code text(...)} call of a scene is
 * {@code create_icbm.ponder.<scene>.text_n}, so keep en_us.json in step when editing.
 */
public final class ICBMPonderScenes
{
    private ICBMPonderScenes()
    {
    }

    public static void launchPad(SceneBuilder builder, SceneBuildingUtil util)
    {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("launch_pad", "Launching Missiles from a Launch Pad");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        final BlockPos pad = util.grid().at(2, 2, 2);
        final Selection kinetics = util.select().fromTo(2, 1, 2, 2, 1, 4).add(util.select().position(pad));
        final Selection controllerSide = util.select().fromTo(2, 1, 0, 2, 2, 1);
        final BlockPos controller = util.grid().at(2, 2, 0);

        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(pad), Direction.DOWN);
        scene.idle(10);
        scene.world().modifyBlockEntity(pad, TileLauncherBase.class,
            tile -> tile.cachedMissileStack = ExplosiveStacks.create(ItemReg.itemExplosiveMissile.get(), ICBMExplosives.CONDENSED));
        scene.idle(10);

        scene.overlay().showText(60)
            .attachKeyFrame()
            .text("Launch Pads hold a missile ready for launch")
            .pointAt(util.vector().topOf(pad))
            .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 1, 4), Direction.NORTH);
        scene.idle(10);
        scene.world().setKineticSpeed(kinetics, 64);
        scene.effects().rotationSpeedIndicator(util.grid().at(2, 1, 4));
        scene.idle(10);

        scene.overlay().showText(70)
            .attachKeyFrame()
            .colored(PonderPalette.GREEN)
            .text("They take Rotational Force on the side facing away from the missile")
            .pointAt(util.vector().blockSurface(pad, Direction.DOWN))
            .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(80)
            .text("Rotation winds up the launch charge. At 64 RPM a pad is fully wound in about 4 seconds")
            .pointAt(util.vector().blockSurface(pad, Direction.WEST))
            .placeNearTarget();
        scene.idle(90);

        scene.overlay().showText(70)
            .colored(PonderPalette.RED)
            .text("A missile only leaves the pad on a full charge, so a stopped or overstressed network holds it back")
            .pointAt(util.vector().blockSurface(pad, Direction.WEST))
            .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(controllerSide, Direction.SOUTH);
        scene.idle(15);

        scene.overlay().showText(70)
            .attachKeyFrame()
            .text("Link pads to a Launch Controller through Launch Gantries or Connectors")
            .pointAt(util.vector().topOf(util.grid().at(2, 2, 1)))
            .placeNearTarget();
        scene.idle(80);

        final Vec3 controllerFront = util.vector().blockSurface(controller, Direction.NORTH);
        scene.overlay().showControls(controllerFront, Pointing.DOWN, 50).rightClick();
        scene.idle(10);
        scene.overlay().showText(70)
            .text("Set a target in the controller and fire every ready pad in the group")
            .pointAt(controllerFront)
            .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlockEntity(pad, TileLauncherBase.class, tile -> tile.cachedMissileStack = ItemStack.EMPTY);
        scene.effects().indicateSuccess(pad);
        scene.idle(20);
        scene.markAsFinished();
    }

    public static void cruiseTurret(SceneBuilder builder, SceneBuildingUtil util)
    {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("cruise_turret", "Aiming and Firing a Cruise Turret");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        final BlockPos turret = util.grid().at(2, 2, 2);
        final Selection kinetics = util.select().fromTo(2, 1, 2, 2, 1, 4).add(util.select().position(turret));

        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(turret), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .attachKeyFrame()
            .text("Cruise Turrets aim a missile and fire it straight at their target")
            .pointAt(util.vector().topOf(turret))
            .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 1, 4), Direction.NORTH);
        scene.idle(10);
        scene.world().setKineticSpeed(kinetics, 32);
        scene.effects().rotationSpeedIndicator(util.grid().at(2, 1, 4));
        scene.idle(10);

        scene.overlay().showText(70)
            .attachKeyFrame()
            .colored(PonderPalette.GREEN)
            .text("A shaft from below turns the turret and winds up its launch charge")
            .pointAt(util.vector().blockSurface(turret, Direction.DOWN))
            .placeNearTarget();
        scene.idle(80);

        scene.world().setKineticSpeed(kinetics, 128);
        scene.effects().rotationSpeedIndicator(util.grid().at(2, 1, 4));
        scene.overlay().showText(80)
            .text("Faster shafts aim faster: 32 RPM turns at normal speed, 128 RPM four times as fast")
            .pointAt(util.vector().blockSurface(turret, Direction.WEST))
            .placeNearTarget();
        scene.idle(90);

        final Vec3 top = util.vector().topOf(turret);
        scene.overlay().showControls(top, Pointing.DOWN, 50).rightClick().withItem(new ItemStack(ItemReg.itemRadarGun.get()));
        scene.idle(10);
        scene.overlay().showText(70)
            .attachKeyFrame()
            .text("Hand it a target with a Laser Range Finder, or type one into its menu")
            .pointAt(top)
            .placeNearTarget();
        scene.idle(80);

        scene.overlay().showText(70)
            .text("Once it is on target and fully wound, a radio signal or the menu fires the missile")
            .pointAt(top)
            .placeNearTarget();
        scene.idle(80);
        scene.markAsFinished();
    }

    public static void radarStation(SceneBuilder builder, SceneBuildingUtil util)
    {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("radar_station", "Tracking Missiles with a Radar Station");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        final BlockPos radar = util.grid().at(2, 2, 2);
        final Selection kinetics = util.select().fromTo(2, 1, 2, 2, 1, 4).add(util.select().position(radar));
        final Selection lamp = util.select().position(1, 2, 2);

        scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(radar), Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(60)
            .attachKeyFrame()
            .text("Radar Stations track missiles in flight")
            .pointAt(util.vector().topOf(radar))
            .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 1, 4), Direction.NORTH);
        scene.idle(10);
        scene.world().setKineticSpeed(kinetics, 8);
        scene.effects().rotationSpeedIndicator(util.grid().at(2, 1, 4));
        scene.idle(10);

        scene.overlay().showText(70)
            .attachKeyFrame()
            .colored(PonderPalette.RED)
            .text("The dish needs at least 16 RPM from below. Slower than that, it stays dark")
            .pointAt(util.vector().blockSurface(radar, Direction.WEST))
            .placeNearTarget();
        scene.idle(80);

        scene.world().setKineticSpeed(kinetics, 32);
        scene.effects().rotationSpeedIndicator(util.grid().at(2, 1, 4));
        scene.effects().indicateSuccess(radar);
        scene.idle(10);

        scene.overlay().showText(70)
            .colored(PonderPalette.GREEN)
            .text("Once it spins fast enough, it scans the sky within its detection range")
            .pointAt(util.vector().topOf(radar).add(0, 1, 0))
            .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(util.select().fromTo(1, 1, 2, 1, 2, 2), Direction.EAST);
        scene.idle(15);
        final Vec3 radarTop = util.vector().topOf(radar);
        scene.overlay().showControls(radarTop, Pointing.DOWN, 40).rightClick().withItem(new ItemStack(Items.REDSTONE));
        scene.idle(10);
        scene.overlay().showText(70)
            .attachKeyFrame()
            .text("Right-click it with Redstone to switch on its redstone output")
            .pointAt(radarTop)
            .placeNearTarget();
        scene.idle(80);

        scene.world().toggleRedstonePower(lamp);
        scene.effects().indicateRedstone(util.grid().at(1, 2, 2));
        scene.overlay().showText(80)
            .colored(PonderPalette.RED)
            .text("A missile heading into its trigger range now raises a redstone signal, and an alert on its radio channel")
            .pointAt(util.vector().blockSurface(util.grid().at(1, 2, 2), Direction.WEST))
            .placeNearTarget();
        scene.idle(90);
        scene.markAsFinished();
    }

    public static void arming(SceneBuilder builder, SceneBuildingUtil util)
    {
        final CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("arming", "Arming Missiles and Bomb Carts");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        final BlockPos depot = util.grid().at(2, 1, 2);
        final BlockPos deployer = util.grid().at(2, 3, 2);
        final Selection deployerSelection = util.select().position(deployer);
        final Selection kinetics = util.select().fromTo(2, 3, 2, 2, 3, 4);

        scene.world().showSection(util.select().position(depot), Direction.DOWN);
        scene.idle(10);
        scene.world().createItemOnBeltLike(depot, Direction.UP, ExplosiveStacks.create(ItemReg.itemExplosiveMissile.get(), ICBMExplosives.MISSILEMODULE));
        scene.idle(10);

        scene.overlay().showText(60)
            .attachKeyFrame()
            .text("A Missile Module starts out without a warhead")
            .pointAt(util.vector().topOf(depot))
            .placeNearTarget();
        scene.idle(70);

        scene.world().showSection(util.select().fromTo(2, 1, 4, 2, 2, 4), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(2, 3, 3, 2, 3, 4), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(deployerSelection, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(kinetics, 32);
        scene.effects().rotationSpeedIndicator(util.grid().at(2, 3, 4));
        scene.idle(10);

        scene.overlay().showText(60)
            .text("A Deployer two blocks above the Depot fits the warhead")
            .pointAt(util.vector().blockSurface(deployer, Direction.WEST))
            .placeNearTarget();
        scene.idle(70);

        final ItemStack condensed = ExplosiveStacks.create(ItemReg.itemBlockExplosive.get(), ICBMExplosives.CONDENSED);
        scene.overlay().showControls(util.vector().topOf(deployer), Pointing.DOWN, 40).rightClick().withItem(condensed);
        scene.idle(7);
        setHeldItem(scene, deployerSelection, condensed);
        scene.idle(10);

        scene.overlay().showText(70)
            .attachKeyFrame()
            .text("Give it an explosive crate. The type of crate decides what the warhead does")
            .pointAt(util.vector().topOf(deployer))
            .placeNearTarget();
        scene.idle(80);

        deploy(scene, deployer, depot, deployerSelection, ExplosiveStacks.create(ItemReg.itemExplosiveMissile.get(), ICBMExplosives.CONDENSED));
        scene.effects().indicateSuccess(depot);
        scene.overlay().showText(60)
            .colored(PonderPalette.GREEN)
            .text("The missile is armed, ready for a Launch Pad or Cruise Turret")
            .pointAt(util.vector().topOf(depot))
            .placeNearTarget();
        scene.idle(70);

        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.UP, new ItemStack(Items.MINECART));
        setHeldItem(scene, deployerSelection, ExplosiveStacks.create(ItemReg.itemBlockExplosive.get(), ICBMExplosives.INCENDIARY));
        scene.idle(10);

        scene.overlay().showText(60)
            .attachKeyFrame()
            .text("A Minecart becomes a Bomb Cart the same way")
            .pointAt(util.vector().topOf(depot))
            .placeNearTarget();
        scene.idle(30);
        deploy(scene, deployer, depot, deployerSelection, ExplosiveStacks.create(ItemReg.itemBombCart.get(), ICBMExplosives.INCENDIARY));
        scene.effects().indicateSuccess(depot);
        scene.idle(20);

        scene.overlay().showText(70)
            .text("Both can also be made at a Crafting Table by combining the explosive crate with a Missile Module or a Minecart")
            .pointAt(util.vector().topOf(depot))
            .placeNearTarget();
        scene.idle(80);
        scene.markAsFinished();
    }

    /** One deployer stroke onto the depot, swapping the depot item for the result */
    private static void deploy(CreateSceneBuilder scene, BlockPos deployer, BlockPos depot, Selection deployerSelection, ItemStack result)
    {
        scene.world().moveDeployer(deployer, 1, 25);
        scene.idle(26);
        scene.world().removeItemsFromBelt(depot);
        scene.world().createItemOnBeltLike(depot, Direction.UP, result);
        setHeldItem(scene, deployerSelection, ItemStack.EMPTY);
        scene.world().moveDeployer(deployer, -1, 25);
        scene.idle(26);
    }

    private static void setHeldItem(CreateSceneBuilder scene, Selection deployerSelection, ItemStack stack)
    {
        scene.world().modifyBlockEntityNBT(deployerSelection, DeployerBlockEntity.class,
            nbt -> nbt.put("HeldItem", stack.saveOptional(scene.world().getHolderLookupProvider())));
    }
}
