package com.jane.predict.weather;

import java.time.LocalDate;

public record WeatherRecord(
    LocalDate date,
    double tempMax,
    double tempMin,
    double humidity,
    double pressure,
    double windSpeed
) {}
