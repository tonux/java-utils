import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;

public class TableMigration {
    public static void main(String[] args) {
        // Set up source and destination databases
        DriverManagerDataSource sourceDS = new DriverManagerDataSource();
        sourceDS.setDriverClassName("com.mysql.cj.jdbc.Driver");
        sourceDS.setUrl("jdbc:mysql://sourceDBHost/sourceDBName");
        sourceDS.setUsername("sourceDBUsername");
        sourceDS.setPassword("sourceDBPassword");

        DriverManagerDataSource destDS = new DriverManagerDataSource();
        destDS.setDriverClassName("com.mysql.cj.jdbc.Driver");
        destDS.setUrl("jdbc:mysql://destDBHost/destDBName");
        destDS.setUsername("destDBUsername");
        destDS.setPassword("destDBPassword");

        // Create JdbcTemplate objects for source and destination databases
        JdbcTemplate sourceJdbcTemplate = new JdbcTemplate(sourceDS);
        JdbcTemplate destJdbcTemplate = new JdbcTemplate(destDS);

        // Retrieve column names and types from source database
        List<Map<String, Object>> columns = sourceJdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tableName'");

        // Generate SQL statement to create table in destination database
        StringBuilder createTableSQL = new StringBuilder("CREATE TABLE tableName (");
        for (Map<String, Object> column : columns) {
            createTableSQL.append(column.get("COLUMN_NAME")).append(" ").append(column.get("DATA_TYPE")).append(",");
        }
        createTableSQL.deleteCharAt(createTableSQL.length() - 1); // Remove last comma
        createTableSQL.append(")");

        // Create table in destination database using generated SQL statement
        destJdbcTemplate.execute(createTableSQL.toString());

        // Retrieve data from table in source database
        List<Map<String, Object>> tableData = sourceJdbcTemplate.queryForList(
                "SELECT * FROM tableName");

        // Generate SQL statement to insert data into table in destination database
        StringBuilder insertDataSQL = new StringBuilder("INSERT INTO tableName (");
        for (Map<String, Object> column : columns) {
            insertDataSQL.append(column.get("COLUMN_NAME")).append(",");
        }
        insertDataSQL.deleteCharAt(insertDataSQL.length() - 1); // Remove last comma
        insertDataSQL.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            insertDataSQL.append("?,");
        }
        insertDataSQL.deleteCharAt(insertDataSQL.length() - 1); // Remove last comma
        insertDataSQL.append(")");

        // Insert data into table in destination database using generated SQL statement
        for (Map<String, Object> row : tableData) {
            Object[] rowValues = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                rowValues[i] = row.get(columns.get(i).get("COLUMN_NAME"));
            }
            destJdbcTemplate.update(insertDataSQL.toString(), rowValues);
        }
    }
}
