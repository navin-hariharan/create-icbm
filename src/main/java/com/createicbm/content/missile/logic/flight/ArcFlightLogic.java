package com.createicbm.content.missile.logic.flight;

import com.createicbm.CreateICBM;
import com.createicbm.ICBMConstants;
import com.createicbm.api.CreateICBMAPI;
import com.createicbm.api.missiles.IMissile;
import com.createicbm.api.missiles.parts.IMissileFlightLogic;
import com.createicbm.api.missiles.parts.IMissileTarget;
import com.createicbm.api.reg.obj.IBuilderRegistry;
import com.createicbm.config.missile.ConfigMissile;
import com.createicbm.content.missile.entity.EntityMissile;
import com.createicbm.content.missile.tracker.ISimulatedFlight;
import com.createicbm.content.missile.tracker.MissileTrackerHandler;
import com.createicbm.content.missile.tracker.SimulationHandOver;
import com.createicbm.lib.buildable.BuildableObject;
import com.createicbm.lib.projectile.EntityProjectile;
import com.createicbm.lib.saving.NbtSaveHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Flight path that moves in a ballistic arc from start position to target position
 */
public class ArcFlightLogic extends BuildableObject<ArcFlightLogic, IBuilderRegistry<IMissileFlightLogic>> implements IMissileFlightLogic, ISimulatedFlight
{
    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "path.arc");

    public boolean hasStartedFlight = false;
    /**
     * Tick runtime of flight arc
     */
    private int missileFlightTime;
    /**
     * Motion Y acceleration for arc to work
     */
    private float acceleration;

    /**
     * Difference in distance from target, used as acceleration
     */
    private double deltaPathX, deltaPathY, deltaPathZ;
    private double startX, startY, startZ;
    private double endX, endY, endZ;

    private boolean flightUpAlways = false;

    private int ticksFlight = 0;

    /** Hand over to the missile tracker, not saved so a missile refused before a reload is offered again */
    private final SimulationHandOver<Entity> simulation = MissileTrackerHandler.newHandOver();

    public ArcFlightLogic() {
        super(REG_NAME, CreateICBMAPI.MISSILE_FLIGHT_LOGIC_REGISTRY, SAVE_LOGIC);
    }

    @Override
    public void calculateFlightPath(final Level world, double startX, double startY, double startZ, final IMissileTarget targetData)
    {
        //Record start and end position
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;

        if(targetData != null)
        {
            this.endX = targetData.getX();
            this.endY = targetData.getY();
            this.endZ = targetData.getZ();
        }

        // TODO check if entire path is chunk loaded, if a reasonable chunk count isn't then we should notify the user in the launcher UI and change to a up/down flight path
        //      This likely will require a static function to run the calculation and show in the GUI. However, should be referenced based on the missile type
        //      As future version might include different guidance systems based on the item's stored data. Meaning a single function wouldn't work but could
        //      be referenced in a prediction callback. Something like IMissile#getGuidance().doPrediction()
    }

    //TODO wire IMissile to connect to ILauncher so we can get launcher source and let the launcher know when we are clear
    //TODO code launcher to not reload until clear, use the all clear flag from the missile combined with collision checks and dead checks

    /**
     * @return true if there is an arc from where the entity is to the target, false for a target straight above or below
     */
    private boolean hasArcFrom(Entity entity)
    {
        final double flat = Math.sqrt((endX - entity.getX()) * (endX - entity.getX()) + (endZ - entity.getZ()) * (endZ - entity.getZ()));
        return flat >= FlightMath.MIN_ARC_DISTANCE;
    }

    protected void calculatePath(Entity entity)
    {
        //TODO rebuild to calculate arc up to maxHeight and move at a fixed speed instead of speed of sound+++++
        //TODO once it reaches maxHeight have it fly flat to make for a smoother player riding experience
        //TODO at end of arc if we can't fix the target offset have the missile fly at an angle strait at the target to fix accuracy issues

        // Calculate the distance difference of the missile
        this.deltaPathX = endX - startX;
        this.deltaPathY = endY - startY;
        this.deltaPathZ = endZ - startZ;

        // TODO: Calculate parabola and relative out the targetHeight.
        // Calculate the power required to reach the target co-ordinates
        // Ground Displacement
        final float flatDistance = (float)Math.sqrt(deltaPathX * deltaPathX + deltaPathZ * deltaPathZ);

        // Targets straight above or below have no flat distance and so no arc, they fly up like out of range targets
        final FlightMath.ArcSolution arc = (flatDistance < ConfigMissile.ARC_DISTANCE_LIMIT || entity.getPassengers().stream().anyMatch(e -> e instanceof Player))
            ? FlightMath.solveArc(flatDistance) : null;

        if(arc != null) {
            missileFlightTime = arc.flightTime();
            this.acceleration = arc.acceleration();
        }
        else {
            this.acceleration = 0;
            this.missileFlightTime = 0;
            flightUpAlways = true;
        }
    }

    /**
     * Fly straight up with a slight lean towards the target, the way targets out of arc range are flown. Also used
     * when there is no arc to fly (target straight above or below the launcher, or the arc maths gave a value that is
     * not finite).
     */
    protected void startFlyingUp(Entity entity)
    {
        flightUpAlways = true;
        final double flatDistance = Math.sqrt(deltaPathX * deltaPathX + deltaPathZ * deltaPathZ);
        final float hortSpeed = 0.05f;
        final float vertSpeed = 2f;
        // No lean when the target is right above or below, dividing by the zero distance made the motion NaN
        final boolean lean = flatDistance >= FlightMath.MIN_ARC_DISTANCE;
        MotionHelper.setMotionX(entity, lean ? (this.deltaPathX / flatDistance) * hortSpeed : 0);
        MotionHelper.setMotionZ(entity, lean ? (this.deltaPathZ / flatDistance) * hortSpeed : 0);
        MotionHelper.setMotionY(entity, vertSpeed);
    }

    @Override
    public void onEntityTick(Entity entity, IMissile missile, int ticksInAir)
    {
        // Reset path if player starts riding after we already started
        if(flightUpAlways && !entity.getPassengers().isEmpty() && entity.getPassengers().stream().anyMatch(e -> e instanceof Player) && hasArcFrom(entity)) {
            this.flightUpAlways = false;

            // TODO instead of restarting flight logic... end this instance and replace with a new instance appended to flight plan
            this.startX = entity.getX();
            this.startY = entity.getY();
            this.startZ = entity.getZ();
            ticksFlight = 0;
            this.calculatePath(entity);
            return;
        }

        //Starts the missile into normal flight
        if (!hasStartedFlight) {

            hasStartedFlight = true;

            this.startX = entity.getX();
            this.startY = entity.getY();
            this.startZ = entity.getZ();

            this.calculatePath(entity);

            // TODO split this into two different flight logics. Then have a parent logic object switch which is running
            if(!flightUpAlways) {

                final double motionY = this.acceleration * ((float) missileFlightTime / 2f);
                final double motionX = this.deltaPathX / missileFlightTime;
                final double motionZ = this.deltaPathZ / missileFlightTime;

                if (FlightMath.isFinite(motionX, motionY, motionZ)) {
                    MotionHelper.setMotionY(entity, motionY);
                    MotionHelper.setMotionX(entity, motionX);
                    MotionHelper.setMotionZ(entity, motionZ);
                }
                else {
                    CreateICBM.logger().warn("{}: arc flight gave a motion that is not finite ({}, {}, {}), flying straight up instead", entity, motionX, motionY, motionZ);
                    this.acceleration = 0;
                    startFlyingUp(entity);
                }
            }
            else {
                startFlyingUp(entity);
            }
        }
        //Normal path logic
        else {
            runFlightLogic(entity, ticksInAir);
        }
    }

    protected void runFlightLogic(Entity entity, int ticksInAir)
    {
        ticksFlight++;

        if (!entity.level().isClientSide())
        {

            // Apply gravity
            if(!flightUpAlways) {

                if(entity instanceof EntityProjectile) {
                    ((EntityProjectile<?>) entity).setMotionVector(entity.getDeltaMovement().x, entity.getDeltaMovement().y - this.acceleration, entity.getDeltaMovement().z);
                }
                else {
                    MotionHelper.addMotionY(entity, -(this.acceleration));
                }
            }

            // Cut off x-z motion to prevent missing target
            if(Math.abs(entity.getX() - endX) <= 0.1f && Math.abs(entity.getZ() - endZ) <= 0.1f) {
                if(entity instanceof EntityProjectile) {
                    ((EntityProjectile<?>) entity).setMotionVector(0, entity.getDeltaMovement().y, 0);
                }
                else {
                   MotionHelper.setMotionX(entity, 0);
                   MotionHelper.setMotionZ(entity, 0);
                }
            }

            // Update animate rotations
            alignWithMotion(entity);

            // Sim system
            simulation.tick(entity, e -> MissileTrackerHandler.canSimulate(e) && shouldSimulate(e));
        }
    }

    @Override
    public boolean isSimulationRefused() {
        return simulation.isRefused();
    }

    @Override
    public boolean canSafelyExitLogic() {
        return hasStartedFlight;
    }

    protected void alignWithMotion(Entity entity)
    {
        // A missile that isn't moving has no direction to face, atan of 0/0 used to make the pitch NaN
        final Vec3 motion = entity.getDeltaMovement();
        if (!FlightMath.hasDirection(motion.x, motion.y, motion.z))
        {
            return;
        }
        entity.setXRot((float) FlightMath.pitchDegrees(motion.x, motion.y, motion.z));
        // Look at the next point
        entity.setYRot((float) FlightMath.yawDegrees(motion.x, motion.z));
    }

    @Override
    public boolean shouldRunEngineEffects(Entity entity) {
        return hasStartedFlight;
    }

    protected boolean shouldSimulate(Entity entity)
    {
        if (EntityMissile.hasPlayerRiding(entity))
        {
            return false;
        }
        else if (entity.getY() >= ConfigMissile.SIMULATION_EXIT_HEIGHT)
        {
            return true;
        }

        // Somebody can see it: it keeps flying as an entity and the chunks ahead are loaded for it, so it is seen all
        // the way to the ground instead of vanishing mid-air
        if (MissileTrackerHandler.isWatched(entity))
        {
            return false;
        }

        // TODO predict if the missile only needed a few more chunks to hit... if so chunk load or let get stuck in board to prevent changing arc path

        final BlockPos futurePos = predictPosition(entity, BlockPos::containing, 2);

        if(entity.level() instanceof ServerLevel serverLevel) {
            return !MissileTrackerHandler.canEntitiesTickBetween(serverLevel, entity.getX(), entity.getZ(), futurePos.getX(), futurePos.getZ());
        }
        return false;
    }

    @Override
    public <V> V predictPosition(Entity entity, VecBuilderFunc<V> builder, int ticks)
    {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        double motionY = entity.getDeltaMovement().y;

        while (ticks-- > 0)
        {
            motionY -= this.acceleration;

            x += entity.getDeltaMovement().x;
            y += motionY;
            z += entity.getDeltaMovement().z;
        }

        return builder.apply(x, y, z);
    }

    @Override
    public boolean shouldDecreaseMotion(Entity entity)
    {
        //Disable gravity and friction
        return false;
    }

    private static final NbtSaveHandler<ArcFlightLogic> SAVE_LOGIC = new NbtSaveHandler<ArcFlightLogic>()
        //Stuck in ground data
        .addRoot("flags")
        /* */.nodeBoolean("flight_started", (bl) -> bl.hasStartedFlight, (bl, data) -> bl.hasStartedFlight = data)
        .base()
        .addRoot("inputs")
        /* */.nodeDouble("start_x", (bl) -> bl.startX, (bl, i) -> bl.startX = i)
        /* */.nodeDouble("start_y", (bl) -> bl.startY, (bl, i) -> bl.startY = i)
        /* */.nodeDouble("start_z", (bl) -> bl.startZ, (bl, i) -> bl.startZ = i)
        /* */.nodeDouble("end_x", (bl) -> bl.endX, (bl, i) -> bl.endX = i)
        /* */.nodeDouble("end_y", (bl) -> bl.endY, (bl, i) -> bl.endY = i)
        /* */.nodeDouble("end_z", (bl) -> bl.endZ, (bl, i) -> bl.endZ = i)
        .base()
        .addRoot("calculated")
        /* */.nodeInteger("flight_time", (bl) -> bl.missileFlightTime, (bl, data) -> bl.missileFlightTime = data)
        /* */.nodeFloat("acceleration", (bl) -> bl.acceleration, (bl, data) -> bl.acceleration = data)
        /* */.nodeDouble("delta_x", (bl) -> bl.deltaPathX, (bl, data) -> bl.deltaPathX = data)
        /* */.nodeDouble("delta_y", (bl) -> bl.deltaPathY, (bl, data) -> bl.deltaPathY = data)
        /* */.nodeDouble("delta_z", (bl) -> bl.deltaPathZ, (bl, data) -> bl.deltaPathZ = data)
        .base()
        .addRoot("timers")
        /* */.nodeInteger("flight", (bl) -> bl.ticksFlight, (bl, i) -> bl.ticksFlight = i)
        .base();

}
