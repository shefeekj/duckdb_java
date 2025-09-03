package sqlserver;

import org.duckdb.DuckDBConnection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        String file1Path = "s3://your-bucket/path/to/file1.parquet";
        String file2Path = "s3://your-bucket/path/to/file2.parquet";

        try {
            // Step 1: Create a DuckDB in-memory connection
            // The FlightSQL server will use this connection
            Connection conn = DriverManager.getConnection("jdbc:duckdb:");
            
            // Step 2: Install and load the httpfs extension for S3 access
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSTALL httpfs;");
                stmt.execute("LOAD httpfs;");
            }
            
            // Step 3: Configure S3 credentials using environment variables
            // This is the best practice for a containerized environment
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE SECRET (TYPE S3, PROVIDER CONFIG, KEY_ID '" + accessKey + "', SECRET '" + secretKey + "', REGION '" + region + "');");
            }

            System.out.println("DuckDB instance started and S3 credentials configured.");
            
            // Step 4: Create a view combining the two Parquet files
            // This is a powerful way to make the remote data look like a table
            try (Statement stmt = conn.createStatement()) {
                String createViewSql = String.format(
                    "CREATE OR REPLACE VIEW combined_data AS " +
                    "SELECT * FROM read_parquet('%s') AS t1 JOIN read_parquet('%s') AS t2 ON t1.join_key = t2.join_key;",
                    file1Path, file2Path);
                stmt.execute(createViewSql);
            }

            System.out.println("View 'combined_data' created successfully.");
            
            // Step 5: Start the FlightSQL server
            // The FlightSQL server will expose the DuckDB instance for remote querying
            // The server will be running on port 31337 by default
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT flight_sql_server_start(31337);");
            }
            System.out.println("FlightSQL server started on port 31337.");

            // Keep the application running indefinitely
            // The server will stop when the application process terminates
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
