import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleAutomator {

    //get connection to sqlite DB
    private Connection connect() {
        String uri = "jdbc:sqlite:C:/Users/trbye/OBIE_PRCHSE.sqlite";
        Connection connection = null;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(uri);
            System.out.print("connection successful");
        } catch (SQLException e) {
            System.out.print("bad connection.");
            System.out.print(e.getMessage());
        }

        return connection;
    }

    public static void main(String[] args) throws AWTException {

        SeleniumCrawler seleniumCrawler = new SeleniumCrawler();
        OracleAutomator automator = new OracleAutomator();

        Connection connection = automator.connect();
        CSVLoader loader = new CSVLoader(connection);

        //crawl site
        try {
            seleniumCrawler.crawl();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //load csv to database
        try {
            loader.loadCSV("C:/Users/trbye/Downloads/Untitled Analysis.csv", "PRCHSE_BY_YEAR", true);
            System.out.print("loaded successfully.");
        } catch (Exception e) {
            System.out.print("bad load.");
            e.printStackTrace();
        }

        File file = new File("C:\\Users\\trbye\\Downloads\\Untitled Analysis.csv");
        file.delete();
    }
}
