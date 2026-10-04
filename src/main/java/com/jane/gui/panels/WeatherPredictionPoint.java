package com.jane.gui.panels;

import java.time.LocalDate;

public record WeatherPredictionPoint(
        LocalDate date,
        double actualTemp,
        double predictedTemp
) {}
