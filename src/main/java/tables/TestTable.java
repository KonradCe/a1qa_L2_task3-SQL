package tables;

import aquality.selenium.core.logging.Logger;
import aquality.selenium.core.utilities.ISettingsFile;
import aquality.selenium.core.utilities.JsonSettingsFile;
import models.TestModel;
import utils.DatabaseUtils;
import utils.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class TestTable {

    private static final ISettingsFile testData = new JsonSettingsFile("TestData.json");
    private static final Logger logger = Logger.getInstance();

    public static int insertTest(TestModel testModel) {
        PreparedStatement insertStatement = null;
        int generatedId = -1;
        try {
            insertStatement = DatabaseUtils.getConnection()
                    .prepareStatement(testData.getValue("/testTableQueries/insertTest").toString(), Statement.RETURN_GENERATED_KEYS);
            fillOutStatementWithTestData(insertStatement, testModel);

            int insertedRows = insertStatement.executeUpdate();
            logger.info("Inserted %d rows", insertedRows);
            ResultSet generatedIds = insertStatement.getGeneratedKeys();
            if (generatedIds.next()) {
                generatedId = generatedIds.getInt(1);
            } else {
                throw new SQLException("Inserting test data, no Id obtained.");
            }
        } catch (SQLException e) {
            logger.error("Error inserting into test table: " + e.getMessage());
        } finally {
            DatabaseUtils.closeConnection();
        }
        return generatedId;
    }

    public static void updateTest(TestModel testModel) {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = DatabaseUtils.getConnection().prepareStatement(testData.getValue("/testTableQueries/updateTest").toString());
            fillOutStatementWithTestData(updateStatement, testModel);
            updateStatement.setInt(11, testModel.getId());

            int updatedRowsNb = updateStatement.executeUpdate();
            logger.info("Updated %d rows", updatedRowsNb);
        } catch (SQLException e) {
            logger.error("Error updating records in test table: " + e.getMessage());
        } finally {
            DatabaseUtils.closeConnection();
        }
    }

    public static void deleteTest(int testId) {
        PreparedStatement deleteStatement = null;
        try {
            deleteStatement = DatabaseUtils.getConnection().prepareStatement(testData.getValue("/testTableQueries/deleteTest").toString());
            deleteStatement.setInt(1, testId);
            int deletedRowsNb = deleteStatement.executeUpdate();
            logger.info("Deleted %d rows", deletedRowsNb);
        } catch (SQLException e) {
            logger.error("Error deleting record from test table: " + e.getMessage());
        } finally {
            DatabaseUtils.closeConnection();
        }
    }

    private static void fillOutStatementWithTestData(PreparedStatement statement, TestModel testModel) throws SQLException {
        statement.setString(1, testModel.getName());
        statement.setInt(2, testModel.getStatusId());
        statement.setString(3, testModel.getMethodName());
        statement.setInt(4, testModel.getProjectId());
        statement.setInt(5, testModel.getSessionId());
        statement.setTimestamp(6, testModel.getStartTime());
        statement.setTimestamp(7, testModel.getEndTime());
        statement.setString(8, testModel.getEnv());
        statement.setString(9, testModel.getBrowser());
        statement.setInt(10, testModel.getAuthorId());
    }

    public static ArrayList<Object[]> getSemiRandomTests() {
        ArrayList<Object[]> result = new ArrayList<>();
        PreparedStatement statement = null;
        String randomIdDigits = StringUtils.getRandomDoubleDigitString();
        logger.info("Looking up to 10 test records which contain repeating digits in id: " + randomIdDigits);
        try {
            statement = DatabaseUtils.getConnection().prepareStatement(testData.getValue("/testTableQueries/selectRandomTests").toString());
            statement.setString(1, randomIdDigits);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                TestModel testModel = new TestModel();
                testModel.setId(rs.getInt("id"));
                testModel.setName(rs.getString("name"));
                testModel.setStatusId(rs.getInt("status_id"));
                testModel.setMethodName(rs.getString("method_name"));
                testModel.setProjectId(rs.getInt("project_id"));
                testModel.setSessionId(rs.getInt("session_id"));
                testModel.setStartTime(StringUtils.convertStringToTimestamp(rs.getString("start_time")));
                testModel.setEndTime(StringUtils.convertStringToTimestamp(rs.getString("end_time")));
                testModel.setEnv(rs.getString("env"));
                testModel.setBrowser(rs.getString("browser"));
                testModel.setAuthorId(rs.getInt("author_id"));
                result.add(new Object[]{testModel});
            }
        } catch (SQLException e) {
            logger.error("Error selecting random test, with id containing " + randomIdDigits + ": " + e.getMessage());
        } finally {
            DatabaseUtils.closeConnection();
        }
        return result;
    }
}
