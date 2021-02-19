/*
Micut Andrei-Ion
Grupa 331CB
 */


package com.apd.tema2.entities.helper;

import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class MaintenanceHelper {
    public static Integer carNoMaxForMentenance;
    public static HashMap<Integer, Semaphore> mentenanceSemaphores;
    public static long carsNoFromFirstLane;
    public static long carsNoFromSecondLane;
    public static CyclicBarrier maintenanceBarrier;
}
