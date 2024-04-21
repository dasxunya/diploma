package org.dasxunya.diploma.Generator.ExpectedTests;

import org.dasxunya.diploma.Generator.SampleTestClasses.Car;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCar {

    @Test
    void testConstructorAndGetters() {
        Car car = new Car("Toyota", "Camry", 2020, 25000.0);

        assertEquals("Toyota", car.getBrand());
        assertEquals("Camry", car.getModel());
        assertEquals(2020, car.getYear());
        assertEquals(25000.0, car.getPrice());
    }

    @Test
    void testSetters() {
        Car car = new Car("Toyota", "Camry", 2020, 25000.0);

        car.setBrand("Honda");
        car.setModel("Accord");
        car.setYear(2021);
        car.setPrice(30000.0);

        assertEquals("Honda", car.getBrand());
        assertEquals("Accord", car.getModel());
        assertEquals(2021, car.getYear());
        assertEquals(30000.0, car.getPrice());
    }

    @Test
    void testGetCarInfo() {
        Car car = new Car("Toyota", "Camry", 2020, 25000.0);
        String expectedInfo = "Brand: Toyota, Model: Camry, Year: 2020, Price: $25000.0";

        assertEquals(expectedInfo, car.getCarInfo());
    }

    @Test()
    void testDrive() {
        Car car = new Car("Toyota", "Camry", 2020, 25000.0);

        // Redirect System.out to catch output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        car.drive();
        assertEquals("The car is being driven.\n", outContent.toString());
    }
}

