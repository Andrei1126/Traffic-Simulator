/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2.factory;

import com.apd.tema2.Main;
import com.apd.tema2.entities.*;
import com.apd.tema2.entities.helper.MaintenanceHelper;
import com.apd.tema2.entities.helper.NewLanesHelper;
import com.apd.tema2.entities.helper.RailRoadHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;

/**
 * Clasa Factory ce returneaza implementari ale InterfaceHandler sub forma unor
 * clase anonime.
 */
public class IntersectionHandlerFactory {
    static volatile int leadingThread = -1;

    public static IntersectionHandler getHandler(String handlerType) {
        // simple semaphore intersection
        // max random N cars roundabout (s time to exit each of them)
        // roundabout with exactly one car from each lane simultaneously
        // roundabout with exactly X cars from each lane simultaneously
        // roundabout with at most X cars from each lane simultaneously
        // entering a road without any priority
        // crosswalk activated on at least a number of people (s time to finish all of
        // them)
        // road in maintenance - 2 ways 1 lane each, X cars at a time
        // road in maintenance - 1 way, M out of N lanes are blocked, X cars at a time
        // railroad blockage for s seconds for all the cars
        // unmarked intersection
        // cars racing
        switch (handlerType) {
            case "simple_semaphore":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        System.out.println("Car " + car.getId() + " has reached the semaphore, now waiting...");
                        try {
                            sleep(car.getWaitingTime());
                        } catch (InterruptedException e) {        //Dezactivat sleep doar pt usurarea tastarii
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " has waited enough, now driving...");
                        Main.cars.remove(Main.cars.get(0));

                    }
                };
            case "simple_n_roundabout":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        System.out.println("Car " + car.getId() + " has reached the roundabout, now waiting...");
                        synchronized (Main.class) {
                            while (Main.currentCarsInRoundabout == Main.maxCarsNoInRoundabout) ;
                            System.out.println("Car " + car.getId() + " has entered the roundabout");
                            Main.currentCarsInRoundabout++;
                        }
                        try {
                            sleep(Main.maxTimeForCarInRoundabout);
                        } catch (InterruptedException e) {        // Dezactivat sleep doar pt usurarea tastarii
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " has exited the roundabout after " + Main.maxTimeForCarInRoundabout / 1000 + " seconds");
                        synchronized (IntersectionHandlerFactory.class) {
                            Main.currentCarsInRoundabout--;
                        }

                    }
                };
            case "simple_strict_1_car_roundabout":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        System.out.println("Car " + car.getId() + " has reached the roundabout");
                        Semaphore semaphore = Main.lanes.get(car.getStartDirection());

                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {     // Cate una din fiecare
                            e.printStackTrace();
                        }

                        int lane;
                        if (Main.lanes.get(car.getStartDirection() + 1) == null) {
                            lane = 0;
                        } else {
                            lane = car.getStartDirection() + 1;
                        }

                        System.out.println("Car " + car.getId() + " has entered the roundabout from lane " + car.getStartDirection());

                        try {
                            sleep(car.getWaitingTime());
                        } catch (InterruptedException e) {            //Dezactivat sleep doar pt usurarea tastarii
                            e.printStackTrace();
                        }

                        Main.lanes.get(lane).release(1);     // Lane X da drumu unei masini din X + 1
                        System.out.println("Car " + car.getId() + " has exited the roundabout after " + Main.maxTimeForCarInRoundabout / 1000 + " seconds");
                    }
                };
            case "simple_strict_x_car_roundabout":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        System.out.println("Car " + car.getId() + " has reached the roundabout, now waiting...");
                        try {
                            Main.cyclicBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }
                        Semaphore semaphore = Main.lanes.get(car.getStartDirection());
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {      // Tot cate una
                            e.printStackTrace();
                        }

                        try {
                            Main.letCarsGoThrough.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " was selected to enter the roundabout from lane " + car.getStartDirection());
                        int lane;
                        if (Main.lanes.get(car.getStartDirection() + 1) == null) {
                            lane = 0;
                        } else {
                            lane = car.getStartDirection() + 1;
                        }
                        Main.lanes.get(lane).release(1);


                        try {
                            Main.waitToEnterRoundaboutBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {     // Le asteptam pe cele strict X
                            e.printStackTrace();
                        }


                        System.out.println("Car " + car.getId() + " has entered the roundabout from lane " + car.getStartDirection());
                        try {
                            sleep(car.getWaitingTime());
                        } catch (InterruptedException e) {        //Dezactivat sleep doar pt usurarea tastarii
                            e.printStackTrace();
                        }
                        try {
                            Main.beforeOutOfRoundabound.await();
                        } catch (InterruptedException | BrokenBarrierException e) {      // Le asteptam sa intre toate in sens inainte sa iasa
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " has exited the roundabout after " + Main.maxTimeForCarInRoundabout / 1000 + " seconds");

                        try {
                            Main.outOfRoundabound.await();
                        } catch (InterruptedException | BrokenBarrierException e) {      // Dupa ce sunt strict X, ies din sens
                            e.printStackTrace();
                        }

                        if (Main.letCarsGoThrough.availablePermits() == 0) {
                            Main.letCarsGoThrough.release(Main.maxCarsNoInRoundabout * (Main.maxLane + 1));
                        }

                    }
                };
            case "simple_max_x_car_roundabout":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        // Get your Intersection instance

                        try {
                            sleep(car.getWaitingTime());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } // NU MODIFICATI

                        // Continuati de aici

                        System.out.println("Car " + car.getId() + " has reached the roundabout from lane " + car.getStartDirection());
                        Semaphore semaphore = Main.lanes.get(car.getStartDirection());
                        try {
                            semaphore.acquire();
                        } catch (InterruptedException e) {            // Semafor cu X intrari, ca sa fie maxim X la un moment dat
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " has entered the roundabout from lane " + car.getStartDirection());
                        try {
                            sleep(car.getWaitingTime());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " has exited the roundabout after " + Main.maxTimeForCarInRoundabout / 1000 + " seconds");
                        Main.lanes.get(car.getStartDirection()).release();      // Cine iese, da celuilalt voie


                    }
                };
            case "priority_intersection":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        // Get your Intersection instance

                        try {
                            sleep(car.getWaitingTime());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } // NU MODIFICATI

                        // Continuati de aici
                        if (car.getPriority() > 1) {
                            try {
                                Main.priorityIntersectionSemaphore.acquire();           // Un semafor pt cele prioritare
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Car " + car.getId() + " with high priority has entered the intersection");
                            try {
                                sleep(2000);                 // 2 secunde se cerea in enunt
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            System.out.println("Car " + car.getId() + " with high priority has exited the intersection");
                            Main.priorityIntersectionSemaphore.release();
                        } else {
                            System.out.println("Car " + car.getId() + " with low priority is trying to enter the intersection...");
                            try {
                                Main.orderedSemaphore.acquire();          // Daca vin mai multe, le vrem in ordinea sosirii, bazat pe semafor fair
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            while (Main.priorityIntersectionSemaphore.availablePermits() != Main.carsNoWithHighPriority)     // Daca exista masini prioritare in intersectie stam pe loc
                                ;
                            System.out.println("Car " + car.getId() + " with low priority has entered the intersection");
                            Main.orderedSemaphore.release();
                        }


                    }
                };
            case "crosswalk":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        while (true) {
                            if (Pedestrians.isPass()) {         // Daca trec cetateni, le dam rosu
                                if ((car.carPass == null || CarPass.GOT_GREEN.equals(car.carPass))) {
                                    car.carPass = CarPass.GOT_RED;
                                    System.out.println("Car " + car.getId() + " has now red light");
                                }
                            } else {       // Altfel le dam verde
                                if (CarPass.GOT_RED.equals(car.carPass) || car.carPass == null) {
                                    car.carPass = CarPass.GOT_GREEN;
                                    System.out.println("Car " + car.getId() + " has now green light");
                                }
                            }
                            if (Pedestrians.isFinished() && CarPass.GOT_GREEN.equals(car.carPass))    // Daca si au luat verde deja si s-a terminat cu pietonii putem iesi
                                break;
                        }
                    }
                };
            case "simple_maintenance":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        System.out.println("Car " + car.getId() + " from side number " + car.getStartDirection() + " has reached the bottleneck");
                        int lane = car.getStartDirection();
                        try {
                            MaintenanceHelper.mentenanceSemaphores.get(car.getStartDirection()).acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int nextLane;
                        if (lane == 0) {
                            nextLane = 1;
                        } else {
                            nextLane = 0;
                        }
                        synchronized (IntersectionHandlerFactory.class) {
                            leadingThread = car.getId();
                        }
                        System.out.println("Car " + car.getId() + " from side number " + lane + " has passed the bottleneck");
                        try {
                            MaintenanceHelper.maintenanceBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {      // Asteptam X dintr-o parte
                            e.printStackTrace();
                        }
                        if (car.getId() == leadingThread) {
                            MaintenanceHelper.mentenanceSemaphores.get(nextLane).release(MaintenanceHelper.carNoMaxForMentenance); // Un singur thread da drumu la X din cealalta parte
                        }

                    }
                };
            case "complex_maintenance":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        try {
                            NewLanesHelper.complexMaintenanceSemaphores.get(car.getStartDirection()).acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Car " + car.getId() + " has come from the lane number " + car.getStartDirection());
                        for (NewLane newLane : NewLanesHelper.newLanes) {
                            if (newLane.getOldLanesDirections().contains(car.getStartDirection())) {
                                newLane.getOldLanes().get(car.getStartDirection()).add(car);
                                break;
                            }
                        }
                        NewLanesHelper.complexMaintenanceSemaphores.get(car.getStartDirection()).release();
                        try {
                            NewLanesHelper.complexMaintenanceBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {                 // Explicatie completa in README
                            e.printStackTrace();
                        }
                        int correspondingLaneId = -1;
                        for (int i = 0; i < NewLanesHelper.newLanes.size(); i++) {
                            if (NewLanesHelper.newLanes.get(i).getOldLanesDirections().contains(car.getStartDirection())) {
                                correspondingLaneId = NewLanesHelper.newLanes.get(i).getID();
                                break;
                            }
                        }
                        while (true) {
                            try {
                                NewLanesHelper.complexMaintenanceEntranceSemaphores.get(correspondingLaneId).acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (NewLanesHelper.getCorrespondingLane(car.getStartDirection()) == null) {
                                NewLanesHelper.complexMaintenanceEntranceSemaphores.get(correspondingLaneId).release();
                                break;
                            }
                            Boolean result = NewLanesHelper.getCorrespondingLane(car.getStartDirection())
                                    .passCar(car.getStartDirection());
                            NewLanesHelper.complexMaintenanceEntranceSemaphores.get(correspondingLaneId).release();
                            if (result == null)
                                break;
                        }

                    }
                };
            case "railroad":
                return new IntersectionHandler() {
                    @Override
                    public void handle(Car car) {
                        System.out.println("Car " + car.getId() + " from side number " + car.getStartDirection() + " has stopped by the railroad");

                        try {
                            Main.orderedSemaphore.acquire();     // semafor fair
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        RailRoadHelper.orderedCars.add(car);     // Adaugate in ordinea sosirii

                        try {
                            RailRoadHelper.railroadBarrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            e.printStackTrace();
                        }

                        synchronized (IntersectionHandlerFactory.class) {
                            if (leadingThread == -1) {
                                leadingThread = car.getId();
                                System.out.println("The train has passed, cars can now proceed");       // Un singur thread da drumu la tren
                            }
                        }
                        while (RailRoadHelper.orderedCars.get(0).getId() != car.getId()) ;
                        System.out.println("Car " + RailRoadHelper.orderedCars.get(0).getId() + " from side number " + RailRoadHelper.orderedCars.get(0).getStartDirection() + " has started driving");
                        RailRoadHelper.orderedCars.remove(RailRoadHelper.orderedCars.get(0));

                    }
                };
            default:
                return null;
        }
    }
}
