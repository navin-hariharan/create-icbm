package com.createicbm.lib.explosive;

import com.createicbm.CreateICBM;
import com.createicbm.lib.thread.IThreadWork;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public class ThreadWorkBlast implements IThreadWork
{
    public final String name;
    public List<BlockPos> editPositions = new ArrayList();
    public BiFunction<Integer, Consumer<BlockPos>, Boolean> runFunction;
    public Consumer<List<BlockPos>> onComplete;

    public ThreadWorkBlast(String name, BiFunction<Integer, Consumer<BlockPos>, Boolean> runFunction, Consumer<List<BlockPos>> onComplete)
    {
        this.name = name;
        this.runFunction = runFunction;
        this.onComplete = onComplete;
    }

    @Override
    public boolean doRun(int steps)
    {
        return runFunction.apply(steps, (blockPos) -> editPositions.add(blockPos));
    }

    @Override
    public void onStarted()
    {
        CreateICBM.logger().debug("ThreadWorkerBlast({}): started", name);
    }

    @Override
    public void onCompleted()
    {
        CreateICBM.logger().debug("ThreadWorkerBlast({}): completed", name);
        onComplete.accept(editPositions);
    }
}
