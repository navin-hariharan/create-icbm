package com.createicbm.lib.thread;

import com.createicbm.CreateICBM;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 10/8/2018.
 */
public class WorkerThreadManager
{
    public static WorkerThreadManager INSTANCE;

    private List<WorkerThread> threads = new ArrayList();

    public WorkerThreadManager(int thread_count)
    {
        for (int i = 0; i < thread_count; i++)
        {
            threads.add(new WorkerThread(i));
        }
    }

    public void addWork(IThreadWork workerTask)
    {
        WorkerThread thread = threads.get(0);
        int workCount = threads.get(0).getWorkCount();
        for (int i = 1; i < threads.size(); i++)
        {
            WorkerThread t = threads.get(i);
            int w = t.getWorkCount();
            if (w < workCount)
            {
                thread = t;
                workCount = w;
            }
        }
        if (CreateICBM.runningAsDev)
        {
            CreateICBM.logger().info("Adding work task '" + workCount + "' to thread '" + thread + "'");
        }
        thread.addWork(workerTask);
    }

    public void startThreads()
    {
        CreateICBM.logger().info("Starting threads");
        threads.forEach(thread -> thread.start());
    }

    public void killThreads()
    {
        CreateICBM.logger().info("Stopping threads");
        threads.forEach(thread -> thread.stopTasks());
    }
}
