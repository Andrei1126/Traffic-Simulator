/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2.entities;

import com.apd.tema2.utils.Constants;

import static java.lang.Thread.sleep;

/**
 * Clasa utilizata pentru gestionarea oamenilor care se strang la trecerea de pietoni.
 */
public class Pedestrians implements Runnable {
    private int pedestriansNo = 0;
    private int maxPedestriansNo;
    public volatile static boolean pass = false;
    public volatile static boolean finished = false;
    private int executeTime;
    private long startTime;

    public Pedestrians(int executeTime, int maxPedestriansNo) {
        this.startTime = System.currentTimeMillis();
        this.executeTime = executeTime;
        this.maxPedestriansNo = maxPedestriansNo;
    }

    @Override
    public void run() {
        while (System.currentTimeMillis() - startTime < executeTime) {
            try {
                pedestriansNo++;
                sleep(Constants.PEDESTRIAN_COUNTER_TIME);

                if (pedestriansNo == maxPedestriansNo) {
                    pedestriansNo = 0;
                    pass = true;
                    sleep(Constants.PEDESTRIAN_PASSING_TIME);
                    pass = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        finished = true;
    }

    public static boolean isPass() {
        return pass;
    }

    public static boolean isFinished() {
        return finished;
    }
}
