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
import com.createicbm.lib.saving.NbtSaveHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * @deprecated replaced with modular flight systems, kept for loading old save data
 */
@Deprecated
public class BallisticFlightLogicOld extends BuildableObject<BallisticFlightLogicOld, IBuilderRegistry<IMissileFlightLogic>> implements IMissileFlightLogic, ISimulatedFlight
{
    //TODO recode to break apart movement into sub-logic
    //  Change silo startup to act as a delayed launch
    //  Have it switch to lock height flight logic next
    //  Then have it switch to the actual ballistic flight logic next
    //  Idea will be to cleanup the code and allow for better control in each version

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "ballistic");

    /**
     * Ticks to animate slowly rising from the launcher
     */
    public static final int PAD_WARM_UP_TIME = 20 * 2; //TODO add config
    public static final double MISSILE_CLIMB_HEIGHT = 2; //TODO add config

    public boolean hasStartedFlight = false;

    /**
     * Height Y to wait before starting arc
     */
    public double lockHeight = 0;
    /**
     * Tick runtime of flight arc
     */
    private int missileFlightTime;
    /**
     * Motion Y acceleration for arc to work
     */
    private float acceleration;

    /**
     * Timer for missile to wait on pad before climbing
     */
    private int padWarmUpTimer = PAD_WARM_UP_TIME;

    /**
     * Distance to slowly climb before starting normal path
     */
    private double climbHeight = MISSILE_CLIMB_HEIGHT;

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

    public BallisticFlightLogicOld(int lockHeight) {
        this();
        this.lockHeight = lockHeight;
    }

    public BallisticFlightLogicOld() {
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
    }

    //TODO wire IMissile to connect to ILauncher so we can get launcher source and let the launcher know when we are clear
    //TODO code launcher to not reload until clear, use the all clear flag from the missile combined with collision checks and dead checks

    protected void calculatePath()
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

        // Over 200 assume we will missile simulate rather than arc. Targets straight above or below have no flat distance and so no arc either
        final FlightMath.ArcSolution arc = (flatDistance < 200)
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
        //Warm up on pad for nice animation
        if (padWarmUpTimer > 0) // Replaced with WarmupFlightLogic
        {
            padWarmUpTimer--;
            idleMissileOnPad(entity, ticksInAir);
        }
        //Slowly climb out of the launcher TODO get climb height from launcher
        else if (climbHeight > 0) // Replaced with MoveDirectionLogic
        {
            doSlowClimb(entity, ticksInAir);
        }
        else if (this.lockHeight > 0) // Replaced with MoveDirectionLogic
        {
            handleLockHeight(entity, ticksInAir);
        }
        //Starts the missile into normal flight
        else if (!hasStartedFlight) {

            hasStartedFlight = true;

            this.startX = entity.getX();
            this.startY = entity.getY();
            this.startZ = entity.getZ();

            calculatePath();

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
                MotionHelper.addMotionY(entity, -(this.acceleration));
            }

            // Cut off x-z motion to prevent missing target
            if(Math.abs(entity.getX() - endX) <= 0.1f && Math.abs(entity.getZ() - endZ) <= 0.1f) {
                MotionHelper.setMotionX(entity, 0);
                MotionHelper.setMotionZ(entity, 0);
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

    protected void handleLockHeight(Entity entity, int ticksInAir)
    {
        MotionHelper.addMotionY(entity, 0.1f);
        MotionHelper.setMotionX(entity, 0);
        MotionHelper.setMotionZ(entity, 0);
        this.lockHeight -= entity.getDeltaMovement().y; //TODO fix to account for slow animation climb
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

    /**
     * Has the missile slow climb up before start full motion
     *
     * @param entity     representing the missile
     * @param ticksInAir the missile has been in the air
     */
    protected void doSlowClimb(Entity entity, int ticksInAir)
    {
        MotionHelper.addMotionY(entity, 0.005f); //TODO add config
        lockHeight -= entity.getDeltaMovement().y;
        climbHeight -= entity.getDeltaMovement().y;
    }

    /**
     * Has the missile wait on the pad while it's engines start and generate a lot of smoke
     *
     * @param entity     representing the missile
     * @param ticksInAir the missile has been in the air
     */
    protected void idleMissileOnPad(Entity entity, int ticksInAir)
    {
        MotionHelper.setPitchAndPrev(entity, 90);
        CreateICBM.proxy.spawnPadSmoke(entity, this, ticksInAir);
    }

    @Override
    public boolean shouldRunEngineEffects(Entity entity) {
        return padWarmUpTimer <= 0;
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

        final BlockPos futurePos = predictPosition(entity, BlockPos::containing, 2);

        if (!entity.level().hasChunksAt(entity.blockPosition(), futurePos))
        {
            return true;
        }

        return entity.level() instanceof ServerLevel serverLevel
            && !MissileTrackerHandler.canEntitiesTickBetween(serverLevel, entity.getX(), entity.getZ(), futurePos.getX(), futurePos.getZ());
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

    private static final NbtSaveHandler<BallisticFlightLogicOld> SAVE_LOGIC = new NbtSaveHandler<BallisticFlightLogicOld>()
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
        /* */.nodeInteger("engine_warm_up", (bl) -> bl.padWarmUpTimer, (bl, data) -> bl.padWarmUpTimer = data)
        /* */.nodeDouble("climb_height", (bl) -> bl.climbHeight, (bl, data) -> bl.climbHeight = data)
        /* */.nodeDouble("lock_height", (bl) -> bl.lockHeight, (bl, i) -> bl.lockHeight = i)
        /* */.nodeInteger("flight", (bl) -> bl.ticksFlight, (bl, i) -> bl.ticksFlight = i)
        .base();

}
