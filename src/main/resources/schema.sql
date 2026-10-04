CREATE TABLE IF NOT EXISTS sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mode TEXT NOT NULL,                 -- 'EVOLVE' or 'PREDICT'
    label TEXT,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS generations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL REFERENCES sessions(id),
    gen_number INTEGER NOT NULL,
    best_fitness REAL NOT NULL,
    avg_fitness REAL NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS genomes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    generation_id INTEGER NOT NULL REFERENCES generations(id),
    weights_json TEXT NOT NULL,         -- serialized NeuralNetwork weights
    fitness REAL NOT NULL,
    is_best INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS weather_runs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id INTEGER NOT NULL REFERENCES sessions(id),
    epoch INTEGER NOT NULL,
    loss REAL NOT NULL,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS weather_predictions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    run_id INTEGER NOT NULL REFERENCES weather_runs(id),
    date TEXT NOT NULL,
    actual_temp REAL NOT NULL,
    predicted_temp REAL NOT NULL
);
