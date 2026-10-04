package com.jane.predict.weather;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeatherCsvLoader {
    
    public static List<WeatherRecord> load(String filepath) {
        List<WeatherRecord> records = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                if (cols.length >= 6) {
                    LocalDate date = LocalDate.parse(cols[0]);
                    double tempMax = Double.parseDouble(cols[1]);
                    double tempMin = Double.parseDouble(cols[2]);
                    double humidity = Double.parseDouble(cols[3]);
                    double pressure = Double.parseDouble(cols[4]);
                    double windSpeed = Double.parseDouble(cols[5]);
                    
                    records.add(new WeatherRecord(date, tempMax, tempMin, humidity, pressure, windSpeed));
                }
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        
        return records;
    }
}
