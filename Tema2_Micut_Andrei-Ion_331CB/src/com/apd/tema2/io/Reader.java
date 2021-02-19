/*
Micut Andrei-Ion
Grupa 331CB
 */

package com.apd.tema2.io;

import com.apd.tema2.Main;
import com.apd.tema2.entities.Car;
import com.apd.tema2.entities.ReaderHandler;
import com.apd.tema2.factory.CarFactory;
import com.apd.tema2.factory.ReaderHandlerFactory;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Clasa de baza care va realiza citirea.
 */
public class Reader {
    private BufferedReader br;

    private static Reader instance;

    private Reader(final String filePath) {
        try {
            File file = new File(filePath);
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Reader getInstance(final String filePath) {
        if (instance == null) {
            instance = new Reader(filePath);
        }

        return instance;
    }

    public Set<Thread> getCarsFromInput() {
        Set<Thread> cars = new HashSet<>();

        try {
            String handlerType = br.readLine();
            ReaderHandler readerHandler = ReaderHandlerFactory.getHandler(handlerType);

            Main.carsNo = Integer.parseInt(br.readLine());
            for (int i = 0; i != Main.carsNo; ++i) {
                Car car = CarFactory.getCar(handlerType, br.readLine().split(" "));
                cars.add(new Thread(car));
                Main.cars.add(car);
            }
            readerHandler.handle(handlerType, br);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cars;
    }


}
