/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2.factory;

import com.apd.tema2.Main;
import com.apd.tema2.entities.Car;
import com.apd.tema2.entities.NewLane;
import com.apd.tema2.entities.Pedestrians;
import com.apd.tema2.entities.ReaderHandler;
import com.apd.tema2.entities.helper.MaintenanceHelper;
import com.apd.tema2.entities.helper.NewLanesHelper;
import com.apd.tema2.entities.helper.RailRoadHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

/**
 * Returneaza sub forma unor clase anonime implementari pentru metoda de citire din fisier.
 */
public class ReaderHandlerFactory {

    public static ReaderHandler getHandler(String handlerType) {
        // simple semaphore intersection
        // max random N cars roundabout (s time to exit each of them)
        // roundabout with exactly one car from each lane simultaneously
        // roundabout with exactly X cars from each lane simultaneously
        // roundabout with at most X cars from each lane simultaneously
        // entering a road without any priority
        // crosswalk activated on at least a number of people (s time to finish all of them)
        // road in maintenance - 1 lane 2 ways, X cars at a time
        // road in maintenance - N lanes 2 ways, X cars at a time
        // railroad blockage for T seconds for all the cars
        // unmarked intersection
        // cars racing
        switch (handlerType) {
            case "simple_semaphore":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) {
                        // Exemplu de utilizare:
                        // Main.intersection = IntersectionFactory.getIntersection("simpleIntersection");
                        Main.cars.sort((o1, o2) -> {
                            if (o1.getPriority() == o2.getPriority()) {
                                return o1.getWaitingTime() - o2.getWaitingTime();
                            }
                            return o2.getPriority() - o1.getPriority();
                        });
                    }
                };
            case "simple_n_roundabout":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        Main.maxCarsNoInRoundabout = Integer.parseInt(line[0]);
                        Main.maxTimeForCarInRoundabout = Integer.parseInt(line[1]);

                    }
                };
            case "simple_strict_1_car_roundabout":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        Main.maxLane = Main.cars
                                .stream()
                                .map(Car::getStartDirection)
                                .max(Integer::compareTo)
                                .orElse(-1);
                        for (int i = 0; i <= Main.maxLane; i++) {
                            Main.lanes.put(i, new Semaphore(0));
                        }
                        Main.lanes.put(0, new Semaphore(1));
                    }
                };
            case "simple_strict_x_car_roundabout":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        Main.maxCarsNoInRoundabout = Integer.parseInt(line[2]);
                        Main.maxTimeForCarInRoundabout = Integer.parseInt(line[1]);

                        Main.maxLane = Main.cars
                                .stream()
                                .map(Car::getStartDirection)
                                .max(Integer::compareTo)
                                .orElse(-1);
                        for (int i = 0; i <= Main.maxLane; i++) {
                            Main.lanes.put(i, new Semaphore(0));
                        }
                        Main.lanes.put(0, new Semaphore(1));
                        Main.waitToEnterRoundaboutBarrier = new CyclicBarrier(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        Main.beforeOutOfRoundabound = new CyclicBarrier(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        Main.outOfRoundabound = new CyclicBarrier(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        Main.letCarsGoThrough = new Semaphore(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));

                    }
                };
            case "simple_max_x_car_roundabout":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        Main.maxCarsNoInRoundabout = Integer.parseInt(line[2]);
                        Main.maxTimeForCarInRoundabout = Integer.parseInt(line[1]);

                        Main.maxLane = Main.cars
                                .stream()
                                .map(Car::getStartDirection)
                                .max(Integer::compareTo)
                                .orElse(-1);
                        for (int i = 0; i <= Main.maxLane; i++) {
                            Main.lanes.put(i, new Semaphore(Main.maxCarsNoInRoundabout));
                        }
                        Main.waitToEnterRoundaboutBarrier = new CyclicBarrier(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        Main.beforeOutOfRoundabound = new CyclicBarrier(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        Main.outOfRoundabound = new CyclicBarrier(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        Main.letCarsGoThrough = new Semaphore(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                    }
                };
            case "priority_intersection":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        Main.carsNoWithHighPriority = Integer.parseInt(line[0]);
                        Main.carsNoWithLowPriority = Integer.parseInt(line[1]);
                        Main.priorityIntersectionSemaphore = new Semaphore(Main.carsNoWithHighPriority);
                        Main.orderedSemaphore = new Semaphore(1, true);
                    }
                };
            case "crosswalk":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        Main.EXECUTION_TIME = Integer.parseInt(line[0]);
                        Main.maxNoOfPedestrians = Integer.parseInt(line[1]);
                        Main.pedestrians = new Pedestrians(Main.EXECUTION_TIME, Main.maxNoOfPedestrians);


                    }
                };
            case "simple_maintenance":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        MaintenanceHelper.carNoMaxForMentenance = Integer.parseInt(line[0]);
                        MaintenanceHelper.mentenanceSemaphores = new HashMap<>();
                        MaintenanceHelper.mentenanceSemaphores.put(0, new Semaphore(MaintenanceHelper.carNoMaxForMentenance));
                        MaintenanceHelper.mentenanceSemaphores.put(1, new Semaphore(0));
                        MaintenanceHelper.carsNoFromFirstLane = Main.cars.stream()
                                .map(car -> car.getStartDirection())
                                .filter(carDirection -> carDirection == 0)
                                .count();
                        MaintenanceHelper.carsNoFromSecondLane = Main.cars.size() - MaintenanceHelper.carsNoFromFirstLane;
                        MaintenanceHelper.maintenanceBarrier = new CyclicBarrier(MaintenanceHelper.carNoMaxForMentenance);
                    }
                };
            case "complex_maintenance":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        String[] line = br.readLine().split(" ");
                        NewLanesHelper.FREE_LANES = Integer.parseInt(line[0]);
                        NewLanesHelper.TOTAL_INITIAL_LANES = Integer.parseInt(line[1]);
                        NewLanesHelper.CARS_IN_ON_GO = Integer.parseInt(line[2]);

                        NewLanesHelper.Lnumbers = NewLanesHelper.TOTAL_INITIAL_LANES / NewLanesHelper.FREE_LANES;
                        NewLanesHelper.laneOrganization = new HashMap<>();
                        NewLanesHelper.newLanes = Collections.synchronizedList(new ArrayList<>());


                        for (int i = 0; i < NewLanesHelper.FREE_LANES; i++) {
                            NewLanesHelper.newLanes.add(new NewLane(i));
                        }

                        int count = 0;
                        int index = 0;
                        for (int i = 0; i < NewLanesHelper.TOTAL_INITIAL_LANES; i++) {
                            if (count == NewLanesHelper.Lnumbers) {
                                count = 0;
                                if (NewLanesHelper.FREE_LANES - index > 1) {
                                    index++;
                                }
                            }
                            NewLanesHelper.newLanes.get(index).getOldLanesDirections().add(i);
                            NewLanesHelper.newLanes.get(index).getOldLanes().put(i, Collections.synchronizedList(new ArrayList<>()));
                            count++;
                        }

                        NewLanesHelper.complexMaintenanceBarrier = new CyclicBarrier(Main.carsNo);
                        NewLanesHelper.complexMaintenanceSemaphores = new HashMap<>();
                        NewLanesHelper.complexMaintenanceEntranceSemaphores = new HashMap<>();
                        for (int i = 0; i < NewLanesHelper.FREE_LANES; i++) {
                            NewLanesHelper.complexMaintenanceEntranceSemaphores.put(NewLanesHelper.newLanes.get(i).getID(), new Semaphore(1));
                        }

                        for (int i = 0; i < NewLanesHelper.TOTAL_INITIAL_LANES; i++) {
                            NewLanesHelper.complexMaintenanceSemaphores.put(i, new Semaphore(1));
                        }
//                    System.out.println(Main.newLanes);
                    }
                };
            case "railroad":
                return new ReaderHandler() {
                    @Override
                    public void handle(final String handlerType, final BufferedReader br) throws IOException {
                        Main.orderedSemaphore = new Semaphore(Main.carsNo, true);
                        RailRoadHelper.railroadBarrier = new CyclicBarrier(Main.carsNo);
                    }
                };
            default:
                return null;
        }
    }

}
