package com.createicbm.lib.thread;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public interface IThreadWork
{
    /**
     * Called to run the work
     *
     * @param steps - number of steps to run
     * @return true to keep running, false to end work task
     */
    boolean doRun(int steps);

    /**
     * Called when work is started
     */
    void onStarted();

    /**
     * Called when the work is completed
     */
    void onCompleted();

}
