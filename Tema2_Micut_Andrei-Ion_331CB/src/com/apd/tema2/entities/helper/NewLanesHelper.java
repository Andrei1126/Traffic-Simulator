/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2.entities.helper;

import com.apd.tema2.entities.NewLane;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class NewLanesHelper {
    public static int FREE_LANES;
    public static int TOTAL_INITIAL_LANES;
    public static int CARS_IN_ON_GO;
    public static int Lnumbers;
    public static HashMap<Integer, List<Integer>> laneOrganization;
    public static List<NewLane> newLanes;
    public static CyclicBarrier complexMaintenanceBarrier;
    public static HashMap<Integer, Semaphore> complexMaintenanceSemaphores;
    public static HashMap<Integer, Semaphore> complexMaintenanceEntranceSemaphores;

    public static NewLane getCorrespondingLane(int direction) {
        return newLanes
                .stream()
                .filter(newLane -> newLane.getOldLanesDirections().contains(direction))
                .findFirst()
                .orElse(null);
    }
}
