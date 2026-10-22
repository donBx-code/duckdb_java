package com.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Generates a year of synthetic daily weather observations per city
 * and writes them to a Parquet file using DuckDB.
 */
public class WeatherDataGenerator
{
    private static final String OUTPUT_DIR = "src/resources/data";
    private static final String OUTPUT_FILE = "weather_by_city.parquet";

    private static final String WEATHER_SQL =
        "WITH cities(city, country, lat) AS (VALUES " +
        "  ('Berlin', 'DE', 52.5), ('Madrid', 'ES', 40.4), ('Oslo', 'NO', 59.9), " +
        "  ('Cairo', 'EG', 30.0), ('Tokyo', 'JP', 35.7), ('Sydney', 'AU', -33.9), " +
        "  ('Toronto', 'CA', 43.7), ('Mumbai', 'IN', 19.1)), " +
        "days AS (SELECT CAST(d AS DATE) AS obs_date " +
        "         FROM range(DATE '2025-01-01', DATE '2026-01-01', INTERVAL 1 DAY) t(d)), " +
        "base AS (" +
        "  SELECT c.city, c.country, c.lat, dy.obs_date, " +
        "         30 - abs(c.lat) * 0.6 AS base_temp, " +
        "         4 + abs(c.lat) / 10 AS amplitude, " +
        "         CASE WHEN c.lat < 0 THEN -1 ELSE 1 END AS hemisphere " +
        "  FROM cities c CROSS JOIN days dy) " +
        "SELECT city, country, obs_date, " +
        "       round(base_temp + amplitude * hemisphere * " +
        "             sin(2 * pi() * (dayofyear(obs_date) - 105) / 365.0) + " +
        "             (random() + random() + random() - 1.5) * 2, 1) AS temperature_c, " +
        "       round(least(100, greatest(20, 60 + (random() - 0.5) * 40)), 0) AS humidity_pct, " +
        "       round(5 + random() * 25, 1) AS wind_kmh, " +
        "       CASE WHEN random() < 0.3 THEN round(random() * 15, 1) ELSE 0.0 END AS precipitation_mm, " +
        "       CASE WHEN random() < 0.3 AND random() > 0.5 THEN 'Rain' " +
        "            WHEN random() < 0.3 THEN 'Drizzle' ELSE 'Clear' END AS condition " +
        "FROM base " +
        "ORDER BY city, obs_date";

    public static void main( String[] args ) throws Exception
    {
        Path dir = Paths.get(OUTPUT_DIR);
        Files.createDirectories(dir);
        String outFile = dir.toAbsolutePath().resolve(OUTPUT_FILE).toString().replace("'", "''");

        Class.forName("org.duckdb.DuckDBDriver");
        try ( Connection conn = DriverManager.getConnection("jdbc:duckdb:");
              Statement stmt = conn.createStatement() )
        {
            stmt.execute("SELECT setseed(0.42)");
            stmt.execute("COPY (" + WEATHER_SQL + ") TO '" + outFile + "' (FORMAT PARQUET)");
        }

        System.out.println( "Wrote weather data to " + outFile );
    }
}
