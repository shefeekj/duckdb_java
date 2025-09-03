package sqlserver;

import org.duckdb.DuckDBConnection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // Retrieve the S3 pre-signed URLs from environment variables.
        // This is how a Lambda function or Docker container would pass dynamic input.
        String file1PresignedUrl = System.getenv("S3_FILE1_URL");
        String file2PresignedUrl = System.getenv("S3_FILE2_URL");

        if (file1PresignedUrl == null || file2PresignedUrl == null) {
            System.err.println("Error: S3_FILE1_URL and S3_FILE2_URL environment variables must be set.");
            System.exit(1);
        }

        try {
            // Step 1: Create a DuckDB in-memory connection
            Connection conn = DriverManager.getConnection("jdbc:duckdb:");
            
            // Step 2: Install and load the httpfs extension for S3 access.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSTALL httpfs;");
                stmt.execute("LOAD httpfs;");
            }
            
            System.out.println("DuckDB instance started and httpfs extension loaded.");
            
            // Step 3: Create a view combining the two Parquet files using the pre-signed URLs.
            try (Statement stmt = conn.createStatement()) {
                String createViewSql = String.format(
                    "CREATE OR REPLACE VIEW combined_data AS " +
                    "SELECT * FROM read_parquet('%s') AS t1 JOIN read_parquet('%s') AS t2 ON t1.join_key = t2.join_key;",
                    file1PresignedUrl, file2PresignedUrl);
                stmt.execute(createViewSql);
            }

            System.out.println("View 'combined_data' created successfully from pre-signed URLs.");
            
            // Step 4: Start the FlightSQL server.
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT flight_sql_server_start(31337);");
            }
            System.out.println("FlightSQL server started on port 31337.");

            // Keep the application running indefinitely
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
