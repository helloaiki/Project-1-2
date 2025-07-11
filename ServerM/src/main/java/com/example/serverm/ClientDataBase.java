package com.example.serverm;
import java.sql.*;
import java.util.ArrayList;

public class ClientDataBase
{
    private static final String URL="jdbc:sqlite:clients.db";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }


    public static void connect()
    {
        createClientTable();
    }

    private static void createClientTable()
    {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            );
            """;
        try (Connection connection=getConnection();Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean userExists(String username)throws SQLException
    {
        String sql="SELECT 1 FROM users WHERE username = ?";

        try(Connection connection=getConnection();PreparedStatement ps=connection.prepareStatement(sql))
        {
            ps.setString(1,username);
            ResultSet rs=ps.executeQuery();

            return rs.next();
        }

    }

    public static boolean validateUser(String username,String password)throws SQLException
    {
        String sql="SELECT password FROM users WHERE username = ?";
        try(Connection connection=getConnection();PreparedStatement ps=connection.prepareStatement(sql))
        {
            ps.setString(1,username);
            ResultSet rs=ps.executeQuery();
            if(rs.next())
            {
                String originalPassword=rs.getString("password");

                return originalPassword.equals(password);
            }
        }

        return false;
    }

    public static void addUser(String username,String password)
    {
        String sql="INSERT INTO users (username, password) VALUES (?, ?)";
        try(Connection connection=getConnection();PreparedStatement ps=connection.prepareStatement(sql))
        {
            ps.setString(1,username);
            ps.setString(2,password);
            ps.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }


    public static ArrayList<String>sendClientList()
    {
        ArrayList<String>clientList=new ArrayList<>();
        String sql="SELECT username FROM users";
        try(Connection connection=getConnection();Statement statement=connection.createStatement())
        {
            ResultSet rs=statement.executeQuery(sql);
            while(rs.next())
            {
                clientList.add(rs.getString("username"));
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

        return clientList;
    }


}
