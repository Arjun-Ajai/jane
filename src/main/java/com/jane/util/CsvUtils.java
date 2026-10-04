package com.jane.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class CsvUtils {
    
    public static void generateSyntheticWeatherCsv(String filepath, int days) {
        File file = new File(filepath);
        file.getParentFile().mkdirs();
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("date,temp_max,temp_min,humidity,pressure,wind_speed\n");
            
            LocalDate date = LocalDate.of(2020, 9, 1);
            for (int i = 0; i < days; i++) {
                // Keep this presentation dataset in a mild, above-freezing season.
                double dayOfYear = date.getDayOfYear();
                double seasonOffset = Math.sin(dayOfYear / 365.0 * 2 * Math.PI);
                
                double tempMax = 22 + seasonOffset * 4 + (Math.random() * 4 - 2);
                double tempMin = tempMax - 5 - (Math.random() * 5);
                double humidity = 50 + (Math.random() * 40 - 20); // 30 to 70
                double pressure = 1013 + (Math.random() * 20 - 10);
                double windSpeed = 10 + (Math.random() * 10 - 5);
                
                bw.write(String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f\n", 
                        date.toString(), tempMax, tempMin, humidity, pressure, windSpeed));
                
                date = date.plusDays(1);
            }
            System.out.println("Synthetic weather dataset generated at " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
