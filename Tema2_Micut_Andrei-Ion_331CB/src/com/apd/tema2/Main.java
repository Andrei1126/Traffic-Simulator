/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2;

import com.apd.tema2.entities.Car;
import com.apd.tema2.entities.Intersection;
import com.apd.tema2.entities.NewLane;
import com.apd.tema2.entities.Pedestrians;
import com.apd.tema2.io.Reader;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class Main {
    public static Pedestrians pedestrians = null;
    public static Intersection intersection;
    public static int carsNo;
    public static int maxLane = -1;
    public static HashMap<Integer, Semaphore> lanes = new HashMap<>();
    public static List<Car> cars = Collections.synchronizedList(new ArrayList<>());
    public static CyclicBarrier cyclicBarrier;
    public static CyclicBarrier waitToEnterRoundaboutBarrier;
    public static CyclicBarrier beforeOutOfRoundabound;
    public static CyclicBarrier outOfRoundabound;
    public static Semaphore letCarsGoThrough;

    public static Integer carsNoWithHighPriority;
    public static Integer carsNoWithLowPriority;
    public static Semaphore priorityIntersectionSemaphore;
    public static Semaphore orderedSemaphore;


    public static Integer EXECUTION_TIME;
    public static Integer maxNoOfPedestrians;


    public static int maxCarsNoInRoundabout;
    public static int maxTimeForCarInRoundabout;
    public static volatile int currentCarsInRoundabout = 0;


    public static void main(String[] args) {
        Reader fileReader = Reader.getInstance(args[0]);
        Set<Thread> cars = fileReader.getCarsFromInput();

        cyclicBarrier = new CyclicBarrier(carsNo);

        for (Thread car : cars) {
            car.start();
        }

        if (pedestrians != null) {
            try {
                Thread p = new Thread(pedestrians);
                p.start();
                p.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Thread car : cars) {
            try {
                car.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
