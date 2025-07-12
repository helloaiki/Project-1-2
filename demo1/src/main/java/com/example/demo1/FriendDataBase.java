package com.example.demo1;

import java.sql.*;
import java.util.ArrayList;

public class FriendDataBase
{
    private static String userName;
    private static String URL;

    public static void initialize(String user)
    {
        userName = user;
        URL = "jdbc:sqlite:" + user + "_data.db";
        createTable();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }




    public static void connect()
    {

        createTable();

    }

    public static void createTable()
    {
        try (Connection connection=getConnection();Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS friends (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sender TEXT NOT NULL,
                    receiver TEXT NOT NULL,
                    message TEXT NOT NULL,
                    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                );
            """);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void addFriend(String username)
    {
        String sql="INSERT OR IGNORE INTO friends (username) VALUES (?) ";
        try(Connection connection=getConnection();PreparedStatement ps=connection.prepareStatement(sql))
        {
            ps.setString(1,username);
            ps.executeUpdate();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean checkFriend(String username)
    {
        String sql="SELECT 1 FROM friends WHERE username = ?";
        try(Connection connection=getConnection();PreparedStatement ps=connection.prepareStatement(sql))
        {
            ps.setString(1,username);
            ResultSet rs=ps.executeQuery();
            return rs.next();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public static void saveMessages(String sender,String receiver,String message)
    {
        String sql="INSERT INTO messages (sender, receiver, message) VALUES (?, ?, ?) ";
        try(Connection connection=getConnection();PreparedStatement ps=connection.prepareStatement(sql))
        {
            ps.setString(1,sender);
            ps.setString(2,receiver);
            ps.setString(3,message);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    public static ArrayList<String>sendFriendList()
    {
        ArrayList<String>friendList=new ArrayList<>();
        String sql="SELECT username FROM friends";
        try(Connection connection=getConnection();Statement statement=connection.createStatement())
        {
            ResultSet rs=statement.executeQuery(sql);
            while(rs.next())
            {
                friendList.add(rs.getString("username"));
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

        return friendList;
    }

    public static ArrayList<String> loadMessages(String username,String friendname)
    {

        ArrayList<String> messages = new ArrayList<>();
        String sql = """
        SELECT sender, message, timestamp FROM messages
        WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)
        ORDER BY timestamp ASC
    """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, friendname);
            ps.setString(3, friendname);
            ps.setString(4, username);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");
                messages.add(sender + ":" + message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

}
