package main;

import java.sql.*;

public class DatabaseConnection
{
    public static Connection getConnection()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection("jdbc:sqlite:src/Database/dict_hh.db");
        }
        catch(Exception e)
        {
            System.out.println(e);
            return null;
        }
    }
}
