/*
Micut Andrei-Ion
Grupa 331CB
*/

package com.apd.tema2.entities.helper;

import com.apd.tema2.entities.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class RailRoadHelper {
    public static CyclicBarrier railroadBarrier;
    public static List<Car> orderedCars = Collections.synchronizedList(new ArrayList<>());
}
