
import java.sql.*;
import java.util.Scanner;

public class Main {

    private static Connection connection;
    private static Statement statement;

    // 1. Create table GOODS(id, prodid, title, cost) using query from Java application
    // id - record number, primary key
    // prodid - unique good number
    // title - good name
    // cost - as is

    final static String CREATE_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS goods (" +
            "id INTEGER PRIMARY KEY," +
            "prodid INTEGER UNIQUE," +
            "title TEXT," +
            "cost INTEGER);";

    final static String ADD_GOOD_QUERY =
            "INSERT INTO goods (prodid, title, cost) VALUES (?, ?, ?)";

    final static String GET_COST_BY_NAME_QUERY =
            "SELECT cost FROM goods WHERE title = ?";

    final static String CHANGE_COST_BY_NAME_QUERY =
            "UPDATE goods SET cost = ? WHERE title = ?";

    final static String GET_GOODS_BY_COST_RANGE_QUERY =
            "SELECT prodid, title, cost FROM goods WHERE COST BETWEEN ? AND ? ORDER BY prodid";

    public static void main(String[] args) {

        connect();

        // 1. Create table GOODS(id, prodid, title, cost) using query from Java application
        try {
            statement.execute(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Clear table goods and add 10'000 goods like
        // prodid 1 title 'good1' cost 10
        // ...
        // prodid 10000 title 'good10000' cost 100010
        try {
            statement.execute("DELETE FROM goods");

            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(ADD_GOOD_QUERY);
            for (int i = 0; i < 10000; i++) {
                ps.setInt(1, i + 1);
                ps.setString(2, "good" + (i + 1));
                ps.setInt(3, (i + 1) * 10);
                ps.executeUpdate();
            }
            connection.setAutoCommit(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Create console applicationto perform tasks:
        // - return good cost (or "good not found" message) by good name (command "/cost goodName")
        // - change good cost by good name (command "/changecost goodName newCost")
        // - show goods in given cost range (command "/goodsbycost cost1 cost2")
        Scanner sc = new Scanner(System.in);
        String userInput = "";
        System.out.println("Available commands:\n" +
                "/cost goodName - show good cost by good name;\n" +
                "/changecost goodName newCost - change good cost by name;\n" +
                "/goodsbycost cost1 cost2 - show goods in cosr range from cost1 to cost2;\n" +
                "/exit - to end application\n\n");
        while (true) {
            System.out.println("Enter command: ");
            userInput = sc.nextLine();
            if (userInput.equals("/exit"))
                break;
            processUserInput(userInput);
        }

        disconnect();
    }

    static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:store.sqlite");
            statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void disconnect() {
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();;
        }
    }

    static void processUserInput(String userInput) {
        String[] cmd = userInput.trim().split("\\s+");
        if (cmd.length == 0) {
            System.out.println("Empty command !!!");
            return;
        }
        if (cmd[0].equals("/cost")) {
            if (cmd.length < 2) {
                System.out.println("Good name must be given !!!");
                return;
            }
            try {
                PreparedStatement ps = connection.prepareStatement(GET_COST_BY_NAME_QUERY);
                ps.setString(1, cmd[1]);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    System.out.println("Cost: " + rs.getInt(1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
        if (cmd[0].equals("/changecost")) {
            if (cmd.length < 3) {
                System.out.println("Good name and new cost must be given !!!");
                return;
            }
            int cost;
            try {
                cost = Integer.parseInt(cmd[2]);
            } catch (Exception e) {
                System.out.println("Error in new price !!!");
                return;
            }
            try {
                PreparedStatement ps = connection.prepareStatement(CHANGE_COST_BY_NAME_QUERY);
                ps.setInt(1, cost);
                ps.setString(2, cmd[1]);
                System.out.println("Affected on rows: " + ps.executeUpdate());
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
        if (cmd[0].equals("/goodsbycost")) {
            if (cmd.length < 3) {
                System.out.println("Start cost & end cost must be given !!!");
                return;
            }
            int startCost, endCost;
            try {
                startCost = Integer.parseInt(cmd[1]);
                endCost = Integer.parseInt(cmd[2]);
            } catch (Exception e) {
                System.out.println("Error in parameters !!!");
                return;
            }
            if (startCost > endCost) {
                System.out.println("Start cost must be less then end cost !!!");
                return;
            }
            try {
                PreparedStatement ps = connection.prepareStatement(GET_GOODS_BY_COST_RANGE_QUERY);
                ps.setInt(1, startCost);
                ps.setInt(2, endCost);
                ResultSet rs = ps.executeQuery();
                while (rs.next())
                    System.out.println("prodid: " + rs.getInt(1)
                            + ", title: " + rs.getString(2)
                            + ", cost: " + rs.getInt(3));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("Unrecognized command !!!");
    }
}
