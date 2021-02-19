/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2.entities;

import com.apd.tema2.Main;
import com.apd.tema2.entities.helper.NewLanesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NewLane {
    private final Integer ID;
    private volatile List<Integer> oldLanesDirections;
    private HashMap<Integer, List<Car>> oldLanes;
    private AtomicInteger numberOfCarsAccesed = new AtomicInteger(0);


    public NewLane(int id) {
        this.ID = id;
        this.oldLanesDirections = Collections.synchronizedList(new ArrayList<>());
        this.oldLanes = new HashMap<>();
    }

    public List<Integer> getOldLanesDirections() {
        return oldLanesDirections;
    }

    public Boolean passCar(int startDirection) {
        synchronized (ID) {

            if (oldLanesDirections.size() == 0) {
                return null;
            }
            if (oldLanesDirections.get(0) != startDirection) {
                return false;
            }
            if (oldLanes.get(startDirection).isEmpty()) {
                oldLanesDirections.remove(oldLanesDirections.get(0));
                System.out.println("The initial lane " + startDirection + " has been emptied and removed from the new lane queue");
                return null;
            }

            Car car = oldLanes.get(startDirection).get(0);
            System.out.println("Car " + car.getId() + " from the lane " + car.getStartDirection() + " has entered lane number " + ID);
            oldLanes.get(startDirection).remove(car);

            int nr = numberOfCarsAccesed.addAndGet(1);
            if (nr == NewLanesHelper.CARS_IN_ON_GO) {
                numberOfCarsAccesed.set(0);
                if (oldLanes.get(oldLanesDirections.get(0)).isEmpty()) {
                    oldLanesDirections.remove(oldLanesDirections.get(0));
                    System.out.println("The initial lane " + startDirection + " has been emptied and removed from the new lane queue");
                    return null;
                } else {
                    int val = oldLanesDirections.get(0);
                    oldLanesDirections.remove(oldLanesDirections.get(0));
                    oldLanesDirections.add(val);
                    System.out.println("The initial lane " + startDirection + " has no permits and is moved to the back of the new lane queue");
                    return false;
                }
            }
        }
        return true;
    }

    public void setOldLanesDirections(List<Integer> oldLanesDirections) {
        this.oldLanesDirections = oldLanesDirections;
    }

    public HashMap<Integer, List<Car>> getOldLanes() {
        return oldLanes;
    }

    public void setOldLanes(HashMap<Integer, List<Car>> oldLanes) {
        this.oldLanes = oldLanes;
    }

    @Override
    public String toString() {
        return "NewLane{" +
                "ID=" + ID +
                ", oldLanesDirections=" + oldLanesDirections +
                ", oldLanes=" + oldLanes +
                ", numberOfCarsAccesed=" + numberOfCarsAccesed +
                '}';
    }

    public int getID() {
        return ID;
    }

}
