# JANE — JAva Neural network Engine

JANE is a general-purpose neural network and machine learning engine written entirely from scratch in Java (no ML/AI libraries like DL4J, Weka, or TensorFlow). The core `NeuralNetwork` class is domain-agnostic and supports two independent learning modes:

1. **Evolve Mode**: The network learns to make decisions through a genetic algorithm (trial-and-error over generations). Demonstrated via a **Flappy Bird** simulation with simultaneous AI-controlled birds.
2. **Predict Mode**: The network learns to forecast numeric values via backpropagation (supervised learning). Demonstrated via a **Weather Forecasting** example, predicting tomorrow's max temperature from recent days' weather data.

## Features

- **Core Engine**: `Matrix`, `ActivationFunction` (Sigmoid, ReLU), `NeuralNetwork` (forward pass, backpropagation, crossover, mutation), `GeneticAlgorithm`.
- **Evolve Mode (Flappy Bird)**: Plug-in architecture via `Environment` and `Agent` interfaces. Swing-based simulation and network visualization.
- **Predict Mode (Weather)**: Backpropagation training, normalizers, and synthetic weather data generation.
- **Persistence**: SQLite and JDBC for persisting session data, generations, best-performing genomes (weights), and weather training runs.

## Prerequisites

- Java 17+
- Maven

## How to Build & Run

To compile the project and run the tests:
```bash
mvn clean install
```

To start the application GUI:
```bash
mvn exec:java
```

## Structure Overview

- `com.jane.core` - Domain-agnostic neural network engine and genetic algorithm.
- `com.jane.env` - Interfaces for Evolve mode agents and environments.
- `com.jane.flappy` - Flappy Bird environment implementation.
- `com.jane.predict` - Weather forecasting implementation and backprop predictor.
- `com.jane.db` - SQLite JDBC database persistence.
- `com.jane.gui` - Java Swing based GUI (MainFrame, Simulation, Weather tabs).
- `com.jane.util` - Configurations and utilities.
